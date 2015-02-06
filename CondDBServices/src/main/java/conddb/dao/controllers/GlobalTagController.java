/**
 *
 */
package conddb.dao.controllers;

import java.sql.Timestamp;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import conddb.annotations.ProfileExecution;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.GlobalTagMapRepository;
import conddb.dao.repositories.GlobalTagRepository;
import conddb.dao.repositories.TagRepository;
import conddb.data.GlobalTag;

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

	@ProfileExecution
	public GlobalTag getGlobalTagFetchTags(String globaltagname)
			throws ConddbServiceException {

		GlobalTag gtag = this.gtagRepository
				.findByNameAndFetchTagsEagerly(globaltagname);
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

}
