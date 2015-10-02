/**
 * 
 */
package conddb.web.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.IovService;
import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.handler.PayloadHandler;
import conddb.utils.bytes.PayloadBytesHandler;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.PayloadResource;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author formica
 *
 */
@Path(Link.PAYLOAD)
@Controller
public class PayloadRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IovService iovService;
	@Autowired
	private SpringResourceFactory springResourceFactory;
	@Autowired
	private PayloadBytesHandler payloadBytesHandler;

	@Value( "${physconddb.upload.dir:/tmp}" )
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{hash}")
	public Response getPayload(
			@Context UriInfo info,
			@PathParam("hash") final String hash,
			@DefaultValue("false") @QueryParam("expand") final boolean expand) throws ConddbWebException {
		this.log.info("PayloadRestController processing request for payload "
				+ hash);
		Response resp = null;
		try {
			
			try {
				Payload entity = iovService.getPayload(hash);
				entity.setResId(hash);
				if (expand) {
					PayloadData entitydata = iovService.getPayloadData(hash);
					entitydata.setResId(hash);
					entity.setData(entitydata);
					log.debug("Payload contains "+entity.toString());
					log.debug("  - data :"+entitydata);
				}
				PayloadResource resource = (PayloadResource)springResourceFactory.getResource("payload",info, entity);
				resp = Response.ok(resource).build();

			} catch (Exception e) {
				throw new ConddbWebException(e.getMessage());
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

	@GET
	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
	@Path("/data/{hash}")
	public Response getBlob(
			@Context UriInfo info,
			@PathParam("hash") final String hash) throws ConddbWebException {
		this.log.info("PayloadRestController processing request to download payload "
				+ hash);
		Response resp = null;
		try {
			
			try {
				PayloadData entitydata = iovService.getPayloadData(hash);
				String filename = "/tmp/"+entitydata.getHash()+".blob";
				payloadBytesHandler.dumpBlobIntoFile(entitydata.getData(),filename);
				File f = new File(filename);
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
				
				resp = Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM_TYPE).header("Content-Disposition", "attachment; filename=\"" + f.getName() + "\"" ).build();
			} catch (Exception e) {
				throw new ConddbWebException(e.getMessage());
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}


	@POST
	@Produces({ MediaType.TEXT_PLAIN })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path("/hash")
	public String getBlobHash(@FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws ConddbWebException {
		try {
			if (fileDetail != null) {
				String name = fileDetail.getFileName();
				try {
					log.info("Uploaded object has name " + name);

					String outfname = name + "-uploaded";
					String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER + outfname;
					payloadBytesHandler.saveToFile(uploadedInputStream, uploadedFileLocation);

					PayloadData pylddata = new PayloadData();
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

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/filter")
	public CollectionResource getPayloadFilteredList(
			@Context UriInfo info,
			@DefaultValue("none") @QueryParam("param") final String param,
			@DefaultValue("eq") @QueryParam("if") final String condition,
			@DefaultValue("0") @QueryParam("value") final String value,
			@DefaultValue("0") @QueryParam("page") Integer ipage, 
            @DefaultValue("1000") @QueryParam("size") Integer size) throws ConddbWebException {
		this.log.info("PayloadRestController processing request for payload filtered list "
				+ param + " " +condition+" "+value);
		Collection<Payload> entitylist;		
		try {			
			List<Payload> payloadList = iovService.getPayloadList(param, condition, value); 
			entitylist = CollectionUtils.iterableToCollection(payloadList);
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (entitylist == null || entitylist.size() == 0) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.PAYLOAD, Collections.emptyList());
        }
		Collection items = new ArrayList(entitylist.size());
        for( Payload pyld : entitylist) {
        	pyld.setResId(pyld.getHash());
            items.add(springResourceFactory.getResource("payload", info, pyld));
        }
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.PAYLOAD, items);
	}

}
