/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
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
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.GlobalTagResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
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
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Path(Link.GLOBALTAGS)
	@ApiOperation(value = "Create a GlobalTag.",
    notes = "Input data are in json, and should match all needed fields for a new global tag.",
    response=GlobalTag.class)
	public Response createGlobalTag(@Context UriInfo info,
			GlobalTag globaltag) throws ConddbWebException {
		try {
//			GlobalTag globaltag = new GlobalTag();
			GlobalTag saved = globalTagService.insertGlobalTag(globaltag);
			saved.setResId(saved.getName());
			GlobalTagResource resource = (GlobalTagResource) springResourceFactory.getResource("globaltag", info,
					saved);
			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error crerating globaltag resource using "+globaltag.toString();
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Path(Link.GLOBALTAGS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Update a GlobalTag.",
		    notes = "Input data are in json, and should match all  fields to be updated.",
		    response=GlobalTag.class)
	public Response updateGlobalTag(@Context UriInfo info, 
			@ApiParam(value = "id: id of the globaltag to be updated", required = true) 
			@PathParam("id") String id, 
			Map map)
			throws ConddbWebException {
		Response resp;
		try {
			log.info("Request for updating global tag "+id+" using "+map.size());
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				String msg = "Error updating globaltag resource: id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			//TODO: This should be activated in the final code
			if (existing.islocked()) {
				String msg = "Error updating locked globaltag resource: id "+id+" is locked!";
				throw buildException(msg, msg, Response.Status.NOT_MODIFIED);
			}
			if (map.containsKey("name")) {
				List<GlobalTagMap> maplist = globalTagService.getGlobalTagMapByGlobalTagName(id);
				if (maplist != null && maplist.size()>0 || existing.islocked()) {
					String msg = "Error updating locked globaltag resource: <name> cannot be modified!";
					throw buildException(msg, msg, Response.Status.NOT_MODIFIED);
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
			existing.setResId(existing.getName());
			GlobalTagResource resource = (GlobalTagResource) springResourceFactory.getResource("globaltag", info,
					existing);
			resp = Response.ok(resource, MediaType.APPLICATION_JSON).build();
			return resp;
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error updating locked globaltag resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		} catch (ConversionException e) {
			log.debug("Generate exception using an ConversionException exception..."+e.getMessage());
			String msg = "ConversionException updating locked globaltag resource:";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@Path(Link.GLOBALTAGS+"/maps/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@ApiOperation(value = "Map a tag to a globaltag.",
    notes = "Can be used for one or many tags.",response=GlobalTag.class)
	public Response mapTagsToGlobalTag(
			@Context UriInfo info, 
			@ApiParam(value = "id: id of the globaltag to be updated", required = true) 
			@PathParam("id") String id, 
			@ApiParam(value = "action: {addtags|merge} default is addtags ", required = true) 
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
				String msg = "Error creating mapping: <name> cannot be null!";
				throw buildException(msg, msg, Response.Status.NOT_MODIFIED);		
			}
			log.info("Request to associate tags to a global tag "+id+" using a pattern "+pattern);
			log.info("   - using common values for record and label :"+record+" "+label);
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				String msg = "Error creating mapping, globaltag resource with id "+id+" not found!";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			if (existing.islocked()) {
				String msg = "Error in add a mapping to a locked globaltag resource: "+existing.toString();
				throw buildException(msg, msg, Response.Status.NOT_MODIFIED);
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
			resp = Response.ok(existing).build();
			return resp;

		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error creating mappings for globaltag resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);	
		}
	}

	@Path(Link.GLOBALTAGS+"/{id}")
	@DELETE
	@ApiOperation(value = "Delete a globaltag.",
    notes = "It should be used one global tag at the time. This method is meant for administration purposes.",
    response=GlobalTag.class)
	public Response deleteGlobalTag(
			@ApiParam(value = "id: id of the globaltag to be deleted", required = true) 
			@PathParam("id") String id) throws ConddbWebException {
		Response resp;
		try {
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing.islocked()) {
				String msg = "Error in removing a locked globaltag resource: "+existing.toString();
				throw buildException(msg, msg, Response.Status.NOT_MODIFIED);
			}
			existing = globalTagAdminController.deleteGlobalTag(id);
			resp = Response.ok(existing).build();
			return resp;
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error removing a globaltag resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);	
		}
	}

}
