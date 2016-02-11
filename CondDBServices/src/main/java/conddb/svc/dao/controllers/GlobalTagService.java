/**
 *
 */
package conddb.svc.dao.controllers;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.hibernate.JDBCException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import conddb.annotations.ProfileExecution;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.GlobalTagStatus;
import conddb.data.Tag;
import conddb.svc.dao.exceptions.ConddbServiceDataIntegrityException;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.GlobalTagMapRepository;
import conddb.svc.dao.repositories.GlobalTagRepository;
import conddb.svc.dao.repositories.TagRepository;

/**
 * @author formica
 *
 */
public class GlobalTagService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository globalTagRepository;
	@Autowired
	private GlobalTagMapRepository globalTagMapRepository;
	@Autowired
	private TagRepository tagRepository;

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Iterable<GlobalTag> findAllGlobalTags() throws ConddbServiceException {
		try {
			return globalTagRepository.findAll();
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag list " + e.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Iterable<GlobalTagMap> findAllGlobalTagMaps(PageRequest preq) throws ConddbServiceException {
		try {
			if (preq == null)
				throw new ConddbServiceException("Cannot query full table without pagination");
			return globalTagMapRepository.findAll(preq);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag map list " + e.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public GlobalTagMap getGlobalTagMap(Long id) throws ConddbServiceException {
		try {
			return globalTagMapRepository.findOne(id);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag map element " + e.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public GlobalTagMap getGlobalTagMapByTagAndGlobalTag(String globaltag, String tag) throws ConddbServiceException {
		try {
			return globalTagMapRepository.findByGlobalTagAndTagName(globaltag, tag);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag map element " + e.getMessage());
		}
	}

	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Iterable<Tag> findAllTags(PageRequest preq) throws ConddbServiceException {
		try {
			if (preq == null)
				throw new ConddbServiceException("Cannot query full table without pagination");
			return tagRepository.findAll(preq);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find tag list " + e.getMessage());
		}
	}

	/**
	 * @param globaltagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<GlobalTag> getGlobalTagByNameLike(String globaltagname) throws ConddbServiceException {
		try {
			return globalTagRepository.findByNameLike(globaltagname);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name: " + e.getMessage());
		}
	}

	@ProfileExecution
	public GlobalTag getGlobalTagFetchTags(String globaltagname) throws ConddbServiceException {
		try {
			GlobalTag gtag = null;
			/////gtag = this.globalTagRepository.findOne(globaltagname);
			// I have modified the sql query to try to load maps+systemTag in one select
			gtag = this.globalTagRepository.findByNameAndFetchTagsEagerly(globaltagname);
			return gtag;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name and fetch tags: " + e.getMessage());
		}
	}

	@ProfileExecution
	public GlobalTag getGlobalTag(String globaltagname) throws ConddbServiceException {
		try {
			GlobalTag gtag = this.globalTagRepository.findOne(globaltagname);
			return gtag;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name: " + e.getMessage());
		}
	}

	@ProfileExecution
	public List<GlobalTag> getGlobalTagByNameLikeFetchTags(String globaltagnamepattern) throws ConddbServiceException {
		try {
			List<GlobalTag> gtaglist =  null;
			gtaglist = this.globalTagRepository.findByNameLikeAndFetchTagsEagerly(globaltagnamepattern);
			return gtaglist;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name like " + globaltagnamepattern
					+ " and fetch tags: " + e.getMessage());
		}
	}

	@ProfileExecution
	public List<GlobalTag> getGlobalTagByInsertionTimeBetween(Timestamp since, Timestamp until)
			throws ConddbServiceException {
		try {
			List<GlobalTag> gtaglist = this.globalTagRepository.findByInsertionTimeBetween(since, until);
			return gtaglist;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tags by insertion time between " + since + " and "
					+ until + " : " + e.getMessage());
		}
	}

	/**
	 * @param globaltagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<GlobalTagMap> getGlobalTagMapByGlobalTagName(String globaltagname) throws ConddbServiceException {
		try {
			return globalTagMapRepository.findByGlobalTagName(globaltagname);
		} catch (Exception e) {
			throw new ConddbServiceException(
					"Exception in retrieving maps using global tag name " + globaltagname + " : " + e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<GlobalTagMap> getGlobalTagMapByTagName(String tagname) throws ConddbServiceException {
		try {
			return globalTagMapRepository.findByTagName(tagname);
		} catch (Exception e) {
			throw new ConddbServiceException(
					"Exception in retrieving maps using tag name " + tagname + " : " + e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<Tag> getTagByNameLike(String tagname) throws ConddbServiceException {
		try {
			return tagRepository.findByNameLike(tagname);
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public Tag getTag(String tagname) throws ConddbServiceException {
		try {
			return tagRepository.findByName(tagname);
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public Tag getTag(Long id) throws ConddbServiceException {
		try {
			return tagRepository.findOne(id);
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public Tag getTagFetchGlobalTags(String tagname) throws ConddbServiceException {
		try {
			return tagRepository.findByNameAndFetchGlobalTagsEagerly(tagname);
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public Tag getTagFetchGlobalTagsWithLock(String tagname) throws ConddbServiceException {
		try {
			return tagRepository.findByNameAndFetchGlobalTagsWithLock(tagname, GlobalTagStatus.LOCKED.name());
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	public Tag insertTag(Tag entity) throws ConddbServiceException {
		return tagRepository.save(entity);
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	public Tag deleteTag(Tag entity) throws ConddbServiceException {

		try {
			Tag existing = tagRepository.findByName(entity.getName());

			Tag removable = tagRepository.findByNameAndFetchGlobalTagsWithLock(entity.getName(),
					GlobalTagStatus.LOCKED.name());
			if (removable != null && removable.getGlobalTagMaps() != null && removable.getGlobalTagMaps().size() > 0) {
				log.debug("Cannot remove a tag which depends on a locked global tag...");
				throw new ConddbServiceDataIntegrityException(
						"Cannot remova tag " + entity.getName() + " : a parent global tag is locked ");
			}
			tagRepository.delete(existing);
			return existing;
		} catch (ConddbServiceDataIntegrityException e) {
			ConddbServiceException ex = new ConddbServiceException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}

	}

	/**
	 * @param atag
	 * @param gtag
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public GlobalTagMap mapAddTagToGlobalTag(Tag atag, GlobalTag gtag, String record, String label)
			throws ConddbServiceException {
		if (atag == null || gtag == null) {
			throw new ConddbServiceException("Cannot associate...there are null elements");
		}
		GlobalTagMap entity = new GlobalTagMap(gtag, atag, record, label);
		if (gtag.islocked()) {
			log.debug("Global tag lock string is " + gtag.getLockstatus() + ";");
			log.debug("   compared with " + GlobalTagStatus.LOCKED.name() + ";");
			throw new ConddbServiceException("Cannot add tags to a locked global tag..");
		}
		return globalTagMapRepository.save(entity);
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional(rollbackFor = ConddbServiceException.class)
	public GlobalTagMap insertGlobalTagMap(GlobalTagMap entity) throws ConddbServiceException {

		try {
			GlobalTag gtag = globalTagRepository.findOne(entity.getGlobalTagName());
			Tag atag = tagRepository.findByName(entity.getTagName());
			if (gtag == null || atag == null) {
				log.debug("Cannot find elements for association");
				throw new ConddbServiceException("Cannot link tags and global tag...they are not found in the DB");
			}
			entity.setGlobalTag(gtag);
			entity.setSystemTag(atag);
			if (gtag.islocked()) {
				log.debug("Global tag lock string is " + gtag.getLockstatus() + ";");
				log.debug("   compared with " + GlobalTagStatus.LOCKED.name() + ";");
				throw new ConddbServiceException("Cannot link tags to a locked global tag..");
			}
			return globalTagMapRepository.save(entity);
		} catch (Exception e1) {
			throw new ConddbServiceException(e1.getMessage());
		}
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public GlobalTagMap removeGlobalTagMap(GlobalTagMap entity) throws ConddbServiceException {
		GlobalTagMap removable = globalTagMapRepository.findOne(entity.getId());
		globalTagMapRepository.delete(removable);
		return removable;
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional(rollbackFor = ConddbServiceException.class)
	public GlobalTag insertGlobalTag(GlobalTag entity) throws ConddbServiceException {
		try {
			return globalTagRepository.save(entity);
		} catch (javax.validation.ConstraintViolationException e) {
			Set<ConstraintViolation<?>> violations = e.getConstraintViolations();
			StringBuffer buf = new StringBuffer();
			for (ConstraintViolation<?> violates : violations) {
				buf.append(violates.getMessage() + "\n");
			}
			throw new ConddbServiceException("Validation exception: " + buf.toString());
		} catch (Exception e1) {
			throw new ConddbServiceException(e1.getMessage());
		}
	}

}
