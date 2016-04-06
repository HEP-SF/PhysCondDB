/**
 *
 */
package conddb.svc.dao.controllers;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import conddb.data.SystemDescription;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.SystemNodeRepository;

/**
 * @author formica
 *
 */
@Service
public class SystemNodeService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SystemNodeRepository systemNodeRepository;

	public SystemDescription getSystemDescription(Long id) throws ConddbServiceException {
		try {
			return systemNodeRepository.findOne(id);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find system description list " + e.getMessage());
		}
	}
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
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Page<SystemDescription> findSystemNodesByTagnameLike(String systemtag, PageRequest preq) throws ConddbServiceException {
		try {
			if (preq == null) {
				preq = new PageRequest(0, 10000);
			}
			return systemNodeRepository.findByTagNameRootLike(systemtag,preq);
		} catch (Exception e) {
			throw new ConddbServiceException("Error in finding system description list " + e.getMessage());
		}
	}
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public SystemDescription getSystemNodesByTagname(String systemtag) throws ConddbServiceException {
		try {
			return systemNodeRepository.findByTagNameRoot(systemtag);
		} catch (Exception e) {
			throw new ConddbServiceException("Error in finding system description list " + e.getMessage());
		}
	}

	/**
	 * @param systemtag
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<SystemDescription> findSystemNodesByTagNameRootLike(String systemtag) throws ConddbServiceException {
		try {
			return systemNodeRepository.findByTagNameRootLike(systemtag);
		} catch (Exception e) {
			throw new ConddbServiceException("Error in finding system description list " + e.getMessage());
		}
	}
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Page<SystemDescription> findSystemNodesByNodeFullpathLike(String nodefullpath, PageRequest preq) throws ConddbServiceException {
		try {
			return systemNodeRepository.findByNodeFullpathLike(nodefullpath,preq);
		} catch (Exception e) {
			throw new ConddbServiceException("Error in finding system description list " + e.getMessage());
		}
	}
	
	/**
	 * @param schemaName
	 * @param preq
	 * @return
	 * @throws ConddbServiceException
	 */
	public Page<SystemDescription> findSystemNodesBySchemaNameLike(String schemaName, PageRequest preq) throws ConddbServiceException {
		try {
			return systemNodeRepository.findBySchemaNameLike(schemaName, preq);
		} catch (Exception e) {
			throw new ConddbServiceException("Error in finding system description list " + e.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public SystemDescription getSystemNodesByNodeFullpath(String nodefullpath) throws ConddbServiceException {
		try {
			return systemNodeRepository.findByNodeFullpath(nodefullpath);
		} catch (Exception e) {
			throw new ConddbServiceException("Error in finding system description list " + e.getMessage());
		}
	}


	@Transactional
	public SystemDescription insertSystemDescription(SystemDescription entity)  throws ConddbServiceException {
		try {
			log.info("Controller is inserting new system " + entity.getTagNameRoot());
			return systemNodeRepository.save(entity);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot insert entity: " + e.getMessage());
		}
	}

	@Transactional
	public SystemDescription updateSystemDescription(SystemDescription entity)  throws ConddbServiceException {
		try {
			log.info("Controller is updating existing system " + entity.getTagNameRoot());
			SystemDescription stored = systemNodeRepository.findByTagNameRoot(entity.getTagNameRoot());
			if (entity.getGroupSize() != null) {
				stored.setGroupSize(entity.getGroupSize());
			} 
			if (entity.getNodeDescription() != null) {
				stored.setNodeDescription(entity.getNodeDescription());
			} 
			if (entity.getSchemaName() != null) {
				stored.setSchemaName(entity.getSchemaName());
			}
			return systemNodeRepository.save(stored);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot update entity: " + e.getMessage());
		}
	}
	
	@Transactional
	public SystemDescription delete(Long id) throws ConddbServiceException {
		try {
			SystemDescription existing = systemNodeRepository.findOne(id);
			systemNodeRepository.delete(id);
			return existing;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot delete system with id "+id);
		}
	}
}
