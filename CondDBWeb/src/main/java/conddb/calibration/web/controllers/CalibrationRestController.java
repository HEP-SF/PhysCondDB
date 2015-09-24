/**
 * 
 */
package conddb.calibration.web.controllers;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.calibration.tools.DirectoryMapperService;
import conddb.dao.controllers.GlobalTagService;
import conddb.data.GlobalTag;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.CALIB)
@Controller
public class CalibrationRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private DirectoryMapperService directoryMapperService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{gtagname}")
	public Response getGlobalTag(
			@PathParam("gtagname") final String globaltagname,
			@DefaultValue("off") @QueryParam("dump") final String dump) throws ConddbWebException {
		this.log.info("CalibrationRestController processing request for getting global tag name"
				+ globaltagname);
		Response resp = null;
		try {
			if (globaltagname.contains("%")) {
				throw new ConddbWebException("Cannot search for generic global tag names");
			} else {
				if (dump.equals("off")) {
					GlobalTag gtag = this.globalTagService.getGlobalTag(globaltagname);
					resp = Response.ok(gtag).build();
				} else if (dump.equals("on")) {
					GlobalTag gtag = this.globalTagService.getGlobalTagFetchTags(globaltagname);
					log.debug("Dumping globaltag "+gtag.getName()+" into file system ");
					directoryMapperService.dumpGlobalTagOnDisk(gtag);
					resp = Response.ok(gtag).build();
				}
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}
}
