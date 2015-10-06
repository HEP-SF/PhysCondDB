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
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.GlobalTagStatus;
import conddb.data.Tag;
import conddb.data.converters.GlobalTagStatusConverter;

/**
 * @author formica
 *
 */
public class GlobalTagService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private GlobalTagMapRepository gtagMapRepository;
	@Autowired
	private TagRepository tagRepository;
	
	/**
	 * @return
	 * @throws ConddbServiceException
	 */
	public Iterable<GlobalTag> findAllGlobalTags() throws ConddbServiceException {
		try {
			return gtagRepository.findAll();
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
			return gtagMapRepository.findAll(preq);
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
			return gtagMapRepository.findOne(id);
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
			return gtagRepository.findByNameLike(globaltagname);
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name: " + e.getMessage());
		}
	}

	@ProfileExecution
	public GlobalTag getGlobalTagFetchTags(String globaltagname) throws ConddbServiceException {
		try {
			GlobalTag gtag = this.gtagRepository.findByNameAndFetchTagsEagerly(globaltagname);
			this.log.debug("Found global tag " + gtag);
			return gtag;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name and fetch tags: " + e.getMessage());
		}
	}

	@ProfileExecution
	public GlobalTag getGlobalTag(String globaltagname) throws ConddbServiceException {
		try {
			GlobalTag gtag = this.gtagRepository.findOne(globaltagname);
			this.log.debug("Found global tag " + gtag);
			return gtag;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name: " + e.getMessage());
		}
	}

	@ProfileExecution
	public List<GlobalTag> getGlobalTagByNameLikeFetchTags(String globaltagnamepattern) throws ConddbServiceException {

		try {
			List<GlobalTag> gtaglist = this.gtagRepository.findByNameLikeAndFetchTagsEagerly(globaltagnamepattern);
			this.log.debug("Found global tag list of size " + gtaglist.size());
			return gtaglist;
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot find global tag by name like "+ globaltagnamepattern +" and fetch tags: " + e.getMessage());
		}
	}

	@ProfileExecution
	public List<GlobalTag> getGlobalTagByInsertionTimeBetween(Timestamp since, Timestamp until)
			throws ConddbServiceException {

		List<GlobalTag> gtaglist = this.gtagRepository.findByInsertionTimeBetween(since, until);
		this.log.debug("Found global tag list of size " + gtaglist.size());

		return gtaglist;
	}
	
	/**
	 * @param globaltagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<GlobalTagMap> getGlobalTagMapByGlobalTagName(String globaltagname) throws ConddbServiceException {
		return gtagMapRepository.findByGlobalTagName(globaltagname);
	}
	/**
	 * @param tagname
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<GlobalTagMap> getGlobalTagMapByTagName(String tagname) throws ConddbServiceException {
		return gtagMapRepository.findByTagName(tagname);
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
	@Transactional
	public Tag insertTag(Tag entity) throws ConddbServiceException {
		return tagRepository.save(entity);
	}
	
	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public Tag deleteTag(Tag entity) throws ConddbServiceException {
		Tag existing = tagRepository.findByName(entity.getName());
		Tag removable = tagRepository.findByNameAndFetchGlobalTagsWithLock(entity.getName(), GlobalTagStatus.LOCKED.name());
		if (removable != null && removable.getGlobalTagMaps() != null && removable.getGlobalTagMaps().size()>0) {
			log.debug("Cannot remove a tag which depends on a locked global tag...");
			throw new ConddbServiceException("Cannot remova tag "+entity.getName()+" : a parent global tag is locked ");
		}
		tagRepository.delete(existing);
		return existing;
	}

	/**
	 * @param atag
	 * @param gtag
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public GlobalTagMap mapAddTagToGlobalTag(Tag atag, GlobalTag gtag, String record, String label) throws ConddbServiceException {
		if (atag == null || gtag == null) {
			throw new ConddbServiceException("Cannot associate...there are null elements");
		}
		GlobalTagMap entity = new GlobalTagMap(gtag, atag, record, label);
		if (gtag.islocked()) {
			log.debug("Global tag lock string is "+gtag.getLockstatus()+";");
			log.debug("   compared with "+GlobalTagStatus.LOCKED.name()+";");
			throw new ConddbServiceException("Cannot add tags to a locked global tag..");
		}
		return gtagMapRepository.save(entity);
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public GlobalTagMap insertGlobalTagMap(GlobalTagMap entity) throws ConddbServiceException {
		GlobalTag gtag = gtagRepository.findOne(entity.getGlobalTagName());
		Tag atag = tagRepository.findByName(entity.getTagName());
		entity.setGlobalTag(gtag);
		entity.setSystemTag(atag);
		if (gtag.islocked()) {
			log.debug("Global tag lock string is "+gtag.getLockstatus()+";");
			log.debug("   compared with "+GlobalTagStatus.LOCKED.name()+";");
			throw new ConddbServiceException("Cannot link tags to a locked global tag..");
		}
		return gtagMapRepository.save(entity);
	}
	
	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public GlobalTagMap removeGlobalTagMap(GlobalTagMap entity) throws ConddbServiceException {
		GlobalTagMap removable = gtagMapRepository.findOne(entity.getId());
		gtagMapRepository.delete(removable);
		return removable;
	}

	/**
	 * @param entity
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional
	public GlobalTag insertGlobalTag(GlobalTag entity) throws ConddbServiceException {
		return gtagRepository.save(entity);
	}

}
