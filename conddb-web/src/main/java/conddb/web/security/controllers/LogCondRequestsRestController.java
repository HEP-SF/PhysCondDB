/**
 * 
 */
package conddb.web.security.controllers;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.data.security.LogCondRequests;
import conddb.svc.security.dao.repositories.LogCondRequestsBaseRepository;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author aformic
 *
 */
@Path(Link.MONITOR+Link.PATH_SEPARATOR+"log")
@Controller
@Api(value = Link.MONITOR+Link.PATH_SEPARATOR+"log")
public class LogCondRequestsRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private LogCondRequestsBaseRepository logCondRequestRepository;
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{httpmethod}")
	@ApiOperation(value = "Finds requests by httpmethod",
    notes = "Use GET, POST, PUT, ...",
    response = LogCondRequests.class,
    responseContainer = "List")
	public Response getLogRequestsByHttpMethod(
			@Context UriInfo info,
			@ApiParam(value = "name pattern for the search", required = true)
			@PathParam("httpmethod") final String httpmeth) throws ConddbWebException {
		this.log.info("LogCondRequestsRestController processing request to get logging requests by httpmethod "
				+ httpmeth);

		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(600);
			
			List<LogCondRequests> entitycoll = null;
			Instant ago = Instant.now().minus(10, ChronoUnit.DAYS);
			Timestamp daysago = new Timestamp(ago.toEpochMilli());	
			log.debug("Search for a httpmethod "+httpmeth+" and associated tags...");
			entitycoll = this.logCondRequestRepository.findByStartTimeGtAndMethod(daysago, httpmeth);
			if (entitycoll == null) {
				String msg = "Entity LogCondRequest not found for time >  "+daysago.toString();
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}	
			log.debug("Retrieved entity list : "+entitycoll.size());
			CollectionResource collres = new CollectionResource(info, Link.MONITOR+Link.PATH_SEPARATOR+"log", entitycoll);			
				return created(collres);
			
		} catch (Exception e) {
			String msg = "Error retrieving globaltag resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
