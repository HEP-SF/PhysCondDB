package conddb.dao.admin.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.GlobalTagMapRepository;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.data.handler.GlobalTagHandler;

public class GlobalTagAdminController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private GlobalTagMapRepository gtagMapRepository;
	@Autowired
	private TagRepository tagRepository;


	@Transactional
	public GlobalTag deleteGlobalTag(String sourcegtag)
			throws ConddbServiceException {
		GlobalTag sgtag = this.gtagRepository
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
		this.gtagRepository.delete(sgtag);
		return sgtag;
	}


	@Transactional
	public void deleteGlobalTagMap(String globaltag, String tag)
			throws ConddbServiceException {
		GlobalTagMap map = this.gtagMapRepository
				.findByGlobalTagAndTagName(globaltag, tag);
		if (map != null) {
			this.log.debug("Retrieved map for removal operation: " + map.toString());
			this.gtagMapRepository.delete(map);
		} else {
			log.info("Cannot find mapping corresponding to "+globaltag+" and "+tag);
			throw new ConddbServiceException("Null mapping element retrieved");
		}
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

}
