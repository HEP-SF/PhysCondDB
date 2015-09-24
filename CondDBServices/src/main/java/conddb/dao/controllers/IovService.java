/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package conddb.dao.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.IovRepository;
import conddb.dao.repositories.PayloadRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
public class IovService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
    @Autowired 
    private PayloadRepository payloadRepository;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloaddataRepository;

    /**
     * @param tag
     * @param preq
     * @return
     * @throws ConddbServiceException
     */
    public Iov getIov(Long id) throws ConddbServiceException {
    	try {
    		return iovRepository.findOne(id);
    	} catch (Exception e) {
    		throw new ConddbServiceException("Cannot find iov "+id+": "+e.getMessage());
    	}
    }
  
    /**
     * @param tag
     * @param preq
     * @return
     * @throws ConddbServiceException
     */
    public List<Iov> getIovsByTag(Tag tag, PageRequest preq) throws ConddbServiceException {
    	try {
    		return iovRepository.findByTag(tag, preq).getContent();
    	} catch (Exception e) {
    		throw new ConddbServiceException("Cannot look for iovs in tag "+tag.getName()+": "+e.getMessage());
    	}
    }
    
    /**
     * @param tag
     * @param since
     * @param until
     * @param preq
     * @return
     * @throws ConddbServiceException
     */
    public List<Iov> getIovsByTagBetween(String tag, BigDecimal since, BigDecimal until, PageRequest preq) throws ConddbServiceException {
    	try {
    		return iovRepository.findByRangeAndTag(tag, since, until);
    	} catch (Exception e) {
    		throw new ConddbServiceException("Cannot look for iovs in tag "+tag+" in range "+since+" "+until+": "+e.getMessage());
    	}
    }
    
    /**
     * @param preq
     * @return
     * @throws ConddbServiceException
     */
    public Page<Iov> findAll(PageRequest preq) throws ConddbServiceException {
    	try {
    		return iovRepository.findAll(preq);
    	} catch (Exception e) {
    		throw new ConddbServiceException("Cannot find iovs : "+e.getMessage());
    	}
    }
    
	@Transactional
	public Iov insertIov(Iov entity) {
		log.info("Controller searching for tag by name "+entity.getTag().getName());
		Tag atag = tagRepository.findByName(entity.getTag().getName());
		log.info("Controller has found tag name "+atag.getName());
		entity.setTag(atag);
		log.info("Controller searching for payload by hash "+entity.getPayload().getHash());
		Payload pyld = payloadRepository.findOne(entity.getPayload().getHash());
		if (pyld == null) {
			log.info("Payload not found...store it");
			pyld = payloadRepository.save(entity.getPayload());
		}
		entity.setPayload(pyld);
		/* Verify that IOV ID does not exists : this method is for insertions ONLY */
		entity.setId(null);
		/* Now search for existing since */
		List<Iov> oldiov = iovRepository.findBySinceAndTagAndInsertionTimeLessThanOrderByInsertionTimeDesc(
				atag.getName(), entity.getSince(), Timestamp.from(Instant.now()));
		if (oldiov != null && oldiov.size()>0) {
			log.info("Found a list of existing iovs..."+oldiov.get(0).getSince()
					+" - "+oldiov.get(0).getInsertionTime());
		}
		return iovRepository.save(entity);
	}

	// Payload related methods
	public Payload getPayload(String hash) throws Exception {
		try {
			Payload pyld = payloadRepository.findByHash(hash);
			if (pyld == null) {
				throw new ConddbServiceException("Cannot find payload for hash "+hash);
			}
			return pyld;
		} catch (Exception e) {
			throw e;
		}
	}

	public List<Payload> getPayloadList(String param, String condition, String value) throws Exception {
		try {
			List<Payload> pyldlist = null;
			if (param.equals("datasize")) {
				if (condition.equals("gt")) {
					pyldlist = payloadRepository.findByDatasizeGreaterThan(new Integer(value));
				}
			} else if (param.equals("objectType")) {
				if (condition.equals("eq")) {
					pyldlist = payloadRepository.findByObjectType(value);
				}
			} else if (param.equals("version")) {
				if (condition.equals("eq")) {
					pyldlist = payloadRepository.findByVersion(value);
				}
			}
			return pyldlist;
		} catch (Exception e) {
			throw e;
		}
	}


	public PayloadData getPayloadData(String hash) throws Exception {
		try {
			PayloadData pyld = payloaddataRepository.find(hash);
			if (pyld == null) {
				throw new ConddbServiceException("Cannot find payload data for hash "+hash);
			}
			return pyld;
		} catch (Exception e) {
			throw e;
		}
	}

}
