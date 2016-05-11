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

import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
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
@Api(value = Link.EXPERT)
public class TagExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.TAGS)
	@ApiOperation(value = "Create a new Tag.",
    notes = "Creation requires the full needed information in json.",
    response = Tag.class)
	public Response createTag(@Context UriInfo info, Tag tag) throws ConddbWebException {
		try {
			Tag saved = globalTagService.insertTag(tag);
			GenericPojoResource<Tag> resource = new GenericPojoResource<>(info, saved, 0, null);
			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error creating tag resource using "+tag.toString();
			log.debug("Got exception "+e.getMessage());
			Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
			throw buildException(msg+" "+e.getMessage(), msg+" "+e.getMessage(), status);
		}
	}

	@Path(Link.TAGS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update an existing Tag.",
    notes = "Requires in the input json the needed fields to be updated. Cannot update a locked tag.",
    response = Tag.class)
	public Response updateTag(@Context UriInfo info, 
			@ApiParam(value = "id: the name of the tag to be updated", required = true)
			@PathParam("id") String id, Map<?, ?> map)
			throws ConddbWebException {
		new ConddbWebException();
		try {
			log.info("Request for updating tag "+id+" using "+map.size());
			Tag existing = globalTagService.getTag(id);
			if (existing == null) {
				String msg = "Error updating tag resource: id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			if (map.containsKey("name")) {
				List<GlobalTagMap> maplist = globalTagService.getGlobalTagMapByTagName(id);
				if (maplist != null && maplist.size()>0) {
					String msg = "Error updating tag resource: <name> cannot be modified!";
					throw buildException(msg, msg, Response.Status.NOT_MODIFIED);
				}
				existing.setName(String.valueOf(map.get("name")));
			}
			if (map.containsKey("description")) {
				existing.setDescription(String.valueOf(map.get("description")));
			}
			if (map.containsKey("synchronization")) {
				existing.setSynchronization(String.valueOf(map.get("synchronization")));
			}
			if (map.containsKey("objectType")) {
				existing.setObjectType(String.valueOf(map.get("objectType")));
			}
			if (map.containsKey("timeType")) {
				existing.setTimeType(String.valueOf(map.get("timeType")));
			}
			if (map.containsKey("lastValidatedTime")) {
				BigDecimal value = new BigDecimal(String.valueOf(map.get("lastValidatedTime")));
				existing.setLastValidatedTime(value);
			}
			if (map.containsKey("endOfValidity")) {
				BigDecimal value = new BigDecimal(String.valueOf(map.get("endOfValidity")));
				existing.setEndOfValidity(value);
			}
			existing = globalTagService.insertTag(existing);
			GenericPojoResource<Tag> resource = new GenericPojoResource<>(info, existing, 0, null);
			return ok(resource);
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error updating tag resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Path(Link.TAGS+"/{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@DELETE
	@ApiOperation(value = "Delete a tag.",
    notes = "It should be used on one tag at the time. This method is meant for administration purposes.",
    response=Tag.class)
	public Response deleteTag(@Context UriInfo info, 
			@ApiParam(value = "id: the name of the tag to be updated", required = true)
			@PathParam("id") String id) throws ConddbWebException {
		try {
			Tag entity = globalTagService.getTag(id);
			if (entity == null) {
				String msg = "Error in removing a tag resource: resource not found with id "+id;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			Tag entitywithmaps = globalTagService.getTagFetchGlobalTagsWithLock(entity.getName());
			if (entitywithmaps != null && entitywithmaps.getGlobalTagMaps() != null && entitywithmaps.getGlobalTagMaps().size()>0) {
				String msg = "Error in removing a tag resource: there is a global tag locked associated to it "+id;
				throw buildException(msg, msg, Response.Status.PRECONDITION_FAILED);				
			}
			Tag removed = null;
			removed = globalTagService.deleteTag(entity);
			GenericPojoResource<Tag> resource = new GenericPojoResource<>(info, removed, 0, null);
			return ok(resource);
			
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error removing a tag resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);	
		}

	}
}
