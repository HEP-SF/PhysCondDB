/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
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
import conddb.data.GlobalTagStatus;
import conddb.data.Tag;
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

	@Path(Link.GLOBALTAGS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateGlobalTag(@Context UriInfo info, @PathParam("id") String id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			log.info("Request for updating global tag "+id+" using "+map.size());
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
			}
			//TODO: This should be activated in the final code
			if (existing.islocked()) {
//				throw new ConddbWebException("Cannot update a locked global tag");
				log.info("In principle no update is possible for a locked global tag");
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
				if (lockstatus.equalsIgnoreCase(GlobalTagStatus.LOCKED.name())) {
					existing.lock(true);
					Timestamp now = new Timestamp(Instant.now().toEpochMilli());
					existing.setSnapshotTime(now);
				} else {
					existing.lock(false);
				}
			}
			if (map.containsKey("release")) {
				existing.setRelease(String.valueOf(map.get("release")));
			}			
			if (map.containsKey("validity")) {
				BigDecimal validity = new BigDecimal(String.valueOf(map.get("validity")));
				existing.setValidity(validity);
			}
			if (map.containsKey("snapshotTime")) {
				Timestamp snapshottime = timestampDeserializer.timestampFromString(String.valueOf(map.get("snapshotTime")));
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

	@Path(Link.GLOBALTAGS+"/maps/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response mapTagsToGlobalTag(
			@Context UriInfo info, 
			@PathParam("id") String id, 
			@DefaultValue("addtags") @QueryParam("action") final String action,
			Map map)
			throws ConddbWebException {
		Response resp;
		try {
			String record = (map.containsKey("record")) ? (String) map.get("record") : "none" ;
			String label = (map.containsKey("label")) ? (String) map.get("label") : "none" ;
			String pattern = null;
			if (map.containsKey("name")) {
				pattern = (String)map.get("name");
			} else {
				throw new ConddbWebException("Cannot apply any mapping if name pattern is null");
			}
			log.info("Request to associate tags to a global tag "+id+" using a pattern "+pattern);
			log.info("   - using common values for record and label :"+record+" "+label);
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				throw new ConddbWebException("Resource not found");
			}
			if (existing.islocked()) {
				throw new ConddbWebException("Cannot add tags to locked global tag");
			}
			if (action.equals("addtags")) {
				log.info("action=addtags: add to "+id+" using tag pattern "+pattern);

				List<Tag> taglist = globalTagService.getTagByNameLike(pattern);
				for (Tag tag : taglist) {
					globalTagService.mapAddTagToGlobalTag(tag, existing,record, label);
				}
			} else if (action.equals("merge")) {
				log.info("action=merge: add to "+id+" using global tag name "+pattern);

				// In this case pattern indicates another global tag name
				GlobalTag sourcegtag = globalTagService.getGlobalTagFetchTags(pattern);
				Set<GlobalTagMap> tagmaplist = sourcegtag.getGlobalTagMaps();
				for (GlobalTagMap globalTagMap : tagmaplist) {
					Tag tag = globalTagMap.getSystemTag();
					globalTagService.mapAddTagToGlobalTag(tag, existing,record, label);
				}
			}
			resp = Response.ok().build();
		} catch (ConddbServiceException e) {
			resp = Response.status(Response.Status.EXPECTATION_FAILED).build();
			e.printStackTrace();
		}
		return resp;
	}

	@Path(Link.GLOBALTAGS+"/{id}")
	@DELETE
	public Response deleteGlobalTag(@PathParam("id") String id) throws ConddbWebException {
		Response resp;
		try {
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing.islocked()) {
				throw new ConddbWebException("Cannot delete a locked global tag");
			}
			existing = globalTagAdminController.deleteGlobalTag(id);
			resp = Response.ok(existing).build();

		} catch (ConddbServiceException e) {
			throw new ConddbWebException("Cannot remove id " + id + " : " + e.getMessage());
		}
		return resp;
	}

}
