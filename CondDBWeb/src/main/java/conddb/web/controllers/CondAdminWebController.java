package conddb.web.controllers;

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

	@POST
	@Path("/cloneGlobalTag")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String cloneGlobalTag(
			@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "destgtag") String destgtag)
			throws Exception {
		this.log.info("CondAdminWebController processing request for cloning "
				+ sourcegtag + " into " + destgtag);
		this.globalTagAdminController.cloneGlobalTag(sourcegtag, destgtag);
		return "Success";
	}

	@POST
	@Path("/updateGlobalTagMap")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String updateGlobalTagMap(
			@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "oldtag") String oldtag,
			@QueryParam(value = "newtag") String newtag)
			throws Exception {
		this.log.info("CondAdminWebController processing request for updating mapping "
				+ sourcegtag + " from " + oldtag + " to " + newtag);
		this.globalTagAdminController.updateTagMapping(sourcegtag, oldtag,
				newtag);
		return "Success";
	}

	@POST
	@Path("/updateGlobalTagLockStatus")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String updateGlobalTagLockStatus(
			@QueryParam(value = "sourcegtag") String sourcegtag,
			@QueryParam(value = "lock") String locking)
			throws Exception {
		this.log.info("CondAdminWebController processing request for updating locking status "
				+ sourcegtag + " using lock " + locking );
		this.globalTagAdminController.updateGlobalTagLocking(sourcegtag, locking);
		return "Success";
	}
	
	@DELETE
	@Path("/deleteGlobalTag")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String deleteGlobalTag(
			@QueryParam(value = "sourcegtag") String sourcegtag)
			throws Exception {
		this.log.info("CondAdminWebController processing request for removing full global tag "
				+ sourcegtag );
		this.globalTagAdminController.deleteGlobalTag(sourcegtag);
		return "Success";
	}
	
	@DELETE
	@Path("/deleteTagLike")
	@Produces({ MediaType.TEXT_HTML })
	@ResponseBody
	public String deleteTagLike(
			@QueryParam(value = "sourcetag") String sourcetag)
			throws Exception {
		this.log.info("CondAdminWebController processing request for removing full tags like "
				+ sourcetag );
		this.globalTagAdminController.deleteTagLike(sourcetag);
		return "Success";
	}
}
