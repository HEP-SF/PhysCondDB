package conddb.web.admin.controllers;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import conddb.data.GlobalTag;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagExpertService;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.web.exceptions.ConddbWebException;
import io.swagger.annotations.Api;

/**
 * @author formica
 *
 */
@Controller
@Path("/admin")
@Api(value = "/admin")
public class CondAdminWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagExpertService globalTagExpertService;
	@Autowired
	private GlobalTagService globalTagService;

	@POST
	@Path("/globaltag/clone")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ResponseBody
	public GlobalTag cloneGlobalTag(@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "destgtag") String destgtag) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for cloning " + sourcegtag + " into " + destgtag);
			this.globalTagExpertService.cloneGlobalTag(sourcegtag, destgtag);
			GlobalTag cloned = this.globalTagService.getGlobalTag(destgtag);

			return cloned;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	@POST
	@Path("/tag/clone")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ResponseBody
	public Tag cloneTag(@QueryParam(value = "sourcetag") String sourcetag,
			@QueryParam(value = "desttag") String desttag,
			@QueryParam(value = "from") String from,
			@QueryParam(value = "to") String to,
			@QueryParam(value = "timetype") String timetype
			) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for cloning " + sourcetag + " into " + desttag);
			this.globalTagExpertService.cloneTag(sourcetag, desttag,from,to,timetype);
			Tag cloned = this.globalTagService.getTag(desttag);
			return cloned;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}
	@POST
	@Path("/map/update")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String updateGlobalTagMap(@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "oldtag") String oldtag, @QueryParam(value = "newtag") String newtag)
					throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for updating mapping " + sourcegtag + " from "
					+ oldtag + " to " + newtag);
			this.globalTagExpertService.updateTagMapping(sourcegtag, oldtag, newtag);
			return "Success";
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}
}
