/**
 * 
 */
package conddb.web.calib.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.svc.calibration.tools.CalibrationService;
import conddb.svc.calibration.tools.DirectoryMapperService;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.utils.converters.CondTimeTypes;
import conddb.utils.data.TimeRanges;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;

/**
 * @author aformic
 *
 */
@Path(Link.EXPERT+Link.CALIB)
@Controller
public class CalibrationRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private DirectoryMapperService directoryMapperService;
	@Autowired
	@Qualifier("payloaddatadbrepo")
	private PayloadDataBaseCustom payloadDataBaseCustom;

	@Value("${physconddb.upload.dir:/tmp}")
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	public static final String PATH_SEPARATOR = "/";

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path("/commit")
	@Transactional
	public Response commitFile(
			@Context UriInfo info,
			@FormDataParam("package") final String packageName, 
			@FormDataParam("path") final String path,
			@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws ConddbWebException {
		Response resp = null;
		try {
			log.debug("Calibration controller has received arguments: "+path+" "+packageName);
			// Check if path and tag do exists
			String filename = fileDetail.getFileName();
			String filenamenoext = filename.substring(0, filename.lastIndexOf("."));
			String extension = filename.substring(filename.lastIndexOf("."),filename.length());
			log.debug("Calibration controller has digested arguments: "+filename+" "+path+" "+packageName);
			
			String nodefullpath = (path.concat(PATH_SEPARATOR + filenamenoext));
			nodefullpath = nodefullpath.replaceAll("//", "/");
			String tag = null;
			log.info("Calibration controller is searching systems with node fullpath: "+nodefullpath);
			SystemDescription sd = systemNodeService.getSystemNodesByNodeFullpath(nodefullpath);
			if (sd == null) {
				log.debug("System with path " + nodefullpath + " not found....create new system");
				sd = new SystemDescription(nodefullpath, packageName, "New file for system " + packageName);

				// Start from 1 because the first word in the nodefullpath is the package name
				String tagnameroot = getUniqueTagNameRoot(nodefullpath, packageName, filenamenoext, 1);
				sd.setTagNameRoot(tagnameroot);
				sd.setGroupSize(new BigDecimal(10000));
				sd = systemNodeService.insertSystemDescription(sd);
			}
			// Use the default HEAD tag for upload of the files...
			// The first time this will create it, in future we will only add
			// IOVs to it.
			// The logic suppose that every system with a reasonable tag name
			// was created before
			tag = sd.getTagNameRoot() + "-HEAD";

			// The tag name requested is compatible with the existing tag name
			// for the specified path
			Tag tagstored = globalTagService.getTag(tag);
			if (tagstored == null) {
				// The tag requested has not yet being stored in the DB
				// Store the new tag as it is
				Tag entity = new Tag(tag, CondTimeTypes.RUN.name(), filename, "none", "Calibration for " + filename,
						new BigDecimal(0), new BigDecimal(0));
				tagstored = globalTagService.insertTag(entity);
			}
			tagstored.setModificationTime(null);
			// We now add the file to the HEAD tag by overwriting the IOV with
			// since = 0
			Payload storable = new Payload(null,filename,"database",extension,"1.0");
			storable = iovService.createStorablePayload(filename, uploadedInputStream, storable);

			// Store the payload: this will then not be rolledback if something goes wrong later on
			// We do not care too much since in that case the payload is simply already there
			Payload stored = iovService.insertPayload(storable, storable.getData());
			stored.setResId(stored.getHash());
			log.info("Stored payload "+stored.getHash());
			
			// Create the iov and store it
			Iov iov = new Iov(new BigDecimal(0), "t0",stored, tagstored);
			log.debug("Inserting iov "+iov);
			iov = iovService.insertIov(iov);

			// This update will change the modification time 
			tagstored = globalTagService.insertTag(tagstored);
			
			resp = Response.ok(iov).build();
			return resp;

		} catch (ConddbServiceException e) {
			String msg = "Cannot commit files for path " + path;
			throw buildException(msg, msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/tag")
	public Response tagFile(
			@Context UriInfo info,
			@QueryParam("globaltag") final String globaltagname, 
			@QueryParam("package") final String packageName) throws ConddbWebException {
		Response resp = null;
		try {
			GlobalTag globaltag = globalTagService.getGlobalTag(globaltagname);
			if (globaltag == null) {
				// Create a new global tag for the package
				if (!globaltagname.startsWith(packageName)) {
					String msg = "Cannot create global tag using the name " + globaltagname+" for package "+packageName;
					throw buildException(msg, msg, Response.Status.NOT_MODIFIED);					
				}
				log.debug("Creating new global tag using "+globaltagname);
				Timestamp maxsnapshottime = TimeRanges.toTimestamp();
				globaltag = new GlobalTag(globaltagname,new BigDecimal(0),"New global tag for package "+packageName,"1.0",maxsnapshottime);
				globaltag = globalTagService.insertGlobalTag(globaltag);
			}
			if (globaltag.islocked()) {
				String msg = "Cannot modify global tag because it is locked";
				throw buildException(msg, msg, Response.Status.NOT_MODIFIED);					
			}
			log.debug("Load systems entries having tag name root "+packageName);
			Page<SystemDescription> pagelist = systemNodeService.findSystemNodesByTagnameLike(packageName+"%", new PageRequest(0,1000));
			List<SystemDescription> systemlist = pagelist.getContent();
			Set<GlobalTagMap> maplist = new HashSet<GlobalTagMap>();
			for (SystemDescription systemDescription : systemlist) {
				String tagnameroot = systemDescription.getTagNameRoot();
				Tag systemtag = globalTagService.getTag(tagnameroot+"-HEAD");
				if (systemtag == null) {
					String msg = "Cannot associate global tag to null system tag " + tagnameroot+" for package "+packageName;
					throw buildException(msg, msg, Response.Status.NOT_MODIFIED);					
				}
				GlobalTagMap globaltagmap = new GlobalTagMap(globaltag,systemtag,"none","none");
				globaltagmap = globalTagService.insertGlobalTagMap(globaltagmap);
				maplist.add(globaltagmap);
			}
			globaltag.setGlobalTagMaps(maplist);
			resp = Response.ok(globaltag).build();
			return resp;

		} catch (ConddbServiceException e) {
			String msg = "Cannot create tag mappings for " + globaltagname;
			throw buildException(msg, msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}


	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/tar/{id}")
	public Response getTarFromGlobalTag(@PathParam("id") final String globaltagname) throws ConddbWebException {
		Response resp = null;
		try {
			GlobalTag globaltag = globalTagService.getGlobalTag(globaltagname);
			File f = directoryMapperService.createTar(globaltag);
			final InputStream in = new FileInputStream(f);
			StreamingOutput stream = new StreamingOutput() {
				public void write(OutputStream out) throws IOException, WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[1024];

						while ((read = in.read(bytes)) != -1) {
							out.write(bytes, 0, read);
						}
					} catch (Exception e) {
						throw new WebApplicationException(e);
					}
				}
			};

			resp = Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
					.header("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"").build();
			return resp;
		} catch (ConddbServiceException e) {
			String msg = "Error dumping tree structure for global tag " + globaltagname;
			throw buildException(msg, msg + ": " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
		} catch (FileNotFoundException e1) {
			String msg = "Error dumping tree structure for global tag " + globaltagname + ": file not found ";
			throw buildException(msg, msg + ": " + e1.getMessage(), Response.Status.NOT_FOUND);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Consumes({ MediaType.APPLICATION_JSON })
	@Path("/collect/{id}")
	public Response dumpContent(@PathParam("id") final String globaltagname) throws ConddbWebException {
		Response resp = null;
		try {
			GlobalTag globaltag = globalTagService.getGlobalTag(globaltagname);
			directoryMapperService.dumpGlobalTagOnDisk(globaltag);
			resp = Response.ok(globaltag).build();
			return resp;
		} catch (ConddbServiceException e) {
			String msg = "Error dumping tree structure for global tag " + globaltagname;
			throw buildException(msg, msg + ": " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * @param nodefullpath
	 * @param pkgname
	 * @param filename
	 * @param i
	 * @return
	 * @throws ConddbWebException
	 */
	protected String getUniqueTagNameRoot(String nodefullpath, String pkgname, String filename, int i) throws ConddbWebException {
		// Verify if tagnameroot exists, if not try to generate a unique name
		try {
			String tagnameroot = pkgname.concat("_" + filename);
			log.debug("Search for system having tagname like "+tagnameroot);
			SystemDescription sd = systemNodeService.getSystemNodesByTagname(tagnameroot);
			if (sd == null) {
				return tagnameroot;
			}
			String[] path = nodefullpath.split("/");
			if (i<path.length) {
				pkgname = pkgname.concat("_"+path[++i]);
				log.debug("Redefine package name to "+pkgname);
				return getUniqueTagNameRoot(nodefullpath, pkgname, filename, i);
			} else {
				String msg = "Cannot generate a unique tag name for the system " + pkgname;
				throw buildException(msg, msg, Response.Status.INTERNAL_SERVER_ERROR);
			}
		} catch (ConddbServiceException e) {
			String msg = "Cannot generate a unique tag name for the system " + pkgname;
			throw buildException(msg, msg+ ": " + e.getMessage(), Response.Status.INTERNAL_SERVER_ERROR);
		}	
	}
}
