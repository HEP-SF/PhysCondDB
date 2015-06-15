/**
 *
 */
package conddb.web.controllers;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
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
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.Tag;
import conddb.web.exceptions.ConddbWebException;

/**
 * @author formica
 *
 */
@RestController
public class CondWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	// private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	// TODO: this does not work
	private DateTimeFormatter formatter = DateTimeFormatter
			.ofPattern("yyyyMMddHHmmss:z");

	@Autowired
	private ConddbClientService conddbsvc;
	@Autowired
	private GlobalTagController globalTagController;

	@RequestMapping(value = "/globaltag/{gtag}/{method}", method = RequestMethod.GET)
	@ResponseBody
	public List<GlobalTag> getGlobalTag(
			@PathVariable("gtag") String globaltagname,
			@PathVariable("method") String method) throws ConddbWebException {
		this.log.info("CondWebController processing request for getGlobalTagLikeTrace: global tag name pattern..."
				+ globaltagname);
		List<GlobalTag> gtaglist = new ArrayList<GlobalTag>();
		try {
			if (method.equals("like")) {
				gtaglist = this.conddbsvc.getGlobalTagLike(globaltagname);
			} else if (method.equals("one")) {
				gtaglist = this.conddbsvc.getGlobalTagOne(globaltagname);
			} else if (method.equals("trace")) {
				gtaglist = this.conddbsvc
						.getGlobalTagTrace(globaltagname);
			} else {
				String help = " use : /like, /one, /trace instead !";
				throw new ConddbWebException("Cannot find method " + method+" "+help);
			}
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
		return gtaglist;
	}

	@RequestMapping(value = "/globaltag/between", method = RequestMethod.GET)
	@ResponseBody
	public List<GlobalTag> getGlobalTagBetweenTime(
			@RequestParam(value = "sincetime", defaultValue = "20071203101530:GMT") String since,
			@RequestParam(value = "untiltime", defaultValue = "20151203101530:GMT") String until,
			@RequestParam(value = "format", defaultValue = "yyyyMMddHHmmss:z") String format)
			throws ConddbWebException {

		this.log.info("CondWebController processing request for getGlobalTagBetweenTime: since until..."
				+ since + " " + until);

		try {
			DateTimeFormatter locformatter = DateTimeFormatter
					.ofPattern(format);

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
			throw new ConddbWebException(e.getMessage());
		}
	}

	@RequestMapping(value = "/tag/{tag}/{method}", method = RequestMethod.GET)
	@ResponseBody
	@LogAction(actionPerformed = "getTag")
	public List<Tag> getTag(@PathVariable("tag") String tagname,
			@PathVariable("method") String method) throws ConddbWebException {
		this.log.info("CondWebController processing request for getTag: name ..."
				+ tagname);
		List<Tag> taglist = new ArrayList<Tag>();
		try {

			if (method.equals("like")) {
				taglist = this.conddbsvc.getTagLike(tagname);
			} else if (method.equals("one")) {
				taglist = this.conddbsvc.getTagOne(tagname);
			} else if (method.equals("iovs")) {
				taglist = this.conddbsvc.getTagIovs(tagname);
			} else if (method.equals("backtrace")) {
				taglist = this.conddbsvc.getTagBackTrace(tagname);
			} else {
				String help = " use : /like, /one, /iovs instead !";
				throw new ConddbWebException("Cannot find method " + method+" "+help);
			}
			return taglist;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@RequestMapping(value = "/iovs/{tag}/{method}", method = RequestMethod.GET)
	@ResponseBody
	public List<Iov> getIovs(@PathVariable("tag") String tagname,
			@PathVariable("method") String method) throws ConddbWebException {
		this.log.info(
				"CondWebController processing request for getIovs: tag name ...",
				tagname);
		List<Iov> iovlist = new ArrayList<Iov>();
		try {
			if (method.equals("list")) {
				iovlist = this.conddbsvc.getIovsForTag(tagname);
			} else if (method.equals("listpayload")) {
				iovlist = this.conddbsvc.getIovsForTagFetchPayload(tagname);
			} else {
				String help = " use : /list, /listpayload instead !";
				throw new ConddbWebException("Cannot find method " + method+" "+help);
			}
			return iovlist;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@RequestMapping(value = "/payload/{hash}", method = RequestMethod.GET)
	@ResponseBody
	public Payload getPayload(@PathVariable("hash") String hash)
			throws ConddbWebException {
		this.log.info(
				"CondWebController processing request for getPayload: hash ..."+hash);
		try {
			Payload pyld = this.conddbsvc.getPayload(hash);
			return pyld;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@RequestMapping(value = "/payloaddata/{hash}", method = RequestMethod.GET)
	@ResponseBody
	public PayloadData getPayloadBlob(@PathVariable("hash") String hash)
			throws ConddbWebException {
		this.log.info(
				"CondWebController processing request for getPayloadBlob: hash ..."+hash);
		try {
			PayloadData pyld = this.conddbsvc.getPayloadData(hash);
			return pyld;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@RequestMapping(value = "/payloadfilter/sizegt/{size}", method = RequestMethod.GET)
	@ResponseBody
	public List<Payload> getPayloadFiltered(@PathVariable("size") Integer size)
			throws ConddbWebException {
		this.log.info(
				"CondWebController processing request for getPayloadSizeGt: size ...",
				size);
		try {
			List<Payload> pyld = this.conddbsvc.getPayloadSizeGt(size);
			return pyld;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
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
