/**
 * 
 */
package conddb.calibration.tools;

import java.util.List;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.controllers.SystemNodeService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.dao.repositories.PayloadRepository;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.Tag;

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
	private PayloadRepository payloadRepository;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;

	@Transactional
	public void commit(Tag atag, Iov iov, Payload payload) throws ConddbServiceException {
		// We now add the file to the HEAD tag by overwriting the IOV with 
		// since = 0
		try {
			PayloadData pylddata = payload.getData();
			Payload pyldstored = payloadRepository.save(payload);
			payloadDataBaseCustom.save(pylddata);
			//TODO: revisit this logic in the commit.
			iov.setPayload(pyldstored);
			iovService.insertIov(iov);
			log.info("Commit new iov in " + atag.getName() + " file name is " + pyldstored.getObjectType());
		} catch (Exception e) {
			throw new ConddbServiceException("Cannot commit the file " + payload.getObjectType());
		}
	}

	public void tagPackage(String tagpattern, GlobalTag globaltag) throws ConddbServiceException {
		List<Tag> taglist = globalTagService.getTagByNameLike(tagpattern);
		if (taglist != null) {
			for (Tag tag : taglist) {
				globalTagService.mapAddTagToGlobalTag(tag,globaltag, null,null);
			}
		}
	}

}
