/**
 * 
 */
package conddb.web.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import conddb.dao.controllers.GlobalTagController;
import conddb.dao.controllers.IovController;
import conddb.dao.svc.ConddbClientService;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
@RestController
public class CondExpertWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagController globalTagController;
	@Autowired
	private IovController iovController;
	@Autowired
	private ConddbClientService clientservice;

	@RequestMapping(value = "/globaltag/add", method = RequestMethod.POST)
	@ResponseBody
	public GlobalTag insertGlobalTag(
			@RequestBody GlobalTag jsonString)
			throws Exception {
		this.log.info("CondExpertWebController processing request for insertGlobalTag ...");
		GlobalTag gtag = this.globalTagController.insertGlobalTag(jsonString);
		return gtag;
	}
	
	@RequestMapping(value = "/globaltag/update/{id}", method = RequestMethod.PUT)
	@ResponseBody
	public GlobalTag updateGlobalTag(
			@PathVariable("id") String id,
			@RequestBody GlobalTag jsonString)
			throws Exception {
		this.log.info("CondExpertWebController processing request for updateGlobalTag ...");
		GlobalTag stored = this.globalTagController.getGlobalTag(id);
		if (stored != null) {
			if (jsonString.getDescription() != null) {
				stored.setDescription(jsonString.getDescription());
			}
			if (jsonString.getLockstatus() != null) {
				stored.setLockstatus(jsonString.getLockstatus());
			}
			if (jsonString.getRelease() != null) {
				stored.setRelease(jsonString.getRelease());
			}
			if (jsonString.getSnapshotTime() != null) {
				stored.setSnapshotTime(jsonString.getSnapshotTime());
			}
			GlobalTag gtag = this.globalTagController.insertGlobalTag(stored);
			return gtag;
		}
		return null;
	}

	@RequestMapping(value = "/tag/add", method = RequestMethod.POST)
	@ResponseBody
	public Tag insertTag(
			@RequestBody Tag jsonString)
			throws Exception {
		this.log.info("CondExpertWebController processing request for insertTag ...");
		Tag tag = this.globalTagController.insertTag(jsonString);
		return tag;
	}

	@RequestMapping(value = "/map/add", method = RequestMethod.POST)
	@ResponseBody
	public GlobalTagMap insertGlobalTagMap(
			@RequestBody GlobalTagMap jsonString)
			throws Exception {
		this.log.info("CondExpertWebController processing request for insertGlobalTagMap ...");
		GlobalTagMap gtagmap = this.globalTagController.insertGlobalTagMap(jsonString);
		return gtagmap;
	}
	
	@RequestMapping(value = "/iov/add", method = RequestMethod.POST)
	@ResponseBody
	public Iov insertIov(
			@RequestBody Iov jsonString)
			throws Exception {
		this.log.info("CondExpertWebController processing request for insertIov using tag..."+jsonString.getTag().getName());
		Iov iov = this.iovController.insertIov(jsonString);
		return iov;
	}

	@RequestMapping(value = "/map/tag2gtag", method = RequestMethod.POST)
	@ResponseBody
	public GlobalTagMap mapTagToGtag(
			@RequestParam(value = "globaltagname", defaultValue = "CONDBR2-01") String globaltagname,
			@RequestParam(value = "tagname", defaultValue = "ATAG-01") String tagname)
			throws Exception {
		this.log.info("CondExpertWebController processing request for mapTagToGtag ..."+globaltagname+" "+tagname);
		GlobalTag gtag = globalTagController.getGlobalTag(globaltagname);
		List<Tag> list = clientservice.getTagOne(tagname);
		Tag atag = list.get(0);
		this.log.info("CondExpertWebController processing request for mapTagToGtag using "+gtag+" "+atag);
		GlobalTagMap gtagmap = this.globalTagController.mapTagToGlobalTag(atag, gtag);
		return gtagmap;
	}

}
