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
import conddb.data.exceptions.ConversionException;
import conddb.utils.json.serializers.TimestampDeserializer;
import conddb.web.exceptions.ConddbWebException;
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
	@Autowired 
	private TimestampDeserializer timestampDeserializer;

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

	@Path(Link.GLOBALTAGS+"/{globaltagname}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateGlobalTag(@Context UriInfo info, @PathParam("globaltagname") String id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			log.info("Request for updating global tag "+id+" using "+map.size());
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
			}
			if (map.containsKey("name")) {
				List<GlobalTagMap> maplist = globalTagService.getGlobalTagMapByGlobalTagName(id);
				if (maplist != null && maplist.size()>0 || existing.islocked()) {
					resp = Response.status(Response.Status.FORBIDDEN).build(); 
					return resp;
				}
				existing.setName(String.valueOf(map.get("name")));
			}
			if (map.containsKey("description")) {
				existing.setDescription(String.valueOf(map.get("description")));
			}
			if (map.containsKey("lockstatus")) {
				String lockstatus = String.valueOf(map.get("lockstatus"));
				if (lockstatus.equalsIgnoreCase("locked"))
				existing.lock();
			}
			if (map.containsKey("release")) {
				existing.setRelease(String.valueOf(map.get("release")));
			}			
			if (map.containsKey("validity")) {
				BigDecimal validity = new BigDecimal(String.valueOf(map.get("validity")));
				existing.setValidity(validity);
			}
			if (map.containsKey("snapshottime")) {
				Timestamp snapshottime = timestampDeserializer.timestampFromString(String.valueOf(map.get("validity")));
				existing.setSnapshotTime(snapshottime);
			}
			existing = globalTagService.insertGlobalTag(existing);
			resp = Response.ok(new GlobalTagResource(info, existing), MediaType.APPLICATION_JSON).build();
		} catch (ConddbServiceException e) {
			resp = Response.status(Response.Status.EXPECTATION_FAILED).build();
			e.printStackTrace();
		} catch (ConversionException e) {
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			e.printStackTrace();
		}
		return resp;
	}

	@Path(Link.GLOBALTAGS+"/{globaltagname}")
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
