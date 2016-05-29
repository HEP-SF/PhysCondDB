/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.generic.GenericPojoResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT)
@Controller
@Api(value=Link.EXPERT)
public class SystemDescriptionExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private GlobalTagService globalTagService;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.SYSTEMS)
	@ApiOperation(value = "Create a System Description entity.",
    notes = "Input data are in json, and should match all needed fields for a new System Description.\n"
    +"System Description has to be unique for the node fullpath name. The tagNameRoot is unique as well.",
    response=SystemDescription.class)
	public Response createSystemDescription(@Context UriInfo info, 
			@ApiParam(value = "A json entry corresponding to SystemDescription", required = true) SystemDescription systemdescription) throws ConddbWebException {
		try {
			log.debug("Controller will create system "+systemdescription);
			SystemDescription saved = systemNodeService.insertSystemDescription(systemdescription);
			saved.setResId(saved.getId().toString());
			log.debug("Controller creating resource for inserted entity "+saved);
			GenericPojoResource<SystemDescription> resource = new GenericPojoResource<>(info, saved, 0, null);
			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error creating system description resource using "+systemdescription.toString();
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);		}
	}

	@Path(Link.SYSTEMS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update a System Description.",
    notes = "Input data are in json, and should match the fields that can be updated: nodeDescription and groupSize (used in pagination queries).",
    response=SystemDescription.class)
	public Response updateSystemDescription(@Context UriInfo info, 
			@ApiParam(value = "system: id of the system description to be updated", required = true) 
			@PathParam("id") String id, Map<?,?> map)
			throws ConddbWebException {
		try {
			log.info("Request for updating system "+id+" using "+map.size());
			SystemDescription existing = systemNodeService.getSystemNodesByTagname(id);
			if (existing == null) {
				String msg = "Error updating system description resource: id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			
			if (map.containsKey("nodeDescription")) {
				existing.setNodeDescription(String.valueOf(map.get("nodeDescription")));
			}
			if (map.containsKey("groupSize")) {
				existing.setGroupSize(new BigDecimal(String.valueOf(map.get("groupSize"))));
			}
			
			existing = systemNodeService.insertSystemDescription(existing);
			GenericPojoResource<SystemDescription> resource = new GenericPojoResource<>(info, existing, 0, null);
			return created(resource);
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error updating system description resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Path(Link.SYSTEMS+"/{id}")
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Delete a system description.",
    notes = "It should be used one system at the time. This method is meant for administration purposes.",
    response=SystemDescription.class)
	public Response deleteSystemDescription(	
			@Context UriInfo info,
		@ApiParam(value = "id: id of the system description to be deleted", required = true) 
		@PathParam("id") Long id) throws ConddbWebException {

		try {
			log.info("Request for deleting system "+id);
			SystemDescription existing = systemNodeService.getSystemDescription(id);
			if (existing == null) {
				String msg = "Error removing system description resource: id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			List<Tag> tags = this.globalTagService.getTagByNameLike(existing.getTagNameRoot() + "%");
			if (tags != null && tags.size()>0) {
				String msg = "Error removing system description resource: id "+id+" because tags exist containing the tagNameRoot !";
				throw buildException(msg, msg, Response.Status.CONFLICT);				
			}
			
			SystemDescription removed = systemNodeService.delete(id);
			GenericPojoResource<SystemDescription> resource = new GenericPojoResource<>(info, removed, 0, null);
			return ok(resource);
			
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error removing system description resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

}
