/**
 * 
 */
package conddb.dao.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private GlobalTagRepository gtagRepository;
	@Autowired
	private GlobalTagMapRepository gtagMapRepository;
	@Autowired
	private TagRepository tagRepository;

	public GlobalTag getGlobalTagFetchTags(String globaltagname) throws ConddbServiceException {
		
		GlobalTag gtag = gtagRepository.findByNameAndFetchTagsEagerly(globaltagname);
		log.debug("Found global tag "+gtag);
		
		return gtag;
	}
}
