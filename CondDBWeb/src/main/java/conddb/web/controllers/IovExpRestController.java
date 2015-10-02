/**
 * 
 */
package conddb.web.controllers;

import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
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

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.dao.admin.controllers.GlobalTagAdminController;
import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.GlobalTagStatus;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;
import conddb.data.exceptions.ConversionException;
import conddb.utils.json.serializers.TimestampDeserializer;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.GlobalTagResource;
import conddb.web.resources.IovResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT)
@Controller
public class IovExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.IOVS)
	public Response create(@Context UriInfo info, Iov iov) throws ConddbWebException {
		try {
			log.debug("Inserting iov "+iov);
			Iov saved = iovService.insertIov(iov);
			saved.setResId(saved.getId().toString());
			IovResource resource = (IovResource) springResourceFactory.getResource("iov", info,
					saved);
			return created(resource);
		} catch (Exception e) {
			throw new ConddbWebException("Cannot create entity " + iov.getSince() + " : " + e.getMessage());
		}
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path(Link.IOVS+"/payload")
	public Response createIovWithPayload(
			@Context UriInfo info, 
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("streamerInfo") String strinfo, 
			@FormDataParam("objectType") String objtype, 
			@FormDataParam("backendInfo") String bkinfo, 
			@FormDataParam("version") String version,
			@FormDataParam("since") BigDecimal since,
			@FormDataParam("sinceString") String sincestr,
			@FormDataParam("tag") String tagname
			) throws ConddbWebException {
		try {
			Tag entity = globalTagService.getTag(tagname);
			entity.setModificationTime(null); // This will be re-set later during the update
			
			Payload storable = new Payload(null,objtype,bkinfo,strinfo,version);
			String filename = fileDetail.getFileName();
			storable = iovService.createStorablePayload(filename, uploadedInputStream, storable);
			
			// Store the payload: this will then not be rolledback if something goes wrong later on
			// We do not care too much since in that case the payload is simply already there
			Payload stored = iovService.insertPayload(storable, storable.getData());
			log.info("Stored payload "+stored.getHash());
			
			// Create the iov and store it
			Iov iov = new Iov(since,sincestr,stored,entity);
			log.debug("Inserting iov "+iov);
			Iov saved = iovService.insertIov(iov);

			// This update will change the modification time 
			entity = globalTagService.insertTag(entity);
			
			// Create the IovResource for the Response
			saved.setResId(saved.getId().toString());
			saved.setTag(entity);
			saved.setPayload(stored);
			IovResource resource = (IovResource) springResourceFactory.getResource("iov", info,
					saved);
			return created(resource);
		} catch (Exception e) {
			throw new ConddbWebException("Cannot create entry for time since " + since + " : " + e.getMessage());
		}
	}


	@Path(Link.IOVS+"/{id}")
	@DELETE
	public Response deleteIov(@PathParam("id") Long id) throws ConddbWebException {
		Response resp;
		try {
			Iov existing = iovService.deleteIov(id);
			resp = Response.ok(existing).build();

		} catch (ConddbServiceException e) {
			throw new ConddbWebException("Cannot remove id " + id + " : " + e.getMessage());
		}
		return resp;
	}

}
