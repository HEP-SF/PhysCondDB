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
import conddb.data.ErrorMessage;
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
		} catch (ConddbServiceException e) {
			ConddbWebException ex = new ConddbWebException();
			ErrorMessage error = new ErrorMessage("Error creating new GlobalTag resource "+globaltag.getName());
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot create new global tag :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
	}

	@Path(Link.GLOBALTAGS+"/{id}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response updateGlobalTag(@Context UriInfo info, @PathParam("id") String id, Map map)
			throws ConddbWebException {
		Response resp;
		try {
			ConddbWebException ex = new ConddbWebException();
			log.info("Request for updating global tag "+id+" using "+map.size());
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				ErrorMessage error = new ErrorMessage("Error updating not existing GlobalTag resource "+id);
				error.setCode(Response.Status.NOT_FOUND.getStatusCode());
				error.setInternalMessage("Cannot update global tag because is not found in DB");
				ex.setStatus(Response.Status.NOT_FOUND);
				ex.setErrMessage(error);
				throw ex;
			}
			//TODO: This should be activated in the final code
			if (existing.islocked()) {
				ErrorMessage error = new ErrorMessage("Error updating locked GlobalTag resource "+id);
				error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
				error.setInternalMessage("Cannot update global tag because it is locked");
				ex.setStatus(Response.Status.NOT_MODIFIED);
				ex.setErrMessage(error);
				log.info("In principle no update is possible for a locked global tag");
			}
			if (map.containsKey("name")) {
				List<GlobalTagMap> maplist = globalTagService.getGlobalTagMapByGlobalTagName(id);
				if (maplist != null && maplist.size()>0 || existing.islocked()) {
					ErrorMessage error = new ErrorMessage("Cannot update GlobalTag resource <name> field when associations are active..."+id);
					error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
					error.setInternalMessage("Cannot update global tag because <name> field cannot be modified");
					ex.setStatus(Response.Status.NOT_MODIFIED);
					ex.setErrMessage(error);
					throw ex;
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
			ConddbWebException ex = new ConddbWebException(e.getMessage());
			ex.setStatus(Response.Status.BAD_REQUEST);
			throw ex;
		} catch (ConversionException e) {
			log.debug("Generate exception using an ConversionException exception..."+e.getMessage());
			ConddbWebException ex = new ConddbWebException(e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			throw ex;
		}
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
		ConddbWebException ex = new ConddbWebException();
		try {
			String record = (map.containsKey("record")) ? (String) map.get("record") : "none" ;
			String label = (map.containsKey("label")) ? (String) map.get("label") : "none" ;
			String pattern = null;
			if (map.containsKey("name")) {
				pattern = (String)map.get("name");
			} else {
				ErrorMessage error = new ErrorMessage("Cannot create mappings when <name> field is null..."+id);
				error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
				error.setInternalMessage("Cannot create mapping because <name> field for tag pattern is null");
				ex.setStatus(Response.Status.NOT_MODIFIED);
				ex.setErrMessage(error);
				throw ex;			
			}
			log.info("Request to associate tags to a global tag "+id+" using a pattern "+pattern);
			log.info("   - using common values for record and label :"+record+" "+label);
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing == null) {
				ErrorMessage error = new ErrorMessage("Cannot create mappings because global tag resource was not found for id="+id);
				error.setCode(Response.Status.NOT_FOUND.getStatusCode());
				error.setInternalMessage("Cannot create mapping because global tag resource was not found in the DB");
				ex.setStatus(Response.Status.NOT_FOUND);
				ex.setErrMessage(error);
				throw ex;			
			}
			if (existing.islocked()) {
				ErrorMessage error = new ErrorMessage("Cannot create mappings because global tag "+id+" is locked");
				error.setCode(Response.Status.NOT_ACCEPTABLE.getStatusCode());
				error.setInternalMessage("Cannot create mapping because global tag resource is locked in the DB");
				ex.setStatus(Response.Status.NOT_ACCEPTABLE);
				ex.setErrMessage(error);
				throw ex;			
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
			return resp;

		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Cannot create mappings because internal service received exception for global tag "+id);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot create mapping because of an internal server error : "+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;			
		}
	}

	@Path(Link.GLOBALTAGS+"/{id}")
	@DELETE
	public Response deleteGlobalTag(@PathParam("id") String id) throws ConddbWebException {
		Response resp;
		ConddbWebException ex = new ConddbWebException();
		try {
			GlobalTag existing = globalTagService.getGlobalTag(id);
			if (existing.islocked()) {
				ErrorMessage error = new ErrorMessage("Cannot remove global tag "+id+" because it is locked");
				error.setCode(Response.Status.NOT_MODIFIED.getStatusCode());
				error.setInternalMessage("Cannot remove a locked global tag");
				ex.setStatus(Response.Status.NOT_MODIFIED);
				ex.setErrMessage(error);
			}
			existing = globalTagAdminController.deleteGlobalTag(id);
			resp = Response.ok(existing).build();

		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Cannot remove global tag because internal service received exception for global tag "+id);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot remove global tag because of an internal server error : "+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;			
		}
		return resp;
	}

}
