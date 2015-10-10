/**
 * 
 */
package conddb.web.controllers;

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
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.GlobalTagMapResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT)
@Controller
public class GlobalTagMapExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private GlobalTagAdminController globalTagAdminController;
	@Autowired
	private SpringResourceFactory springResourceFactory;
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.GLOBALTAGMAPS)
	public Response create(@Context UriInfo info, Map map) throws ConddbWebException {

		ConddbWebException ex = new ConddbWebException();
		try {
			GlobalTagMap globaltagmap = createGlobalTagMap(map);
			GlobalTagMap saved = globalTagService.insertGlobalTagMap(globaltagmap);
			String resid = new String(saved.getId().toString());
			saved.setResId(resid);
			GlobalTagMapResource resource = (GlobalTagMapResource) springResourceFactory.getResource("globaltagmap", info,
					saved);
			return created(resource);
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error creating association resource ");
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot creating an association resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
	}

	@Path(Link.GLOBALTAGMAPS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateGlobalTagMap(@Context UriInfo info, @PathParam("id") Long id, Map map)
			throws ConddbWebException {
		Response resp;
		ConddbWebException ex = new ConddbWebException();
		try {
			log.info("Request for updating global tag "+id+" using "+map.size());
			GlobalTagMap existing = globalTagService.getGlobalTagMap(id);
			if (existing == null) {
				ErrorMessage error = new ErrorMessage("Error updating association resource "+id);
				error.setCode(Response.Status.NOT_FOUND.getStatusCode());
				error.setInternalMessage("Cannot updating an association resource because it was not found in the DB");
				ex.setStatus(Response.Status.NOT_FOUND);
				ex.setErrMessage(error);
				throw ex;
			}
			if (map.containsKey("label")) {
				existing.setLabel(String.valueOf(map.get("label")));
			}
			if (map.containsKey("record")) {
				existing.setRecord(String.valueOf(map.get("record")));
			}
			existing = globalTagService.insertGlobalTagMap(existing);
			resp = Response.ok(new GlobalTagMapResource(info, existing), MediaType.APPLICATION_JSON).build();
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error updating association resource "+id);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot updating an association resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
		return resp;
	}

	@Path(Link.GLOBALTAGMAPS+"/{id}")
	@DELETE
	public void deleteGlobalTagMap(@PathParam("id") Long id) throws ConddbWebException {
		GlobalTagMap existing;
		try {
			existing = globalTagAdminController.deleteGlobalTagMap(id);
			if (existing == null) {
				ConddbWebException ex = new ConddbWebException();
				ErrorMessage error = new ErrorMessage("Error removing association resource "+id);
				error.setCode(Response.Status.NOT_FOUND.getStatusCode());
				error.setInternalMessage("Cannot remove an association resource because id was not found");
				ex.setStatus(Response.Status.NOT_FOUND);
				ex.setErrMessage(error);
				throw ex;
			}
		} catch (ConddbServiceException e) {
			ConddbWebException ex = new ConddbWebException();
			ErrorMessage error = new ErrorMessage("Error removing association resource "+id);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot remove an association resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}

	}
	
	protected GlobalTagMap createGlobalTagMap(Map map) throws ConddbServiceException {
		GlobalTagMap globaltagmap = new GlobalTagMap();
		if (map == null) {
			throw new ConddbServiceException("Cannot create global tag mapping from null map");
		}
		if (map.containsKey("globaltagname")) {
			GlobalTag globaltag = globalTagService.getGlobalTag((String.valueOf(map.get("globaltagname"))));
			globaltagmap.setGlobalTag(globaltag);
		}
		if (map.containsKey("tagname")) {
			Tag tag = globalTagService.getTag((String.valueOf(map.get("tagname"))));
			globaltagmap.setSystemTag(tag);
		}
		if (map.containsKey("record")) {
			globaltagmap.setRecord(String.valueOf(map.get("record")));
		}
		if (map.containsKey("label")) {
			globaltagmap.setLabel(String.valueOf(map.get("label")));

		}
		return globaltagmap;
	}

}
