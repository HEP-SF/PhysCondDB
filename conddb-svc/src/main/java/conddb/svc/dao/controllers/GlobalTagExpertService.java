package conddb.svc.dao.controllers;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.data.exceptions.ConversionException;
import conddb.data.handler.GlobalTagHandler;
import conddb.data.handler.IovHandler;
import conddb.data.handler.TagHandler;
import conddb.data.utils.converters.CondTimeTypes;
import conddb.data.utils.converters.IovConversionHandler;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.GlobalTagMapRepository;
import conddb.svc.dao.repositories.GlobalTagRepository;
import conddb.svc.dao.repositories.IovRepository;
import conddb.svc.dao.repositories.TagRepository;

@Service
public class GlobalTagExpertService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository globalTagRepository;
	@Autowired
	private GlobalTagMapRepository globalTagMapRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
	@Autowired
	private IovConversionHandler iovConversionHandler;

	/**
	 * This method creates a new global tag by cloning an old one.
	 * All tags associated to the original global tag will be associated to the clone as well.
	 * @param sourcegtag
	 * @param destgtag
	 * @throws ConddbServiceException
	 */
	@Transactional
	public void cloneGlobalTag(String sourcegtag, String destgtag)
			throws ConddbServiceException {
		GlobalTag sgtag = this.globalTagRepository
				.findByNameAndFetchTagsEagerly(sourcegtag);
		this.log.debug("Retrieved globaltag for cloning: " + sgtag
				+ " linked to " + sgtag.getGlobalTagMaps().size() + " tags");
		GlobalTag newgtag = new GlobalTagHandler().cloneObject(sgtag,destgtag);
		Set<GlobalTagMap> sgtagmap = sgtag.getGlobalTagMaps();
		
		this.globalTagRepository.save(newgtag);
		GlobalTag stored = this.globalTagRepository.findByName(destgtag);
		
//		Set<GlobalTagMap> newmaps = new HashSet<GlobalTagMap>();
		for (GlobalTagMap globalTagMap : sgtagmap) {
			Tag atag = globalTagMap.getSystemTag();
			GlobalTagMap amap = new GlobalTagMap(stored, atag, globalTagMap.getRecord(),globalTagMap.getLabel());
			this.log.debug("Save new map entry : " + amap.toString());
			this.globalTagMapRepository.save(amap);
//			newmaps.add(amap);
		}
//		this.globalTagMapRepository.save(newmaps);
	}

	/**
	 * Perform a clone of a source tag. The new tag will have a different name, but it will contain
	 * all IOVs which were associated to the original tag. To use carefully.
	 * This method should also give the possibility to select a range for IOV cloning.
	 * @param sourcetag
	 * @param desttag
	 * @param from
	 * @param to
	 * @param timetype
	 * @throws ConddbServiceException
	 * @throws ConversionException 
	 */
	@Transactional
	public void cloneTag(String sourcetag, String desttag, String from, String to, String timetype)
			throws ConddbServiceException, ConversionException {
		
		this.log.debug("Timetype parameter is ignored for the moment : " + timetype);
		CondTimeTypes time = CondTimeTypes.valueOf(timetype);
		BigDecimal since = iovConversionHandler.convert(time, CondTimeTypes.TIME, from);
		BigDecimal until = null;
		if (to.equalsIgnoreCase("INF")) {
			until = new BigDecimal(Iov.MAX_TIME);
		} else {
			until = iovConversionHandler.convert(time, CondTimeTypes.TIME, to);
		}
		Tag atag = this.tagRepository.findByName(sourcetag);
		this.log.debug("Retrieved tag for cloning: " + atag
				+ " linked to " + atag.getIovs().size() + " iovs");
		
		Tag newtag = new TagHandler().cloneObject(atag,desttag);
		this.tagRepository.save(newtag);
		Tag stored = this.tagRepository.findByName(desttag);
				
		Page<Iov> iovs = this.iovRepository.findByRangeAndTag(atag.getId(), since, until, new PageRequest(0,200));
//		Page<Iov> iovs = this.iovRepository.findAllByTagName(sourcetag,new PageRequest(0,20));
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
		GlobalTagMap map = this.globalTagMapRepository.findByGlobalTagAndTagName(
				sourcegtag, oldtag);
		GlobalTag gtag = map.getGlobalTag();
		if (gtag.islocked()) {
			throw new ConddbServiceException("Cannot update mapping on locked global tag");
		}
		Tag tag = this.tagRepository.findByName(newtag);
		map.setSystemTag(tag);
		this.globalTagMapRepository.save(map);
	}
	
	@Transactional
	public void updateGlobalTagLocking(String sourcegtag, String locking)
			throws ConddbServiceException {

		GlobalTag gtag = globalTagRepository.findByName(sourcegtag);
		gtag.setLockstatus(locking);
		this.globalTagRepository.save(gtag);
	}

}
