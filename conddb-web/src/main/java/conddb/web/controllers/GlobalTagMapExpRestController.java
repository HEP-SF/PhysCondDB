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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagAdminService;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
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
public class GlobalTagMapExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private GlobalTagAdminService globalTagAdminService;
	@Autowired
	private SpringResourceFactory springResourceFactory;
	
	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.GLOBALTAGMAPS)
	@ApiOperation(value = "Create a GlobalTagMap.",
    notes = "Input data are in json, and should match all needed fields for a new global tag to tag association.\n"
    +"These key fields are: globaltagname, tagname, record and label.",
    response=GlobalTagMap.class)
	public Response createGlobalTagMap(@Context UriInfo info, @ApiParam(value = "a json entry corresponding to mapping parameters.", required = true) Map map) throws ConddbWebException {

		try {
			log.info("Request for creating a new global tag mapping using data map of size "+map.size());
			GlobalTagMap globaltagmap = createGlobalTagMap(map);
			
			GlobalTagMap saved = globalTagService.insertGlobalTagMap(globaltagmap);
			GenericPojoResource<GlobalTagMap> resource = (GenericPojoResource) springResourceFactory.getGenericResource(info, saved, 0, null);
			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error creating association resource using "+map.toString();
			throw buildException(msg+" "+e.getMessage(), msg+" "+e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Path(Link.GLOBALTAGMAPS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Update a GlobalTagMap association.",
    notes = "Input data are in json, and should match the fields that can be updated: record and label.",
    response=GlobalTagMap.class)
	public Response updateGlobalTagMap(@Context UriInfo info, 
			@ApiParam(value = "id: id of the globaltagmap to be updated", required = true) 
			@PathParam("id") Long id, Map map)
			throws ConddbWebException {
		try {
			log.info("Request for updating global tag mapping "+id+" using "+map.size());
			GlobalTagMap existing = globalTagService.getGlobalTagMap(id);
			if (existing == null) {
				String msg = "Error updating association resource: id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			if (map.containsKey("label")) {
				existing.setLabel(String.valueOf(map.get("label")));
			}
			if (map.containsKey("record")) {
				existing.setRecord(String.valueOf(map.get("record")));
			}
			GenericPojoResource<GlobalTagMap> resource = (GenericPojoResource) springResourceFactory.getGenericResource(info, existing, 0, null);
			return ok(resource);
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error updating association resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Path(Link.GLOBALTAGMAPS+"/{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@DELETE
	@ApiOperation(value = "Delete a globaltag mapping.",
    notes = "It should be used one global tag at the time. This method is meant for administration purposes.",
    response=GlobalTagMap.class)
	public Response deleteGlobalTagMap(@Context UriInfo info, 
			@ApiParam(value = "id: id of the association to be deleted", required = true) 
			@PathParam("id") Long id) throws ConddbWebException {
		try {
			GlobalTagMap existing = globalTagService.getGlobalTagMapFetchChildren(id);
			if (existing == null) {
				String msg = "Error removing association resource: id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			GlobalTag gtag = existing.getGlobalTag();
			if (gtag.islocked()) {
				log.debug("Cannot remove a mapping for a locked global tag: "+gtag.getName());
				String msg = "Error removing an association resource: global tag is locked !";
				throw buildException(msg, msg, Response.Status.PRECONDITION_FAILED);				
			}
			existing = globalTagAdminService.deleteGlobalTagMap(id);
			GenericPojoResource<GlobalTagMap> resource = (GenericPojoResource) springResourceFactory.getGenericResource(info, existing, 0, null);

			return ok(resource);
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using a ConddbService exception..."+e.getMessage());
			String msg = "Error removing an association resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}

	}
	
	protected GlobalTagMap createGlobalTagMap(Map map) throws ConddbServiceException {
		GlobalTagMap globaltagmap = new GlobalTagMap();
		if (map == null) {
			throw new ConddbServiceException("Cannot create global tag mapping from null map");
		}
		if (map.containsKey("globaltagname")) {
			GlobalTag globaltag = globalTagService.getGlobalTag((String.valueOf(map.get("globaltagname"))));
			if (globaltag == null) {
				throw new ConddbServiceException("Global tag "+String.valueOf(map.get("globaltagname"))+" does not exists ");
			}
			globaltagmap.setGlobalTag(globaltag);
		}
		if (map.containsKey("tagname")) {
			Tag tag = globalTagService.getTag((String.valueOf(map.get("tagname"))));
			if (tag == null) {
				throw new ConddbServiceException("Tag "+String.valueOf(map.get("tagname"))+" does not exists ");
			}
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
