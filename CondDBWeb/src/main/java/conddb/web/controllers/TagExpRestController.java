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
		try {
			Tag saved = globalTagService.insertTag(tag);
			saved.setResId(saved.getName());
			TagResource resource = (TagResource) springResourceFactory.getResource("tag", info,
					saved);
			return created(resource);
		} catch (Exception e) {
			throw new ConddbWebException("Cannot create entity " + tag.getName() + " : " + e.getMessage());
		}
	}

	@Path(Link.TAGS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateTag(@Context UriInfo info, @PathParam("id") String id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			log.info("Request for updating tag "+id+" using "+map.size());
			Tag existing = globalTagService.getTag(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
			}
			if (map.containsKey("name")) {
				List<GlobalTagMap> maplist = globalTagService.getGlobalTagMapByTagName(id);
				if (maplist != null && maplist.size()>0) {
					resp = Response.status(Response.Status.FORBIDDEN).build(); 
					return resp;
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
			resp = Response.status(Response.Status.EXPECTATION_FAILED).build();
			e.printStackTrace();
		}
		return resp;
	}

	@Path(Link.TAGS+"/{id}")
	@DELETE
	public void deleteTag(@PathParam("id") String id) throws ConddbWebException {
		Tag existing;
		try {
			existing = globalTagAdminController.deleteTag(id);
			if (existing == null) {
				throw new ConddbWebException("Cannot remove id " + id);
			}
		} catch (ConddbServiceException e) {
			throw new ConddbWebException("Cannot remove id " + id + " : " + e.getMessage());
		}

	}

}
