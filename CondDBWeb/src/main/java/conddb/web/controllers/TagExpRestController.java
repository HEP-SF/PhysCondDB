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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.dao.admin.controllers.GlobalTagAdminController;
import conddb.dao.controllers.GlobalTagService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.ErrorMessage;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.TagResource;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT)
@Controller
public class TagExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private GlobalTagAdminController globalTagAdminController;
	@Autowired
	private SpringResourceFactory springResourceFactory;
	
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.TAGS)
	public Response create(@Context UriInfo info, Tag tag) throws ConddbWebException {
		ConddbWebException ex = new ConddbWebException();
		try {
			Tag saved = globalTagService.insertTag(tag);
			saved.setResId(saved.getName());
			TagResource resource = (TagResource) springResourceFactory.getResource("tag", info,
					saved);
			return created(resource);
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error creating tag resource "+tag.getName());
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot create a tag resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
	}

	@Path(Link.TAGS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTag(@Context UriInfo info, @PathParam("id") String id, Map map)
			throws ConddbWebException {
		Response resp;
		ConddbWebException ex = new ConddbWebException();
		try {
			log.info("Request for updating tag "+id+" using "+map.size());
			Tag existing = globalTagService.getTag(id);
			if (existing == null) {
				ErrorMessage error = new ErrorMessage("Error updating not existing Tag resource "+id);
				error.setCode(Response.Status.NOT_FOUND.getStatusCode());
				error.setInternalMessage("Cannot update tag because is not found in DB");
				ex.setStatus(Response.Status.NOT_FOUND);
				ex.setErrMessage(error);
				throw ex;
			}
			if (map.containsKey("name")) {
				List<GlobalTagMap> maplist = globalTagService.getGlobalTagMapByTagName(id);
				if (maplist != null && maplist.size()>0) {
					ErrorMessage error = new ErrorMessage("Error updating tag name for Tag resource "+id);
					error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
					error.setInternalMessage("Cannot update tag name because it is already associated ");
					ex.setStatus(Response.Status.NOT_MODIFIED);
					ex.setErrMessage(error);
					throw ex;
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
			resp = Response.ok(new TagResource(info, existing), MediaType.APPLICATION_JSON).build();
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error updating tag resource "+id);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot update a tag resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
		return resp;
	}

	@Path(Link.TAGS+"/{id}")
	@DELETE
	public void deleteTag(@PathParam("id") Long id) throws ConddbWebException {
		Tag existing;
		ConddbWebException ex = new ConddbWebException();
		try {
			Tag entity = globalTagService.getTag(id);
			existing = globalTagService.deleteTag(entity);
			if (existing == null) {
				ErrorMessage error = new ErrorMessage("Error removing not existing Tag resource "+id);
				error.setCode(Response.Status.NOT_FOUND.getStatusCode());
				error.setInternalMessage("Cannot remove tag because is not found in DB");
				ex.setStatus(Response.Status.NOT_FOUND);
				ex.setErrMessage(error);
				throw ex;
			}
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error removing tag resource "+id);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot remove a tag resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}

	}

}
