/**
 *
 */
package conddb.dao.controllers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import conddb.annotations.ProfileExecution;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.GlobalTagMapRepository;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.IovRepository;
import conddb.dao.repositories.PayloadRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
public class GlobalTagController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private GlobalTagMapRepository gtagMapRepository;
	@Autowired
	private TagRepository tagRepository;
	@Autowired
	private IovRepository iovRepository;
    @Autowired 
    private PayloadRepository payloadRepository;
	
    public List<GlobalTag> getGlobalTagByNameLike(String globaltagname) throws ConddbServiceException {
    	try {
    		return gtagRepository.findByNameLike(globaltagname);
    	} catch (Exception e) {
    		throw new ConddbServiceException("Cannot find global tag by name: "+e.getMessage());
    	}
    }
    
	@ProfileExecution
	public GlobalTag getGlobalTagFetchTags(String globaltagname)
			throws ConddbServiceException {

		GlobalTag gtag = this.gtagRepository
				.findByNameAndFetchTagsEagerly(globaltagname);
		this.log.debug("Found global tag " + gtag);

		return gtag;
	}

	@ProfileExecution
	public GlobalTag getGlobalTag(String globaltagname)
			throws ConddbServiceException {

		GlobalTag gtag = this.gtagRepository
				.findOne(globaltagname);
		this.log.debug("Found global tag " + gtag);

		return gtag;
	}

	@ProfileExecution
	public List<GlobalTag> getGlobalTagByNameLikeFetchTags(
			String globaltagnamepattern) throws ConddbServiceException {

		List<GlobalTag> gtaglist = this.gtagRepository
				.findByNameLikeAndFetchTagsEagerly(globaltagnamepattern);
		this.log.debug("Found global tag list of size " + gtaglist.size());

		return gtaglist;
	}

	@ProfileExecution
	public List<GlobalTag> getGlobalTagByInsertionTimeBetween(Timestamp since,
			Timestamp until) throws ConddbServiceException {

		List<GlobalTag> gtaglist = this.gtagRepository
				.findByInsertionTimeBetween(since, until);
		this.log.debug("Found global tag list of size " + gtaglist.size());

		return gtaglist;
	}

	@Transactional
	public GlobalTag insertGlobalTag(GlobalTag entity) throws ConddbServiceException {
		return gtagRepository.save(entity);
	}
	
	@ProfileExecution
	public Tag getTag(String tagname)
			throws ConddbServiceException {

		Tag atag = this.tagRepository
				.findByName(tagname);
		this.log.debug("Found tag " + atag);

		return atag;
	}

	@Transactional
	public Tag insertTag(Tag entity) throws ConddbServiceException {
		return tagRepository.save(entity);
	}

	@Transactional
	public GlobalTagMap mapAddTagToGlobalTag(Tag atag, GlobalTag gtag) throws ConddbServiceException {
		if (atag == null || gtag == null) {
			throw new ConddbServiceException("Cannot associate...there are null elements");
		}
		GlobalTagMap entity = new GlobalTagMap(gtag, atag);
		return gtagMapRepository.save(entity);
	}
	

	@Transactional
	public GlobalTagMap insertGlobalTagMap(GlobalTagMap entity) throws ConddbServiceException {
		GlobalTag gtag = gtagRepository.findOne(entity.getGlobalTagName());
		Tag atag = tagRepository.findByName(entity.getTagName());
		entity.setGlobalTag(gtag);
		entity.setSystemTag(atag);
		return gtagMapRepository.save(entity);
	}


}
