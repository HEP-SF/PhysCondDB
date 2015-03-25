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

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import conddb.dao.repositories.IovRepository;
import conddb.dao.repositories.PayloadRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
public class IovController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
    @Autowired 
    private PayloadRepository payloadRepository;

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

}
