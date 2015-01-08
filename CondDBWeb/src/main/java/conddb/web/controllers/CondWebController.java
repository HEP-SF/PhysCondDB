/**
 * 
 */
package conddb.web.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import conddb.annotations.LogAction;
import conddb.dao.svc.ConddbClientService;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
@RestController
public class CondWebController {

	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private ConddbClientService conddbsvc;

	@RequestMapping(value = "/gtagtrace", method = RequestMethod.GET)
	@ResponseBody
	public GlobalTag getGlobalTag(
			@RequestParam(value = "name", defaultValue = "CONDB%") String gtagpattern) throws Exception {
		log.info(
				"CoolController processing request for getGlobalTag: global ...",
				gtagpattern);
		GlobalTag gtag = conddbsvc.getGlobalTagTrace(gtagpattern);
		return gtag;
	}

	@RequestMapping(value = "/tag", method = RequestMethod.GET)
	@ResponseBody
	@LogAction(actionPerformed="getTag")
	public Tag getTag(
			@RequestParam(value = "name", defaultValue = "none") String tagname) throws Exception {
		log.info(
				"CondWebController processing request for getTag: name ...",
				tagname);
		Tag tag = conddbsvc.getTagIovs(tagname);
		return tag;
	}

	@RequestMapping(value = "/iovs", method = RequestMethod.GET)
	@ResponseBody
	public List<Iov> getIovs(
			@RequestParam(value = "name", defaultValue = "none") String tagname) throws Exception {
		log.info(
				"CondWebController processing request for getIovs: tag name ...",
				tagname);
		List<Iov> iovlist = conddbsvc.getIovsForTag(tagname);
		return iovlist;
	}

	@RequestMapping(value = "/iovspayload", method = RequestMethod.GET)
	@ResponseBody
	public List<Iov> getIovsFetchPayload(
			@RequestParam(value = "name", defaultValue = "none") String tagname) throws Exception {
		log.info(
				"CondWebController processing request for getIovsFetchPayload: tag name ...",
				tagname);
		List<Iov> iovlist = conddbsvc.getIovsForTagFetchPayload(tagname);
		return iovlist;
	}

	@ExceptionHandler
	ResponseEntity handleExceptions(Exception ex) {
		ResponseEntity responseEntity = null;
		if (ex instanceof Exception) {
			responseEntity = new ResponseEntity(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return responseEntity;
	}
}
