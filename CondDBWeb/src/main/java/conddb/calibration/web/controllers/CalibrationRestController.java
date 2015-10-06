/**
 * 
 */
package conddb.calibration.web.controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import conddb.calibration.tools.CalibrationService;
import conddb.calibration.tools.DirectoryMapperService;
import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.controllers.SystemNodeService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.ErrorMessage;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagStatus;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;

/**
 * @author aformic
 *
 */
@Path(Link.CALIB)
@Controller
public class CalibrationRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	@Autowired
	private CalibrationService calibrationService;
	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private DirectoryMapperService directoryMapperService;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;

	@Value( "${physconddb.upload.dir:/tmp}" )
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path("/commit")
	public Response commitFile(
			@QueryParam("filename") final String filename,
			@QueryParam("path") final String path,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws ConddbWebException {
		Response resp = null;
		try {
			// Check if path and tag do exists
			SystemDescription sd = systemNodeService.getSystemNodesByNodeFullpath(path);
			if (sd == null) {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
			} else {
				// Use the default HEAD tag for upload of the files...
				// The first time this will create it, in future we will only add IOVs to it
				// The logic suppose that every system with a reasonable tag name was created before
				String tag = sd.getTagNameRoot()+"-HEAD";
				
				// The tag name requested is compatible with the existing tag name for the specified path
				Tag tagstored = globalTagService.getTag(tag);
				if (tagstored == null) {
					//The tag requested has not yet being stored in the DB
					//Store the new tag as it is
					Tag entity = new Tag(tag,"run",filename,"none","Calibration for "+filename,new BigDecimal(0),new BigDecimal(0));
					tagstored = globalTagService.insertTag(entity);
					
				} 
				// We now add the file to the HEAD tag by overwriting the IOV with since = 0
				Payload payload = uploadFile(fileDetail,uploadedInputStream,filename,"HEAD","none");
				Iov storable = new Iov(new BigDecimal(0),"0",null, tagstored);
				calibrationService.commit(tagstored, storable, payload);
			}
			
		} catch (Exception e) {
			resp = Response.status(Response.Status.BAD_REQUEST).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}
	
	private Payload uploadFile(FormDataContentDisposition fileDetail, InputStream uploadedInputStream, String filename,
			String string, String string2) {
		return null;
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/tag")
	public Response tagFile(
			@QueryParam("name") final String tagname,
			@QueryParam("globaltag") final String globaltagname,
			@QueryParam("desc") final String description,
			@QueryParam("release") final String release) throws ConddbWebException {
		Response resp = null;
		try {
			if (!globaltagname.startsWith(tagname)) {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
				throw new ConddbWebException("ERROR: For calibration files we assume that globaltag and tagname start with the same string");
			}
			List<Tag> taglist = globalTagService.getTagByNameLike(tagname);
			if (taglist == null) {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
			} else {
				GlobalTag globaltag = globalTagService.getGlobalTag(globaltagname);
				if (globaltag == null) {
					globaltag = new GlobalTag(globaltagname,new BigDecimal(0),description,release);
					// Fixing the snapshot time we can be sure that the IOVs that are retrieved belong
					// to the correct version of the file
					Timestamp snapshotTime = new Timestamp(Instant.now().toEpochMilli());
					globaltag.setSnapshotTime(snapshotTime);
					globaltag = globalTagService.insertGlobalTag(globaltag);
				}
				if (globaltag.islocked()) {
					resp = Response.status(Response.Status.BAD_REQUEST).build();
					throw new ConddbWebException("Cannot tag a locked globaltag");
				}
				calibrationService.tagPackage(tagname, globaltag);
			}
			
		} catch (Exception e) {
			resp = Response.status(Response.Status.BAD_REQUEST).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/lock")
	public Response lock(
			@QueryParam("globaltag") final String globaltagname,
			@QueryParam("status") final String status) throws ConddbWebException {
		Response resp = null;
		try {
			GlobalTag entity = globalTagService.getGlobalTag(globaltagname);
			if (entity.islocked()) {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
				throw new ConddbWebException("Cannot modify or lock a locked globaltag");
			}
			entity.setLockstatus(GlobalTagStatus.valueOf(status).toString());
			Timestamp snapshotTime = new Timestamp(Instant.now().toEpochMilli());
			entity.setSnapshotTime(snapshotTime);
			entity = globalTagService.insertGlobalTag(entity);
			
		} catch (Exception e) {
			resp = Response.status(Response.Status.BAD_REQUEST).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/dump/{id}")
	public Response dumpContent(
			@PathParam("id") final String globaltagname) throws ConddbWebException {
		Response resp = null;
		ConddbWebException ex = new ConddbWebException();
		try {
			GlobalTag globaltag = globalTagService.getGlobalTag(globaltagname);
			directoryMapperService.dumpGlobalTagOnDisk(globaltag);
			resp = Response.ok().build();
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error dumping tree structure for global tag "+globaltagname);
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot dump tree structure :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
		return resp;
	}

	
	public Response getGlobalTag(
			@PathParam("gtagname") final String globaltagname,
			@DefaultValue("off") @QueryParam("dump") final String dump) throws ConddbWebException {
		this.log.info("CalibrationRestController processing request for getting global tag name"
				+ globaltagname);
		Response resp = null;
		try {
			if (globaltagname.contains("%")) {
				throw new ConddbWebException("Cannot search for generic global tag names");
			} else {
				if (dump.equals("off")) {
					GlobalTag gtag = this.globalTagService.getGlobalTag(globaltagname);
					resp = Response.ok(gtag).build();
				} else if (dump.equals("on")) {
					GlobalTag gtag = this.globalTagService.getGlobalTagFetchTags(globaltagname);
					log.debug("Dumping globaltag "+gtag.getName()+" into file system ");
					directoryMapperService.dumpGlobalTagOnDisk(gtag);
					resp = Response.ok(gtag).build();
				}
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
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
