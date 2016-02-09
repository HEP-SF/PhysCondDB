/**
 * 
 */
package conddb.web.controllers;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import conddb.data.Payload;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.repositories.PayloadRepository;
import conddb.utils.bytes.PayloadBytesHandler;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.PayloadResource;
import conddb.web.resources.SpringResourceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author formica
 *
 */
@Path(Link.EXPERT)
@Controller
@Api(value = Link.EXPERT)
public class PayloadExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayloadRepository payloadRepository;
	@Autowired
	private IovService iovService;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;
	@Autowired
	private PayloadBytesHandler payloadBytesHandler;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@Value("${physconddb.upload.dir:/tmp}")
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path(Link.PAYLOAD)
	@ApiOperation(value = "Insert a Payload.", notes = "Input data are in a FORM, containing file, type, streamer and version information.", response = Payload.class)
	public Response create(@Context UriInfo info, @FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("type") String objtype,
			@FormDataParam("streamer") String strinfo, 
			@FormDataParam("version") String version)
					throws ConddbWebException {

		if (fileDetail == null) {
			String msg = "Error in creating a payload resource: file is null";
			throw buildException(msg, msg, Response.Status.BAD_REQUEST);
		}
		String name = fileDetail.getFileName();
		try {
			String outfname = name + "-uploaded";
			String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER + outfname;
			log.debug("Uploads files location is " + SERVER_UPLOAD_LOCATION_FOLDER);
//			payloadBytesHandler.saveToFile(uploadedInputStream, uploadedFileLocation);
//			byte[] bytes = payloadBytesHandler.readFromFile(uploadedFileLocation);
//
//			Payload apayload = new Payload();
//			apayload.setVersion(version);
//			apayload.setObjectType(objtype);
//			apayload.setStreamerInfo(strinfo);
//			apayload.setDatasize(bytes.length);
//
//			PayloadData pylddata = new PayloadData();
//			// pylddata.setData(bytes);
//			pylddata.setUri(uploadedFileLocation);
//
//			PayloadHandler phandler = new PayloadHandler(pylddata);
//			PayloadData storable = phandler.getPayloadWithHash();
//			PayloadData stored = payloadDataBaseCustom.save(storable);
//
//			apayload.setHash(storable.getHash());
			Payload storable = new Payload(null,objtype,"db",strinfo,version);
			storable = iovService.createStorablePayload(fileDetail.getFileName(),uploadedInputStream, storable);
			
			log.info("Uploaded object has hash " + storable.getHash());
			log.info("Uploaded object has data size " + storable.getDatasize());
			Payload existing = payloadRepository.findOne(storable.getHash());
			if (existing != null) {
				String msg = "Error in creating a payload resource: hash already exists in the DB.";
				throw buildException(msg, msg, Response.Status.NOT_MODIFIED);
			}
//			apayload.setBackendInfo(stored.getUri());
//			Payload saved = payloadRepository.save(apayload);

			// Store the payload: this will then not be rolledback if something goes wrong later on
			// We do not care too much since in that case the payload is simply already there
			Payload stored = iovService.insertPayload(storable, storable.getData());

			PayloadResource resource = (PayloadResource) springResourceFactory.getResource("payload", info, stored);
			return created(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error creating payload resource using input file " + name;
			throw buildException(msg + " " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
