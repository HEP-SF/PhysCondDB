/**
 * 
 */
package conddb.web.controllers;

import java.io.InputStream;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.dao.repositories.PayloadRepository;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.handler.PayloadHandler;
import conddb.utils.bytes.PayloadBytesHandler;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;

/**
 * @author formica
 *
 */
@Path(Link.EXPERT)
@Controller
public class PayloadExpRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayloadRepository payloadRepository;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;
	@Autowired
	private PayloadBytesHandler payloadBytesHandler;

	@Value( "${physconddb.upload.dir:/tmp}" )
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path(Link.PAYLOAD)
	public @ResponseBody String handleFileUpload(
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, 
			@FormDataParam("type") String objtype,
			@FormDataParam("streamer") String strinfo, 
			@FormDataParam("version") String version) throws ConddbWebException {
		try {
			if (fileDetail != null) {
				String name = fileDetail.getFileName();
				try {
					String outfname = name + "-uploaded";
					String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER + outfname;
					log.debug("Uploads files location is "+SERVER_UPLOAD_LOCATION_FOLDER);
					payloadBytesHandler.saveToFile(uploadedInputStream, uploadedFileLocation);
					byte[] bytes = payloadBytesHandler.readFromFile(uploadedFileLocation);

					Payload apayload = new Payload();
					apayload.setVersion(version);
					apayload.setObjectType(objtype);
					apayload.setStreamerInfo(strinfo);
					apayload.setDatasize(bytes.length);

					PayloadData pylddata = new PayloadData();
//					pylddata.setData(bytes);
					pylddata.setUri(uploadedFileLocation);

					PayloadHandler phandler = new PayloadHandler(pylddata);
					PayloadData storable = phandler.getPayloadWithHash();
					PayloadData stored = payloadDataBaseCustom.save(storable);

					apayload.setHash(storable.getHash());
					log.info("Uploaded object has hash " + storable.getHash());
					log.info("Uploaded object has data size " + apayload.getDatasize());

					if (payloadRepository.findOne(apayload.getHash()) == null) {
						apayload.setBackendInfo(stored.getUri());
						payloadRepository.save(apayload);
					} else {
						return "Payload with hash " + storable.getHash() + " already exists...skip update ";
					}
					return "You successfully uploaded " + name + " into " + outfname + ", with hash "
							+ storable.getHash();
				} catch (Exception e) {
					return "You failed to upload " + name + " => " + e.getMessage();
				}
			} else {
				return "You failed to upload because the filedetail object is null.";
			}
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}
}
