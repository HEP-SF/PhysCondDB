/**
 * 
 */
package conddb.admin.web.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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
import conddb.web.exceptions.ConddbWebException;
import io.swagger.annotations.Api;

/**
 * @author formica
 *
 */
@Path("/user")
@Api(value = "/user")
@Controller
public class CondPayloadWebController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private PayloadRepository payloadRepository;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;

	@Value( "${physconddb.upload.dir:/tmp}" )
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path("/payload/upload")
	public @ResponseBody String handleFileUpload(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("type") String objtype,
			@FormDataParam("streamer") String strinfo, @FormDataParam("version") String version)
					throws ConddbWebException {
		try {
			if (fileDetail != null) {
				String name = fileDetail.getFileName();
				try {
					String outfname = name + "-uploaded";
					String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER + outfname;
					log.debug("Uploads files location is "+SERVER_UPLOAD_LOCATION_FOLDER);
					saveToFile(uploadedInputStream, uploadedFileLocation);
					byte[] bytes = readFromFile(uploadedFileLocation);

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

	@GET
	@Produces({ MediaType.TEXT_PLAIN })
	@Path(value = "/payload/exists/{hash}")
	public @ResponseBody String findHash(@PathParam("hash") String hash) throws ConddbWebException {

		try {
			Payload storedhash = payloadRepository.findOne(hash);
			if (storedhash == null) {
				return "NOT_EXISTS";
			}
			return storedhash.toString();
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path(value = "/payload/makehash")
	public String getBlobHash(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws ConddbWebException {
		try {
			if (fileDetail != null) {
				String name = fileDetail.getFileName();
				try {
					log.info("Uploaded object has name " + name);

					String outfname = name + "-uploaded";
					String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER + outfname;
					saveToFile(uploadedInputStream, uploadedFileLocation);
					//byte[] bytes = readFromFile(uploadedFileLocation);

					PayloadData pylddata = new PayloadData();
//					pylddata.setData(bytes);
					pylddata.setUri(uploadedFileLocation);

					PayloadHandler phandler = new PayloadHandler(pylddata);
					PayloadData storable = phandler.getPayloadWithHash();

					log.info("Uploaded object has hash " + storable.getHash());
					log.warn("This method does not perform insertions");

					return storable.getHash();
				} catch (Exception e) {
					return "You failed to upload " + name + " => " + e.getMessage();
				}
			} else {
				return "You failed to upload because the filedetail was null.";
			}
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
	}

	private void saveToFile(InputStream uploadedInputStream, String uploadedFileLocation) {

		try {
			OutputStream out = null;
			int read = 0;
			byte[] bytes = new byte[1024];

			out = new FileOutputStream(new File(uploadedFileLocation));
			while ((read = uploadedInputStream.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
			out.flush();
			out.close();
		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	private byte[] readFromFile(String uploadedFileLocation) {

		try {
			java.nio.file.Path path = Paths.get(uploadedFileLocation);
			byte[] data = Files.readAllBytes(path);
			return data;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;

	}
}
