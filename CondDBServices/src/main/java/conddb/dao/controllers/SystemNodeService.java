/**
 *
 */
package conddb.dao.controllers;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import conddb.annotations.ProfileExecution;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.GlobalTagMapRepository;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.SystemNodeRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.SystemDescription;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
public class SystemNodeService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SystemNodeRepository systemNodeRepository;
	@Autowired
	private TagRepository tagRepository;
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Iterable<SystemDescription> findAllSystemNodes() throws ConddbServiceException {
		try {
			return systemNodeRepository.findAll();
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find system description list " + e.getMessage());
		}
	}
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Iterable<SystemDescription> findAllSystemNodes(PageRequest preq) throws ConddbServiceException {
		try {
			if (preq == null)
				throw new ConddbServiceException("Cannot query full table without pagination");
			return systemNodeRepository.findAll(preq);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find system description map list " + e.getMessage());
		}
	}
	
}
