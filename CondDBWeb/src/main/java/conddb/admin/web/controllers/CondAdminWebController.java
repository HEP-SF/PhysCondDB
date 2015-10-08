package conddb.admin.web.controllers;

import io.swagger.annotations.Api;

import javax.ws.rs.DELETE;
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

import conddb.dao.admin.controllers.GlobalTagAdminController;
import conddb.dao.controllers.GlobalTagService;
import conddb.dao.expert.controllers.GlobalTagExpertController;
import conddb.data.GlobalTag;
import conddb.data.Tag;
import conddb.web.exceptions.ConddbWebException;

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
	private GlobalTagAdminController globalTagAdminController;
	@Autowired
	private GlobalTagExpertController globalTagExpertController;
	@Autowired
	private GlobalTagService globalTagController;

	@POST
	@Path("/globaltag/clone")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ResponseBody
	public GlobalTag cloneGlobalTag(@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "destgtag") String destgtag) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for cloning " + sourcegtag + " into " + destgtag);
			this.globalTagExpertController.cloneGlobalTag(sourcegtag, destgtag);
			GlobalTag cloned = this.globalTagController.getGlobalTag(destgtag);

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
			this.globalTagExpertController.cloneTag(sourcetag, desttag,from,to,timetype);
			Tag cloned = this.globalTagController.getTag(desttag);
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
			this.globalTagExpertController.updateTagMapping(sourcegtag, oldtag, newtag);
			return "Success";
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	@DELETE
	@Path("/map/delete")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String deleteTagFromGlobalTagMap(@QueryParam(value = "globaltag") String sourcegtag,
			@QueryParam(value = "tag") String tag) throws ConddbWebException {
		try {
			this.log.info(
					"CondAdminWebController processing request for removing mapping " + tag + " from " + sourcegtag);
			this.globalTagAdminController.deleteGlobalTagMap(sourcegtag, tag);
			return "Success";
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	@POST
	@Path("/globaltag/lock/update")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String updateGlobalTagLockStatus(@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "lock") String locking) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for updating locking status " + sourcegtag
					+ " using lock " + locking);
			this.globalTagExpertController.updateGlobalTagLocking(sourcegtag, locking);
			return "Success";
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	@DELETE
	@Path("/globaltag/delete")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String deleteGlobalTag(@QueryParam(value = "sourcegtag") String sourcegtag) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for removing full global tag " + sourcegtag);
			this.globalTagAdminController.deleteGlobalTag(sourcegtag);
			return "Success";
		} catch (Exception e) {
			throw new ConddbWebException("Cannot delete global tag "+sourcegtag,e);
		}
	}

	@DELETE
	@Path("/tag/delete")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String deleteTagLike(@QueryParam(value = "sourcetag") String sourcetag) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for removing full tags like " + sourcetag);
			this.globalTagAdminController.deleteTagLike(sourcetag);
			return "Success";
		} catch (Exception e) {
			throw new ConddbWebException("Cannot delete tag "+sourcetag,e);
		}
	}
}
