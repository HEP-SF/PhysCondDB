package conddb.svc.dao.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.GlobalTagMapRepository;
import conddb.svc.dao.repositories.GlobalTagRepository;
import conddb.svc.dao.repositories.TagRepository;

@Service
public class GlobalTagAdminService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository globalTagRepository;
	@Autowired
	private GlobalTagMapRepository globalTagMapRepository;
	@Autowired
	private TagRepository tagRepository;


	@Transactional
	public GlobalTag deleteGlobalTag(String sourcegtag)
			throws ConddbServiceException {
		GlobalTag sgtag = this.globalTagRepository
				.findOne(sourcegtag);
		int ntags = 0;
		if (sgtag != null && sgtag.getGlobalTagMaps() != null) {
			ntags = sgtag.getGlobalTagMaps().size();
		} else {
			log.info("Cannot find global tag corresponding to "+sourcegtag);
			throw new ConddbServiceException("Null global tag retrieved");
		}
		this.log.debug("Retrieved globaltag for removal operation: " + sgtag
				+ " linked to " + ntags + " tags: cascade deleting ALL tags and iovs ");
		this.globalTagRepository.delete(sgtag);
		return sgtag;
	}


	@Transactional
	public void deleteGlobalTagMap(String globaltag, String tag)
			throws ConddbServiceException {
		GlobalTagMap map = this.globalTagMapRepository
				.findByGlobalTagAndTagName(globaltag, tag);
		if (map != null) {
			this.log.debug("Retrieved map for removal operation: " + map.toString());
			this.globalTagMapRepository.delete(map);
		} else {
			log.info("Cannot find mapping corresponding to "+globaltag+" and "+tag);
			throw new ConddbServiceException("Null mapping element retrieved");
		}
	}
	
	@Transactional
	public GlobalTagMap deleteGlobalTagMap(Long id)
			throws ConddbServiceException {
		GlobalTagMap map = this.globalTagMapRepository
				.findOne(id);
		if (map != null) {
			this.log.debug("Retrieved map for removal operation: " + map.toString());
			this.globalTagMapRepository.delete(map);
		} else {
			log.info("Cannot find mapping corresponding to "+id);
			throw new ConddbServiceException("Null mapping element retrieved");
		}
		return map;
	}
	
	@Transactional
	public void deleteTagLike(String tag)
			throws ConddbServiceException {
		List<Tag> stags = this.tagRepository
				.findByNameLike(tag);
		int ntags = 0;
		if (stags != null) {
			ntags = stags.size();
		} else {
			log.info("Cannot find tags corresponding to "+tag);
			throw new ConddbServiceException("Null tag list retrieved");
		}
		log.debug("Removing list of "+ntags+" tags....");
		int counter = 0;
		for (Tag atag : stags) {
			this.log.debug("Retrieved tag for removal operation: " + atag);
			this.tagRepository.delete(atag);
			counter++;
		}
		log.info("Remove "+counter+" tags");
	}
	
	@Transactional
	public Tag deleteTag(String tag)
			throws ConddbServiceException {
		Tag entity = this.tagRepository
				.findByName(tag);
		if (entity != null) {
			log.debug("Removing entity"+entity.getName());
			this.tagRepository.delete(entity);
		} else {
			log.info("Cannot find "+tag);
			throw new ConddbServiceException("Null tag retrieved");
		}
		return entity;
	}
}
