package conddb.web.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import conddb.dao.admin.controllers.GlobalTagAdminController;

@RestController
public class CondAdminWebController {
	
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private GlobalTagAdminController globalTagAdminController;

	@RequestMapping(value = "/cloneGlobalTag", method = RequestMethod.POST)
	@ResponseBody
	public String cloneGlobalTag(
			@RequestParam(value = "sourcegtag", defaultValue = "CONDBR2-TEST-01") String sourcegtag,
			@RequestParam(value = "destgtag", defaultValue = "CONDBR2-TEST-02") String destgtag
			) throws Exception {
		log.info(
				"CondAdminWebController processing request for cloning "+sourcegtag+" into "+destgtag);
		globalTagAdminController.cloneGlobalTag(sourcegtag, destgtag);
		return "Success";
	}

	@RequestMapping(value = "/updateGlobalTagMap", method = RequestMethod.POST)
	@ResponseBody
	public String cloneGlobalTag(
			@RequestParam(value = "sourcegtag", defaultValue = "CONDBR2-TEST-01") String sourcegtag,
			@RequestParam(value = "oldtag", defaultValue = "NONE") String oldtag,
			@RequestParam(value = "newtag", defaultValue = "NONE") String newtag
			) throws Exception {
		log.info(
				"CondAdminWebController processing request for updating mapping "+sourcegtag+" from "+oldtag+" to "+newtag);
		globalTagAdminController.updateTagMapping(sourcegtag, oldtag, newtag);
		return "Success";
	}

}
