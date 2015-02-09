/**
 *
 */
package conddb.web.controllers;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
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
import conddb.dao.controllers.GlobalTagController;
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

	private Logger log = LoggerFactory.getLogger(this.getClass());
//	private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	// TODO: this does not work
    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss:z");

	@Autowired
	private ConddbClientService conddbsvc;
	@Autowired
	private GlobalTagController globalTagController;

	@RequestMapping(value = "/gtagtrace", method = RequestMethod.GET)
	@ResponseBody
	public GlobalTag getGlobalTagTrace(
			@RequestParam(value = "name", defaultValue = "CONDBR2-01") String globaltagname)
			throws Exception {
		this.log.info("CondWebController processing request for getGlobalTagTrace: global tag name ..."
				+ globaltagname);
		GlobalTag gtag = this.globalTagController
				.getGlobalTagFetchTags(globaltagname);
		return gtag;
	}

	@RequestMapping(value = "/gtagliketrace", method = RequestMethod.GET)
	@ResponseBody
	public List<GlobalTag> getGlobalTagLikeTrace(
			@RequestParam(value = "namepattern", defaultValue = "CONDB%") String globaltagnamepattern)
			throws Exception {
		this.log.info("CondWebController processing request for getGlobalTagLikeTrace: global tag name pattern..."
				+ globaltagnamepattern);
		List<GlobalTag> gtaglist = this.globalTagController
				.getGlobalTagByNameLikeFetchTags(globaltagnamepattern);
		return gtaglist;
	}

	@RequestMapping(value = "/gtagbetween", method = RequestMethod.GET)
	@ResponseBody
	public List<GlobalTag> getGlobalTagBetweenTime(
			@RequestParam(value = "sincetime", defaultValue = "20071203101530:GMT") String since,
			@RequestParam(value = "untiltime", defaultValue = "20151203101530:GMT") String until,
			@RequestParam(value = "format", defaultValue = "yyyyMMddHHmmss:z") String format)
			throws Exception {

		this.log.info("CondWebController processing request for getGlobalTagBetweenTime: since until..."
				+ since + " " + until);

		try {
			DateTimeFormatter locformatter = DateTimeFormatter.ofPattern(format);
			
			ZonedDateTime sincedate = ZonedDateTime.parse(since, locformatter);
			ZonedDateTime untildate = ZonedDateTime.parse(until, locformatter);

			this.log.info("CondWebController sending request using dates..."
					+ sincedate + " " + untildate);

			Timestamp dsince = Timestamp.valueOf(sincedate.toLocalDateTime());
			Timestamp duntil = Timestamp.valueOf(untildate.toLocalDateTime());
			
			List<GlobalTag> gtaglist = this.globalTagController
					.getGlobalTagByInsertionTimeBetween(dsince, duntil);
			return gtaglist;
		} catch (Exception e) {
			this.log.error("Error in method getGlobalTagBetweenTime "
					+ e.getMessage());
		}
		return null;
	}

	@RequestMapping(value = "/tag", method = RequestMethod.GET)
	@ResponseBody
	@LogAction(actionPerformed = "getTag")
	public Tag getTag(
			@RequestParam(value = "name", defaultValue = "none") String tagname)
			throws Exception {
		this.log.info(
				"CondWebController processing request for getTag: name ...",
				tagname);
		Tag tag = this.conddbsvc.getTagIovs(tagname);
		return tag;
	}

	@RequestMapping(value = "/iovs", method = RequestMethod.GET)
	@ResponseBody
	public List<Iov> getIovs(
			@RequestParam(value = "name", defaultValue = "none") String tagname)
			throws Exception {
		this.log.info(
				"CondWebController processing request for getIovs: tag name ...",
				tagname);
		List<Iov> iovlist = this.conddbsvc.getIovsForTag(tagname);
		return iovlist;
	}

	@RequestMapping(value = "/iovspayload", method = RequestMethod.GET)
	@ResponseBody
	public List<Iov> getIovsFetchPayload(
			@RequestParam(value = "name", defaultValue = "none") String tagname)
			throws Exception {
		this.log.info(
				"CondWebController processing request for getIovsFetchPayload: tag name ...",
				tagname);
		List<Iov> iovlist = this.conddbsvc.getIovsForTagFetchPayload(tagname);
		return iovlist;
	}

	@ExceptionHandler
	ResponseEntity<?> handleExceptions(Exception ex) {
		ResponseEntity<?> responseEntity = null;
		if (ex instanceof Exception) {
			log.debug(ex.getMessage());
			responseEntity = new ResponseEntity(
					HttpStatus.INTERNAL_SERVER_ERROR);
		}
		return responseEntity;
	}
}
