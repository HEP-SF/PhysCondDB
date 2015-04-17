package conddb.dao.admin.controllers;

import java.util.HashSet;
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
	public void cloneGlobalTag(String sourcegtag, String destgtag)
			throws ConddbServiceException {
		GlobalTag sgtag = this.gtagRepository
				.findByNameAndFetchTagsEagerly(sourcegtag);
		this.log.debug("Retrieved globaltag for cloning: " + sgtag
				+ " linked to " + sgtag.getGlobalTagMaps().size() + " tags");
		GlobalTag newgtag = new GlobalTagHandler().cloneObject(sgtag,destgtag);
		Set<GlobalTagMap> sgtagmap = sgtag.getGlobalTagMaps();
		
		this.gtagRepository.save(newgtag);

		Set<GlobalTagMap> newmaps = new HashSet<GlobalTagMap>();
		for (GlobalTagMap globalTagMap : sgtagmap) {
			Tag atag = globalTagMap.getSystemTag();
			GlobalTagMap amap = new GlobalTagMap(newgtag, atag);
			newmaps.add(amap);
		}
		this.gtagMapRepository.save(newmaps);
	}

	@Transactional
	public void updateTagMapping(String sourcegtag, String oldtag, String newtag)
			throws ConddbServiceException {
		GlobalTagMap map = this.gtagMapRepository.findByGlobalTagAndTagName(
				sourcegtag, oldtag);
		GlobalTag gtag = map.getGlobalTag();
		if (gtag.islocked()) {
			throw new ConddbServiceException("Cannot update mapping on locked global tag");
		}
		Tag tag = this.tagRepository.findByName(newtag);
		map.setSystemTag(tag);
		this.gtagMapRepository.save(map);
	}
	
	@Transactional
	public void updateGlobalTagLocking(String sourcegtag, String locking)
			throws ConddbServiceException {

		GlobalTag gtag = gtagRepository.findOne(sourcegtag);
		gtag.setLockstatus(locking);
		this.gtagRepository.save(gtag);
	}

	@Transactional
	public void deleteGlobalTag(String sourcegtag)
			throws ConddbServiceException {
		GlobalTag sgtag = this.gtagRepository
				.findByNameAndFetchTagsEagerly(sourcegtag);
		this.log.debug("Retrieved globaltag for removal operation: " + sgtag
				+ " linked to " + sgtag.getGlobalTagMaps().size() + " tags");
				
		this.gtagRepository.delete(sgtag);
	}

}
