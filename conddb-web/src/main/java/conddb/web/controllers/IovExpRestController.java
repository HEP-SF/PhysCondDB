/**
 * 
 */
package conddb.web.controllers;

import java.io.InputStream;
import java.math.BigDecimal;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
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

import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.PayloadRepository;
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
public class IovExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private PayloadRepository payloadRepository;
	@Autowired
	private IovService iovService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@POST
	@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path(Link.IOVS)
	@ApiOperation(value = "Create an IOV entry.",
    notes = "Input data are in json, and should match all needed fields for a new Iov. Here the problem is to know the hash of the payload.",
    response=Iov.class)
	public Response createIov(@Context UriInfo info, Iov iov) throws ConddbWebException {
		try {
			log.debug("Inserting iov "+iov);
			Iov saved = iovService.insertIov(iov);
			saved.setResId(saved.getId().toString());
			GenericPojoResource<Iov> resource = (GenericPojoResource<Iov>) springResourceFactory.getGenericResource(info, saved, 1, null);
			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error creating IOV resource using "+iov.toString();
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path(Link.IOVS+"/payload")
	@ApiOperation(value = "Create an IOV entry with its own payload.",
    notes = "Input data are inside a FORM. It should contain: file, streamerInfo, objectType, backendInfo, version, since, sinceString, tag.",
    response=Iov.class)
	@Transactional
	public Response createIovWithPayload(
			@Context UriInfo info, 
			@ApiParam(value = "file: the filename of the input payload", required = true) 
			@FormDataParam("file") InputStream uploadedInputStream,
			@ApiParam(hidden = true) 
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@ApiParam(value = "streamerInfo: the streamer information of the input payload", required = true) 
			@FormDataParam("streamerInfo") String strinfo, 
			@ApiParam(value = "objectType: the object type of the input payload", required = true) 
			@FormDataParam("objectType") String objtype, 
			@ApiParam(value = "backendInfo: the backend system of the input payload", required = true) 
			@FormDataParam("backendInfo") String bkinfo, 
			@ApiParam(value = "version: the version of the input payload", required = true) 
			@FormDataParam("version") String version,
			@ApiParam(value = "since: the since time of the IOV.", required = true) 
			@FormDataParam("since") BigDecimal since,
			@ApiParam(value = "sinceString: the since time string representation of the IOV.", required = true) 
			@FormDataParam("sinceString") String sincestr,
			@ApiParam(value = "tag: the tag name where to store the IOV.", required = true) 
			@FormDataParam("tag") String tagname
			) throws ConddbWebException {
		try {
			log.info("IovExpRestController: create iov with payload has arguments " + tagname+" "+since+" "+fileDetail.getFileName());

			Tag entity = globalTagService.getTag(tagname);
			entity.setModificationTime(null); // This will be re-set later during the update
			
			Payload storable = new Payload(null,objtype,bkinfo,strinfo,version);
			storable = iovService.createStorablePayload(fileDetail.getFileName(),uploadedInputStream, storable);

			log.info("Uploaded object has hash " + storable.getHash());
			log.debug("Uploaded object has data size " + storable.getDatasize());

			// Store the payload: this will then not be rolledback if something goes wrong later on
			// We do not care too much since in that case the payload is simply already there
			Payload stored = iovService.insertPayload(storable, storable.getData());

			stored.setResId(stored.getHash());
			log.debug("Stored payload "+stored.getHash());
			
			// Create the iov and store it
			Iov iov = new Iov(since,sincestr,stored,entity);
			log.debug("Inserting iov "+iov);
			Iov saved = iovService.insertIov(iov);
			log.debug("Stored iov "+iov);

			// This update will change the modification time 
			entity = globalTagService.insertTag(entity);
			entity.setResId(entity.getName());
			log.debug("Updated tag modification time");
			
			// Create the IovResource for the Response
			saved.setResId(saved.getId().toString());
			saved.setTag(entity);
			saved.setPayload(stored);
			
			log.info("Upload of payload in iov "+iov+" was successful");
			GenericPojoResource<Iov> resource = (GenericPojoResource<Iov>) springResourceFactory.getGenericResource(info, iov, 0, null);

			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error creating IOV resource inside tag "+tagname;
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path(Link.IOVS+"/async/payload")
	@ApiOperation(value = "Create an IOV entry with its own payload.",
    notes = "Input data are inside a FORM. It should contain: file, streamerInfo, objectType, backendInfo, version, since, sinceString, tag.",
    response=Iov.class)
	public void asyncCreateIovWithPayload(
			@Context UriInfo info, 
			@ApiParam(value = "file: the filename of the input payload", required = true) 
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@ApiParam(value = "streamerInfo: the streamer information of the input payload", required = true) 
			@FormDataParam("streamerInfo") String strinfo, 
			@ApiParam(value = "objectType: the object type of the input payload", required = true) 
			@FormDataParam("objectType") String objtype, 
			@ApiParam(value = "backendInfo: the backend system of the input payload", required = true) 
			@FormDataParam("backendInfo") String bkinfo, 
			@ApiParam(value = "version: the version of the input payload", required = true) 
			@FormDataParam("version") String version,
			@ApiParam(value = "since: the since time of the IOV.", required = true) 
			@FormDataParam("since") BigDecimal since,
			@ApiParam(value = "sinceString: the since time string representation of the IOV.", required = true) 
			@FormDataParam("sinceString") String sincestr,
			@ApiParam(value = "tag: the tag name where to store the IOV.", required = true) 
			@FormDataParam("tag") String tagname,
			@Suspended final AsyncResponse asyncResponse
			) throws ConddbWebException {

		new Thread(new Runnable() {
		    @Override
		    public void run() {
		        Response result;
				try {
					Payload storable = new Payload(null,objtype,"db",strinfo,version);
					String uploadedfilename = tagname+"_"+fileDetail.getFileName();
					storable = iovService.createStorablePayload(uploadedfilename,uploadedInputStream, storable);
					Iov iov = new Iov();
					iov.setSince(since);
					iov.setSinceString(sincestr);
					Iov saved = storePayload(storable, iov, tagname);
					GenericPojoResource<Iov> resource = (GenericPojoResource<Iov>) springResourceFactory.getGenericResource(info, saved, 1, null);
					result =  created(resource);

				} catch (ConddbServiceException e) {
					String msg = "Error creating IOV resource inside tag "+tagname;
					ConddbWebException webex = buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
					result = Response.status(webex.getErrMessage().getCode())
					.entity(webex.getErrMessage())
					.type(MediaType.APPLICATION_JSON).
					build();
				}
		        asyncResponse.resume(result);
		    }
		    
		    @Transactional
		    private Iov storePayload(Payload storable, Iov iov, String tagname) throws ConddbServiceException {
				Tag entity = globalTagService.getTag(tagname);
				entity.setModificationTime(null); // This will be re-set later during the update


				log.info("Uploaded object has hash " + storable.getHash());
				log.info("Uploaded object has data size " + storable.getDatasize());
				Payload stored = iovService.insertPayload(storable, storable.getData());

				stored.setResId(stored.getHash());
				log.info("Stored payload "+stored.getHash());
				iov.setPayload(stored);
				iov.setTag(entity);
				log.debug("Inserting iov "+iov);
				Iov saved = iovService.insertIov(iov);
				entity = globalTagService.insertTag(entity);
				entity.setResId(entity.getName());
				// Create the IovResource for the Response
				saved.setResId(saved.getId().toString());
				saved.setTag(entity);
				saved.setPayload(stored);
				return saved;
		    }
		}).start();
	}


	@Path(Link.IOVS+"/{id}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@DELETE
	@ApiOperation(value = "Delete an IOV.",
    notes = "It should be used one IOV at the time. This method is meant for administration purposes. The payload associated is not removed.",
    response=Iov.class)
	public Response deleteIov(
			@ApiParam(value = "id: id of the IOV to be deleted", required = true) 
			@PathParam("id") Long id) throws ConddbWebException {
		Response resp;
		try {
			Iov existing = iovService.deleteIov(id);
			resp = Response.ok(existing).build();
			return resp;
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..."+e.getMessage());
			String msg = "Error removing a IOV resource: internal server exception !";
			throw buildException(msg+" \n "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);	
		}
	}

}
