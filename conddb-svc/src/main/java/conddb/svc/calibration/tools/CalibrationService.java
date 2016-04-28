/**
 * 
 */
package conddb.svc.calibration.tools;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.GlobalTagMapRepository;
import conddb.svc.dao.repositories.PayloadRepository;
import conddb.svc.dao.repositories.TagRepository;
import conddb.svc.dao.specifications.GenericSpecBuilder;

/**
 * @author aformic
 *
 */
@Service
public class CalibrationService {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private GlobalTagMapRepository globalTagMapRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private PayloadRepository payloadRepository;

	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;

	/**
	 * @param atag
	 * @param iov
	 * @param payload
	 * @throws ConddbServiceException
	 */
	@Transactional
	public void commit(Tag atag, Iov iov, Payload payload) throws ConddbServiceException {
		// We now add the file to the HEAD tag by overwriting the IOV with
		// since = 0
		try {
			PayloadData pylddata = payload.getData();
			Payload pyldstored = payloadRepository.save(payload);
			payloadDataBaseCustom.save(pylddata);
			// TODO: revisit this logic in the commit.
			iov.setPayload(pyldstored);
			iovService.insertIov(iov);
			log.info("Commit new iov in " + atag.getName() + " file name is " + pyldstored.getObjectType());
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot commit the file " + payload.getObjectType());
		}
	}

	/**
	 * @param tagpattern
	 * @param globaltag
	 * @throws ConddbServiceException
	 */
	public void tagPackage(String tagpattern, GlobalTag globaltag) throws ConddbServiceException {
		List<Tag> taglist = globalTagService.getTagByNameLike(tagpattern);
		if (taglist != null) {
			for (Tag tag : taglist) {
				globalTagService.mapAddTagToGlobalTag(tag, globaltag, null, null);
			}
		}
	}

	/**
	 * @param entity
	 * @param systemlist
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional(rollbackOn = ConddbServiceException.class)
	public GlobalTag createMapFromSystemTags(GlobalTag entity, List<SystemDescription> systemlist)
			throws ConddbServiceException {
		try {
			Set<GlobalTagMap> maplist = new HashSet<GlobalTagMap>();
			for (SystemDescription systemDescription : systemlist) {
				String tagnameroot = systemDescription.getTagNameRoot();
				Tag systemtag = tagRepository.findByName(tagnameroot + Tag.DEFAULT_TAG_EXTENSION);
				if (systemtag == null) {
					throw new ConddbServiceException("Cannot associate global tag : one of the system tags is null "
							+ tagnameroot + Tag.DEFAULT_TAG_EXTENSION);
				}
				log.debug("Use tagnameroot and systemtag to generate a map entry: " + tagnameroot + " "
						+ systemtag.getName());
				GlobalTagMap globaltagmap = new GlobalTagMap(entity, systemtag, systemDescription.getSchemaName(),
						tagnameroot);
				globaltagmap = globalTagMapRepository.save(globaltagmap);
				maplist.add(globaltagmap);
			}
			entity.setGlobalTagMaps(maplist);
			return entity;
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param entity
	 * @param systemlist
	 * @return
	 * @throws ConddbServiceException
	 */
	@Transactional(rollbackOn = ConddbServiceException.class)
	public GlobalTag createMapAsgToPackage(GlobalTag asgentity, String globaltagpackagename)
			throws ConddbServiceException {
		try {
			GlobalTag packagegtag = globalTagService.getGlobalTagFetchTags(globaltagpackagename);

			Set<GlobalTagMap> tagmaplist = packagegtag.getGlobalTagMaps();
			for (GlobalTagMap globalTagMap : tagmaplist) {
				Tag tag = globalTagMap.getSystemTag();
				String newlabel = packagegtag.getName();
				globalTagService.mapAddTagToGlobalTag(tag, asgentity, globalTagMap.getRecord(), newlabel);
			}
			GlobalTag asgmodified = globalTagService.getGlobalTagFetchTags(asgentity.getName());
			return asgmodified;
		} catch (Exception e) {
			throw new ConddbServiceException(e.getMessage());
		}
	}

	/**
	 * @param key
	 * @param operation
	 * @param value
	 * @return
	 * @throws ConddbServiceException
	 */
	public List<SystemDescription> filterSystems(String key, String operation, Object value)
			throws ConddbServiceException {
		try {
			GenericSpecBuilder<SystemDescription> builder = new GenericSpecBuilder<>();
			builder.with(key, operation, value);
			Specification<SystemDescription> spec = builder.build();
			List<SystemDescription> systemlist = systemNodeService.getSystemNodeRepository().findAll(spec);
			return systemlist;
		} catch (Exception e) {
			throw new ConddbServiceException("Error in filtering systems : " + e.getMessage());
		}
	}

}
