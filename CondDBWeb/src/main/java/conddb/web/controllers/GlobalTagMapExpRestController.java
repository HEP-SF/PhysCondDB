/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.data.exceptions.ConversionException;
import conddb.utils.json.serializers.TimestampDeserializer;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.GlobalTagMapResource;
import conddb.web.resources.GlobalTagResource;
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
	@Autowired 
	private TimestampDeserializer timestampDeserializer;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.GLOBALTAGMAPS)
	public Response create(@Context UriInfo info, Map map) throws ConddbWebException {
		try {
			GlobalTagMap globaltagmap = createGlobalTagMap(map);
			GlobalTagMap saved = globalTagService.insertGlobalTagMap(globaltagmap);
			String resid = new String(saved.getId().toString());
			saved.setResId(resid);
			GlobalTagMapResource resource = (GlobalTagMapResource) springResourceFactory.getResource("globaltagmap", info,
					saved);
			return created(resource);
		} catch (Exception e) {
			throw new ConddbWebException("Cannot create entity from map: " + e.getMessage());
		}
	}

	@Path(Link.GLOBALTAGMAPS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateGlobalTagMap(@Context UriInfo info, @PathParam("id") Long id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			log.info("Request for updating global tag "+id+" using "+map.size());
			GlobalTagMap existing = globalTagService.getGlobalTagMap(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
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
			resp = Response.status(Response.Status.EXPECTATION_FAILED).build();
			e.printStackTrace();
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
				throw new ConddbWebException("Cannot remove id " + id);
			}
		} catch (ConddbServiceException e) {
			throw new ConddbWebException("Cannot remove id " + id + " : " + e.getMessage());
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
