/**
 * 
 */
package conddb.admin.web.controllers;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.svc.ConddbClientService;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.web.exceptions.ConddbWebException;
import io.swagger.annotations.Api;

/**
 * @author formica
 *
 */
@Path("/expert")
@Api(value = "/expert")
@Controller
public class CondExpertWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	@Autowired
	private ConddbClientService clientservice;

	/**
	 * Add a new global tag.
	 * @param jsonString
	 * @return
	 * @throws ConddbWebException
	 */
	@POST
	@Path("/globaltag/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTag insertGlobalTag(GlobalTag jsonString) throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for insertGlobalTag ...");
			GlobalTag gtag = this.globalTagService.insertGlobalTag(jsonString);
			return gtag;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	/**
	 * Update global tag fields.
	 * TODO: Remove the lock status that should be updated only by an expert. A method already
	 * exists in GlobalTagExpertController for this purpose.
	 * @param jsonString
	 * @return
	 * @throws ConddbWebException
	 */
	@POST
	@Path("/globaltag/update")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTag updateGlobalTag(GlobalTag jsonString) throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for updateGlobalTag ...");
			GlobalTag stored = this.globalTagService.getGlobalTag(jsonString.getName());
			if (stored != null) {
				if (jsonString.getDescription() != null) {
					stored.setDescription(jsonString.getDescription());
				}
				// FIXME: this should be removed....updating the lock status should be done
				// via the REST service /globaltag/lock/update
				if (jsonString.getLockstatus() != null) {
					stored.setLockstatus(jsonString.getLockstatus());
				}
				if (jsonString.getRelease() != null) {
					stored.setRelease(jsonString.getRelease());
				}
				if (jsonString.getSnapshotTime() != null) {
					stored.setSnapshotTime(jsonString.getSnapshotTime());
				}
				if (jsonString.getValidity() != null) {
					stored.setValidity(jsonString.getValidity());
				}
				GlobalTag gtag = this.globalTagService.insertGlobalTag(stored);
				return gtag;
			}
			return null;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}


	/**
	 * Add a new tag.
	 * @param jsonString
	 * @return The new tag.
	 * @throws ConddbWebException
	 */
	@POST
	@Path("/tag/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Tag insertTag(Tag jsonString) throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for insertTag ...");
			Tag tag = this.globalTagService.insertTag(jsonString);
			return tag;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	
	/**
	 * The tag update url is used to update a set of fields for an existing tag.
	 * @param jsonString
	 * @return
	 * @throws ConddbWebException
	 */
	@POST
	@Path("/tag/update")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Tag updateTag(Tag jsonString) throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for updateTag ...");
			Tag stored = this.globalTagService.getTag(jsonString.getName());
			if (stored != null) {
				if (jsonString.getDescription() != null) {
					stored.setDescription(jsonString.getDescription());
				}
				if (jsonString.getObjectType() != null) {
					stored.setObjectType(jsonString.getObjectType());
				}
				if (jsonString.getTimeType() != null) {
					stored.setTimeType(jsonString.getTimeType());
				}
				if (jsonString.getSynchronization() != null) {
					stored.setSynchronization(jsonString.getSynchronization());
				}
				if (jsonString.getEndOfValidity() != null) {
					stored.setEndOfValidity(jsonString.getEndOfValidity());
				}
				Tag atag = this.globalTagService.insertTag(stored);
				return atag;
			}
			return null;
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@POST
	@Path("/map/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTagMap insertGlobalTagMap(GlobalTagMap jsonString) throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for insertGlobalTagMap ...");
			GlobalTagMap gtagmap = this.globalTagService.insertGlobalTagMap(jsonString);
			return gtagmap;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	@POST
	@Path("/iov/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Iov insertIov(Iov jsonString) throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for insertIov using tag..."
					+ jsonString.getTag().getName());
			Iov iov = this.iovService.insertIov(jsonString);
			return iov;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

	@POST
	@Path("/map/addtoglobaltag")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTagMap mapAddTagToGtag(@QueryParam(value = "globaltagname") String globaltagname,
			@QueryParam(value = "tagname") String tagname) throws ConddbWebException {
		try {
			this.log.info(
					"CondExpertWebController processing request for mapTagToGtag ..." + globaltagname + " " + tagname);
			GlobalTag gtag = globalTagService.getGlobalTag(globaltagname);
			List<Tag> list = clientservice.getTagOne(tagname);
			Tag atag = list.get(0);
			this.log.info("CondExpertWebController processing request for mapTagToGtag using " + gtag + " " + atag);
			GlobalTagMap gtagmap = this.globalTagService.mapAddTagToGlobalTag(atag, gtag, null, null);
			return gtagmap;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}
//	@POST
//	@Path("/map/rmfromglobaltag")
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	public void mapRemoveTagFromGtag(@QueryParam(value = "globaltagname") String globaltagname,
//			@QueryParam(value = "tagname") String tagname) throws ConddbWebException {
//		try {
//			this.log.info(
//					"CondExpertWebController processing request for mapRemoveTagFromGtag ..." + globaltagname + " " + tagname);
//			GlobalTag gtag = globalTagController.getGlobalTag(globaltagname);
//			List<Tag> list = clientservice.getTagOne(tagname);
//			Tag atag = list.get(0);
//			this.log.info("CondExpertWebController processing request for mapRemoveTagFromGtag using " + gtag + " " + atag);
//			this.globalTagExpertController.
//			return;
//		} catch (Exception e) {
//			throw new ConddbWebException(e);
//		}
//	}

}
