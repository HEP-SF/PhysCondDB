/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.SystemNodeService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.ErrorMessage;
import conddb.data.GlobalTagMap;
import conddb.data.SystemDescription;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.SystemDescriptionResource;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT)
@Controller
public class SystemDescriptionExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.SYSTEMS)
	public Response create(@Context UriInfo info, SystemDescription systemdescription) throws ConddbWebException {
		try {
			log.debug("Controller will create system "+systemdescription);
			SystemDescription saved = systemNodeService.insertSystemDescription(systemdescription);
			saved.setResId(saved.getId().toString());
			log.debug("Controller creating resource for inserted entity "+saved);
			SystemDescriptionResource resource = (SystemDescriptionResource) springResourceFactory.getResource("system", info,
					saved);
			return created(resource);
		} catch (Exception e) {
			log.debug("Controller could not insert entity....");
			ConddbWebException ex = new ConddbWebException();
			ErrorMessage error = new ErrorMessage("Cannot insert system "+systemdescription);
			error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
			error.setInternalMessage(e.getMessage());
			ex.setStatus(Response.Status.NOT_MODIFIED);
			ex.setErrMessage(error);
			throw ex;	
		}
	}

	@Path(Link.SYSTEMS+"/{system}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateSystemDescription(@Context UriInfo info, @PathParam("system") String id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			log.info("Request for updating system "+id+" using "+map.size());
			SystemDescription existing = systemNodeService.getSystemNodesByTagname(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
			}
			
			if (map.containsKey("nodeDescription")) {
				existing.setNodeDescription(String.valueOf(map.get("nodeDescription")));
			}
			if (map.containsKey("groupSize")) {
				existing.setGroupSize(new BigDecimal(String.valueOf(map.get("groupSize"))));
			}
			
			existing = systemNodeService.insertSystemDescription(existing);
			resp = Response.ok(new SystemDescriptionResource(info, existing), MediaType.APPLICATION_JSON).build();
			return resp;
		} catch (ConddbServiceException e) {
			log.debug("Controller could not update entity....");
			ConddbWebException ex = new ConddbWebException();
			ErrorMessage error = new ErrorMessage("Cannot update system "+id);
			error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
			error.setInternalMessage(e.getMessage());
			ex.setStatus(Response.Status.NOT_MODIFIED);
			ex.setErrMessage(error);
			throw ex;	
		}
	}

	@Path(Link.SYSTEMS+"/{id}")
	@DELETE
	public void deleteSystemDescription(@PathParam("id") Long id) throws ConddbWebException {
		SystemDescription existing;
		try {
			existing = systemNodeService.delete(id);
			if (existing == null) {
				throw new ConddbWebException("Cannot remove id " + id);
			}
		} catch (ConddbServiceException e) {
			throw new ConddbWebException("Cannot remove id " + id + " : " + e.getMessage());
		}

	}

}
