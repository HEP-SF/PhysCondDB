/**
 * 
 */
package conddb.web.controllers;

import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagController;
import conddb.dao.svc.ConddbClientService;
import conddb.data.GlobalTag;
import conddb.web.exceptions.ConddbWebException;
import io.swagger.annotations.Api;

/**
 * @author aformic
 *
 */
@Path("/userrst")
@Api(value = "/userrst")
@Controller
public class CondRestWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private ConddbClientService conddbsvc;
	@Autowired
	private GlobalTagController globalTagController;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/globaltags/{gtag}")
	public Response getGlobalTag(
			@PathParam("gtag") String globaltagname) throws ConddbWebException {
		this.log.info("CondRestWebController processing request for get global tag name"
				+ globaltagname);
		Response resp = null;
		try {
			if (globaltagname.contains("%")) {
				List<GlobalTag> gtaglist = this.globalTagController.getGlobalTagByNameLike(globaltagname);
				resp = Response.ok(gtaglist).build();
			} else {
				GlobalTag gtag = this.globalTagController.getGlobalTag(globaltagname);
				resp = Response.ok(gtag).build();
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

}
