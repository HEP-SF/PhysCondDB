/**
 * 
 */
package conddb.web.controllers;

import io.swagger.annotations.Api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagController;
import conddb.dao.controllers.IovController;
import conddb.dao.svc.ConddbClientService;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.web.exceptions.ConddbWebException;

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
	private GlobalTagController globalTagController;
	@Autowired
	private IovController iovController;
	@Autowired
	private ConddbClientService clientservice;

	@POST
	@Path("/globaltag/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTag insertGlobalTag(GlobalTag jsonString) throws Exception {
		this.log.info("CondExpertWebController processing request for insertGlobalTag ...");
		GlobalTag gtag = this.globalTagController.insertGlobalTag(jsonString);
		return gtag;
	}

	@PUT
	@Path("/globaltag/update/{id}")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTag updateGlobalTag(@PathParam("id") String id,
			GlobalTag jsonString) throws Exception {
		this.log.info("CondExpertWebController processing request for updateGlobalTag ...");
		GlobalTag stored = this.globalTagController.getGlobalTag(id);
		if (stored != null) {
			if (jsonString.getDescription() != null) {
				stored.setDescription(jsonString.getDescription());
			}
			if (jsonString.getLockstatus() != null) {
				stored.setLockstatus(jsonString.getLockstatus());
			}
			if (jsonString.getRelease() != null) {
				stored.setRelease(jsonString.getRelease());
			}
			if (jsonString.getSnapshotTime() != null) {
				stored.setSnapshotTime(jsonString.getSnapshotTime());
			}
			GlobalTag gtag = this.globalTagController.insertGlobalTag(stored);
			return gtag;
		}
		return null;
	}

	@POST
	@Path("/tag/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Tag insertTag(Tag jsonString) throws Exception {
		this.log.info("CondExpertWebController processing request for insertTag ...");
		Tag tag = this.globalTagController.insertTag(jsonString);
		return tag;
	}

	@POST
	@Path("/map/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTagMap insertGlobalTagMap(GlobalTagMap jsonString)
			throws Exception {
		this.log.info("CondExpertWebController processing request for insertGlobalTagMap ...");
		GlobalTagMap gtagmap = this.globalTagController
				.insertGlobalTagMap(jsonString);
		return gtagmap;
	}

	@POST
	@Path("/iov/add")
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public Iov insertIov(Iov jsonString) throws Exception {
		this.log.info("CondExpertWebController processing request for insertIov using tag..."
				+ jsonString.getTag().getName());
		Iov iov = this.iovController.insertIov(jsonString);
		return iov;
	}

	@POST
	@Path("/map/tag2gtag")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public GlobalTagMap mapTagToGtag(
			@QueryParam(value = "globaltagname") String globaltagname,
			@QueryParam(value = "tagname") String tagname)
			throws ConddbWebException {
		try {
			this.log.info("CondExpertWebController processing request for mapTagToGtag ..."
					+ globaltagname + " " + tagname);
			GlobalTag gtag = globalTagController.getGlobalTag(globaltagname);
			List<Tag> list = clientservice.getTagOne(tagname);
			Tag atag = list.get(0);
			this.log.info("CondExpertWebController processing request for mapTagToGtag using "
					+ gtag + " " + atag);
			GlobalTagMap gtagmap = this.globalTagController.mapTagToGlobalTag(
					atag, gtag);
			return gtagmap;
		} catch (Exception e) {
			throw new ConddbWebException(e);
		}
	}

}
