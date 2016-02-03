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
package conddb.svc.dao.controllers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.GlobalTagStatus;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.Tag;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.exceptions.ConddbServiceDataIntegrityException;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.IovRepository;
import conddb.svc.dao.repositories.PayloadRepository;
import conddb.svc.dao.repositories.TagRepository;
import conddb.utils.bytes.PayloadBytesHandler;

/**
 * @author formica
 *
 */
@Service
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
	@Autowired
	private PayloadBytesHandler payloadBytesHandler;

	@Value( "${physconddb.upload.dir:/tmp}" )
	private String SERVER_UPLOAD_LOCATION_FOLDER;

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
			throw new ConddbServiceException("Cannot find iov " + id + ": " + e.getMessage());
		}
	}

	/**
	 * @param tag
	 * @param preq
	 * @param snapshot
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<Iov> getIovsByTag(Tag tag, PageRequest preq, Timestamp snapshot) throws ConddbServiceException {
		try {
			// FIXME: we are not using page request for the moment....
			log.debug("Get iovs for tag "+tag.getName());
			Long tagid = tag.getId();
			if (snapshot == null) {
				// Retrieve only the last inserted iov for every since
				return iovRepository.findByTagAndInsertionTimeMax(tagid);
			} else {
				log.debug("Call by range is using "+tagid+" snapshot "+snapshot);
				return iovRepository.findByTagAndInsertionTimeSnapshot(tagid, snapshot);
			}
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot look for iovs in tag " + tag.getName() + ": " + e.getMessage());
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
	public List<Iov> getIovsByTagBetween(Long tagid, BigDecimal since, BigDecimal until, PageRequest preq, Timestamp snapshot)
			throws ConddbServiceException {
		try {
			if (snapshot == null) {
				// Retrieve only the last inserted iov for every since
				return iovRepository.findByRangeAndTagAndInsertionTimeMax(tagid,since,until);
			} else {
				log.debug("Call by range is using "+tagid+" from "+since+" to "+until+" snapshot "+snapshot);
				return iovRepository.findByRangeAndTagAndInsertionTimeLessThan(tagid, since, until,snapshot);
			}
		} catch (Exception e) {
			throw new ConddbServiceException(
					"Cannot look for iovs in tag " + tagid + " in range " + since + " " + until + ": " + e.getMessage());
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
	public List<Iov> getLastNIovsByTag(Long tagid, PageRequest preq)
			throws ConddbServiceException {
		try {		
			// Retrieve only the last inserted iov for every since
			Page<Iov> page = iovRepository.findByTagAndInsertionTimeMax(tagid,preq);
			return page.getContent();
		} catch (Exception e) {
			throw new ConddbServiceException(
					"Cannot look for last N iovs in tag " + tagid + ": " + e.getMessage());
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
	public List<Iov> getIovsByTagNameBetween(String tag, BigDecimal since, BigDecimal until, PageRequest preq, Timestamp snapshot)
			throws ConddbServiceException {
		try {
			Tag atag = tagRepository.findByName(tag);
			if (snapshot == null) {
				// Retrieve only the last inserted iov for every since
				return iovRepository.findByRangeAndTagAndInsertionTimeMax(atag.getId(),since,until);
			} else
				return iovRepository.findByRangeAndTag(atag.getId(), since, until);
		} catch (Exception e) {
			throw new ConddbServiceException(
					"Cannot look for iovs in tag " + tag + " in range " + since + " " + until + ": " + e.getMessage());
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
			throw new ConddbServiceException("Cannot find iovs : " + e.getMessage());
		}
	}
	
	public Payload createStorablePayload(String filename, InputStream uploadedInputStream, Payload apayload) throws ConddbServiceException {
		try {
			String outfname = filename + "-uploaded";
			String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER+ "/" + outfname;
			log.debug("Upload file location is "+SERVER_UPLOAD_LOCATION_FOLDER);
//			payloadBytesHandler.saveToFile(uploadedInputStream, uploadedFileLocation);

			// Dump into file and get the hash
			String hash = payloadBytesHandler.saveToFileGetHash(uploadedInputStream, uploadedFileLocation);

			// Retrieve the file size to store it in the DB
			long fsize = payloadBytesHandler.lengthOfFile(uploadedFileLocation);
			apayload.setDatasize((int)fsize);

			PayloadData pylddata = new PayloadData();
//			pylddata.setData(bytes);
			pylddata.setHash(hash);
			pylddata.setUri(uploadedFileLocation);

//			PayloadHandler phandler = new PayloadHandler(pylddata);
//			PayloadData storable = phandler.getPayloadWithHash();
			apayload.setHash(hash);
			apayload.setData(pylddata);
			
			log.info("Uploaded object has hash " + pylddata.getHash());
			log.info("Uploaded object has data size " + apayload.getDatasize());

			return apayload;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot create storable payload "+e.getMessage());
		}		
	}
	
//	public Payload createStorablePayloadInMemory(InputStream uploadedInputStream, Payload apayload) throws ConddbServiceException {
//		try {
//			log.debug("Upload inputstream with no intermediate file ");
//			PayloadHandler phandler = new PayloadHandler(uploadedInputStream);
//			Blob blob = payloadBytesHandler.createBlobFromStream(uploadedInputStream);
//			IStreamHash ishash = phandler.createJavaIStreamHashFromStream(uploadedInputStream);
//			if (ishash == null) 
//				throw new ConddbServiceException("Cannot create storable payload : null ishash object...");
//			apayload.setDatasize((int)ishash.length);
//
//			PayloadData pylddata = new PayloadData();
//			pylddata.setData(blob);
//			pylddata.setHash(ishash.hash);
//
//			apayload.setHash(pylddata.getHash());
//			apayload.setData(pylddata);
//			
//			log.info("Uploaded object has hash " + pylddata.getHash());
//			log.info("Uploaded object has data size " + apayload.getDatasize());
//
//			return apayload;
//		} catch (Exception e) {
//			throw new ConddbServiceException("Cannot create storable payload "+e.getMessage());
//		}		
//	}


	@Transactional
	public Iov insertIov(Iov entity) throws ConddbServiceException {
		try {
			log.info("Controller searching for tag by name " + entity.getTag().getName());
			Tag atag = tagRepository.findByName(entity.getTag().getName());
			log.info("Controller has found tag name " + atag.getName());

			log.info("Controller searching for payload by hash " + entity.getPayload().getHash());
			Payload pyld = payloadRepository.findOne(entity.getPayload().getHash());
			if (pyld == null) {
				log.error("Payload not found...store it before");
				//pyld = payloadRepository.save(entity.getPayload());
				throw new ConddbServiceException("Cannot store iov if the payload is not found..");
			}
			entity.setPayload(pyld);
			/*
			 * Verify that IOV ID does not exists : this method is for
			 * insertions ONLY and the ID is internally generated
			 */
			entity.setId(null);
			/* Now search for existing since */
			List<Iov> oldiov = iovRepository.findBySinceAndTagAndInsertionTimeLessThanOrderByInsertionTimeDesc(
					atag.getName(), entity.getSince(), Timestamp.from(Instant.now()));
			if (oldiov != null && oldiov.size() > 0) {
				log.info("Found a list of existing iovs..." + oldiov.get(0).getSince() + " - "
						+ oldiov.get(0).getInsertionTime());
			}
			return iovRepository.save(entity);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot insert entity: " + e.getMessage());
		}
	}

	// Payload related methods
	public Payload getPayload(String hash) throws ConddbServiceException {
		try {
			Payload pyld = payloadRepository.findByHash(hash);
			if (pyld == null) {
				throw new ConddbServiceException("Cannot find payload for hash " + hash);
			}
			return pyld;
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	public List<Payload> getPayloadList(String param, String condition, String value) throws ConddbServiceException {
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
			throw new ConddbServiceException(e.getMessage());
		}
	}

	public PayloadData getPayloadData(String hash) throws ConddbServiceException {
		try {
			PayloadData pyld = payloaddataRepository.find(hash);
			if (pyld == null) {
				throw new ConddbServiceException("Cannot find payload data for hash " + hash);
			}
			return pyld;
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	@Transactional(rollbackFor= ConddbServiceException.class)
	public Payload insertPayload(Payload entity, PayloadData pylddata) throws ConddbServiceException {
		try {
			// Assume that hash key is already filled
			log.debug("Search for hash "+entity.getHash()+" , verify data hash ["+pylddata.getHash()+"]");
			Payload stored = payloadRepository.findOne(pylddata.getHash());
			if (stored == null) {
				log.debug("Hash not found in DB....store the payload");
				payloaddataRepository.save(pylddata);
				log.debug("Store the payload metadata : "+entity.toString());
				stored = payloadRepository.save(entity);
			} else {
				log.debug("Payload with hash "+pylddata.getHash()+" is already stored...");
			}
			return stored;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot insert payload..."+e.getMessage());
		}
	}
	
	@Transactional
	public Iov deleteIov(Long id) throws ConddbServiceException {
		try {
			Iov removable = iovRepository.findOne(id);
			Tag tag = removable.getTag();
			Tag tagwithmap = tagRepository.findByNameAndFetchGlobalTagsWithLock(tag.getName(), GlobalTagStatus.LOCKED.name());
			if (tagwithmap.getGlobalTagMaps().size()>0) {
				throw new ConddbServiceDataIntegrityException("Cannot remove IOV "+id+" from a tag associated to a locked global tag");
			}
			iovRepository.delete(id);
			return removable;
		} catch (ConddbServiceDataIntegrityException e) {
			ConddbServiceException ex = new ConddbServiceException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
	}

}
