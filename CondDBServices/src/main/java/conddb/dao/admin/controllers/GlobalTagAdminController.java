package conddb.dao.admin.controllers;

import java.sql.Timestamp;
import java.util.Date;
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
		GlobalTag newgtag = new GlobalTag(destgtag);
		Set<GlobalTagMap> sgtagmap = sgtag.getGlobalTagMaps();
		newgtag.setDescription(sgtag.getDescription() + " - Cloned from "
				+ sourcegtag);
		newgtag.setValidity(sgtag.getValidity());
		newgtag.setRelease(sgtag.getRelease());
		newgtag.setSnapshotTime(sgtag.getSnapshotTime());
		newgtag.setInsertionTime(new Timestamp(new Date().getTime()));
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
		Tag tag = this.tagRepository.findByName(newtag);
		map.setSystemTag(tag);
		this.gtagMapRepository.save(map);
	}
}
