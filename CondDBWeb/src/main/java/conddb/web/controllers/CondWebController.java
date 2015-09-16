/**
 *
 */
package conddb.web.controllers;

import io.swagger.annotations.Api;

import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;

import conddb.annotations.LogAction;
import conddb.dao.controllers.GlobalTagService;
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
@Path("/user")
@Api(value = "/user")
@Controller
public class CondWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	// private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

	// TODO: this does not work
	private DateTimeFormatter formatter = DateTimeFormatter
			.ofPattern("yyyyMMddHHmmss:z");

	@Autowired
	private ConddbClientService conddbsvc;
	@Autowired
	private GlobalTagService globalTagController;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/globaltag/{gtag}/{method}")
//	@ResponseBody
	public List<GlobalTag> getGlobalTag(
			@PathParam("gtag") String globaltagname,
			@PathParam("method") String method) throws ConddbWebException {
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

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/globaltag/between")
	public List<GlobalTag> getGlobalTagBetweenTime(
			@QueryParam(value = "sincetime") String since,
			@QueryParam(value = "untiltime") String until,
			@QueryParam(value = "format") String format)
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

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/tag/{tag}/{method}")
	@LogAction(actionPerformed = "getTag")
	public List<Tag> getTag(@PathParam("tag") String tagname,
			@PathParam("method") String method) throws ConddbWebException {
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
				String help = " use : /like, /one, /iovs, /backtrace instead !";
				throw new ConddbWebException("Cannot find method " + method+" "+help);
			}
			return taglist;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/iovs/{tag}/{method}")
	public List<Iov> getIovs(@PathParam("tag") String tagname,
			@PathParam("method") String method) throws ConddbWebException {
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
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/payload/{hash}")
	public Payload getPayload(@PathParam("hash") String hash)
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
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/payloaddata/{hash}")
	public PayloadData getPayloadBlob(@PathParam("hash") String hash)
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

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/payloadfilter/sizegt/{size}")
	public List<Payload> getPayloadFiltered(@PathParam("size") Integer size)
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

//	@ExceptionHandler
//	ResponseEntity<?> handleExceptions(Exception ex) {
//		ResponseEntity<?> responseEntity = null;
//		if (ex instanceof Exception) {
//			log.debug(ex.getMessage());
//			responseEntity = new ResponseEntity(
//					HttpStatus.INTERNAL_SERVER_ERROR);
//		}
//		return responseEntity;
//	}
}
