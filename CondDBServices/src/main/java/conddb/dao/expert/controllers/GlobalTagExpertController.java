package conddb.dao.expert.controllers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.GlobalTagMapRepository;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.IovRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.data.handler.GlobalTagHandler;
import conddb.data.handler.IovHandler;
import conddb.data.handler.TagHandler;

public class GlobalTagExpertController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private GlobalTagMapRepository gtagMapRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;

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
		GlobalTag stored = this.gtagRepository.findOne(destgtag);
		
		Set<GlobalTagMap> newmaps = new HashSet<GlobalTagMap>();
		for (GlobalTagMap globalTagMap : sgtagmap) {
			Tag atag = globalTagMap.getSystemTag();
			GlobalTagMap amap = new GlobalTagMap(stored, atag);
			newmaps.add(amap);
		}
		this.gtagMapRepository.save(newmaps);
	}

	@Transactional
	public void cloneTag(String sourcetag, String desttag, String from, String to, String timetype)
			throws ConddbServiceException {
		
		this.log.debug("Timetype parameter is ignored for the moment : " + timetype);
//		BigDecimal since = new BigDecimal(from);
//		BigDecimal until = new BigDecimal(to);
		Tag atag = this.tagRepository.findByName(sourcetag);
		this.log.debug("Retrieved tag for cloning: " + atag
				+ " linked to " + atag.getIovs().size() + " iovs");
		
		Tag newtag = new TagHandler().cloneObject(atag,desttag);
		this.tagRepository.save(newtag);
		Tag stored = this.tagRepository.findByName(desttag);
		
//		List<Iov> iovlist = this.iovRepository.findByTagName(sourcetag);
//		Set<Iov> newiovs = new HashSet<Iov>();	
//		for (Iov iov : iovlist) {
//			log.debug(" set tag "+stored.getName()+" for iov "+iov.getSince());
//			Iov newiov = new IovHandler().cloneObject(iov, null);
//			newiov.setTag(stored);
//			newiovs.add(newiov);
//		}
//		log.debug("Storing list of iovs "+newiovs.size());
//		this.iovRepository.save(newiovs);
		
		Page<Iov> iovs = this.iovRepository.findAllByTagName(sourcetag,new PageRequest(0,20));
		float nrOfPages = iovs.getTotalPages();
		log.debug("Retrieved list of pages : "+nrOfPages);
		
		for (int apage=0; apage<nrOfPages; apage++) {
			Set<Iov> newiovs = new HashSet<Iov>();		
			List<Iov> pageiovs = iovs.getContent();
			log.debug(" page : "+apage+" contains "+pageiovs.size()+" iovs");

			for (Iov iov : pageiovs) {
				log.debug(" set tag "+stored.getName()+" for iov "+iov.getSince());
				Iov newiov = new IovHandler().cloneObject(iov, null);
				newiov.setTag(stored);
				newiovs.add(newiov);
			}
			log.debug("Storing list of iovs "+newiovs.size());
			this.iovRepository.save(newiovs);
		}
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

}
