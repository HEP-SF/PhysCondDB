/**
 * 
 */
package conddb.web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.CacheControl;
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
import conddb.data.GlobalTag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.GlobalTagResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT)
@Controller
public class GlobalTagExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private GlobalTagAdminController globalTagAdminController;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.GLOBALTAGS)
	public Response create(@Context UriInfo info, GlobalTag globaltag) throws ConddbWebException {
		try {
			GlobalTag saved = globalTagService.insertGlobalTag(globaltag);
			saved.setResId(saved.getName());
			GlobalTagResource resource = (GlobalTagResource) springResourceFactory.getResource("globaltag", info,
					saved);
			return created(resource);
		} catch (Exception e) {
			throw new ConddbWebException("Cannot create entity " + globaltag.getName() + " : " + e.getMessage());
		}
	}

	@Path("/{globaltagname}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateGlobalTag(@Context UriInfo info, @PathParam("globaltagname") String id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
			}
			if (map.containsKey("name")) {
				existing.setName(String.valueOf(map.get("name")));
			}
			if (map.containsKey("description")) {
				existing.setDescription(String.valueOf(map.get("description")));
			}
			existing = globalTagService.insertGlobalTag(existing);
			resp = Response.ok(new GlobalTagResource(info, existing), MediaType.APPLICATION_JSON).build();
		} catch (ConddbServiceException e) {
			resp = Response.status(Response.Status.EXPECTATION_FAILED).build();
			e.printStackTrace();
		}
		return resp;
	}

	@Path("/{globaltagname}")
	@DELETE
	public void deleteGlobalTag(@PathParam("globaltagname") String id) throws ConddbWebException {
		GlobalTag existing;
		try {
			existing = globalTagAdminController.deleteGlobalTag(id);
			if (existing == null) {
				throw new ConddbWebException("Cannot remove id " + id);
			}
		} catch (ConddbServiceException e) {
			throw new ConddbWebException("Cannot remove id " + id + " : " + e.getMessage());
		}

	}

}
