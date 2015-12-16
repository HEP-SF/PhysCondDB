/**
 * 
 */
package conddb.web.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
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

import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.data.handler.PayloadHandler;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.utils.bytes.PayloadBytesHandler;
import conddb.utils.collections.CollectionUtils;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.GenericMessageResource;
import conddb.web.resources.Link;
import conddb.web.resources.PayloadResource;
import conddb.web.resources.SpringResourceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author formica
 *
 */
@Path(Link.PAYLOAD)
@Controller
@Api(value = Link.PAYLOAD)
public class PayloadRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private IovService iovService;
	@Autowired
	private SpringResourceFactory springResourceFactory;
	@Autowired
	private PayloadBytesHandler payloadBytesHandler;

	@Value("${physconddb.upload.dir:/tmp}")
	private String SERVER_UPLOAD_LOCATION_FOLDER;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{hash}")
	@ApiOperation(value = "Finds payload by hash; the payload object contains only metadata on the payload itself.", notes = "Select one payload at the time, no regexp searches allowed here", response = PayloadResource.class)
	public Response getPayload(@Context UriInfo info,
			@ApiParam(value = "hash of the payload", required = true) @PathParam("hash") final String hash,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") final boolean expand)
					throws ConddbWebException {
		this.log.info("PayloadRestController processing request for payload " + hash);
		Response resp = null;
		try {

			Payload entity = iovService.getPayload(hash);
			if (entity == null) {
				String msg = "Cannot find payload corresponding to hash " + hash;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			entity.setResId(hash);
			if (expand) {
				PayloadData entitydata = iovService.getPayloadData(hash);
				if (entitydata == null) {
					String msg = "Cannot find payload data corresponding to hash " + hash;
					throw buildException(msg, msg, Response.Status.NOT_FOUND);
				}
				entitydata.setResId(hash);
				entity.setData(entitydata);
				log.debug("Payload contains " + entity.toString());
				log.debug("  - data :" + entitydata);
			}
			PayloadResource resource = (PayloadResource) springResourceFactory.getResource("payload", info, entity);
			return created(resource);
		} catch (ConddbServiceException e) {
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			throw new ConddbWebException(e.getMessage());
		}
	}

//	@GET
//	@Produces({ MediaType.APPLICATION_OCTET_STREAM })
//	@Path("/data/{hash}")
//	@ApiOperation(value = "Finds payload data by hash; the payload object contains the real BLOB.", notes = "Select one payload at the time, no regexp searches allowed here", response = StreamingOutput.class)
//	public Response getBlob(@Context UriInfo info,
//			@ApiParam(value = "hash of the payload", required = true) @PathParam("hash") final String hash)
//					throws ConddbWebException {
//		this.log.info("PayloadRestController processing request to download payload " + hash);
//		Response resp = null;
//		try {
//			StreamingOutput stream = null;
//			// Initialization of an empty file name
//			String filename = "/tmp/none.blob";
//			PayloadData entitydata = iovService.getPayloadData(hash);
//			Payload entity = iovService.getPayload(hash);
//
//			if (entitydata == null || entity == null) {
//				String msg = "Cannot find payload data corresponding to hash " + hash;
//				throw buildException(msg, msg, Response.Status.NOT_FOUND);
//			}
//			// filename = entitydata.getHash() + "."+entity.getStreamerInfo();
//			filename = entitydata.getHash();
//			File f = new File(entitydata.getUri());
//			// payloadBytesHandler.dumpBlobIntoFile(entitydata.getData(),
//			// filename);
//			// Open a file and an inputstream to read it
//			// f = new File(filename);
//			final InputStream in = new FileInputStream(f);
//			// Set the output stream for the response
//			stream = new StreamingOutput() {
//				public void write(OutputStream out) throws IOException, WebApplicationException {
//					try {
//						int read = 0;
//						byte[] bytes = new byte[2048];
//
//						while ((read = in.read(bytes)) != -1) {
//							out.write(bytes, 0, read);
//							log.debug("Copying " + read + " bytes into the output...");
//						}
//						out.flush();
//					} catch (Exception e) {
//						throw new WebApplicationException(e);
//					} finally {
//						log.debug("closing streams...");
//						out.close();
//						in.close();
//					}
//				}
//			};
//
//			resp = Response.ok(stream, MediaType.APPLICATION_OCTET_STREAM_TYPE)
//					.header("Content-Disposition", "attachment; filename=\"" + filename + "\"")
//					//.header("Content-Length", new Long(f.length()).toString())
//					.build();
//			return resp;
//
//		} catch (ConddbServiceException e) {
//			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//			throw new ConddbWebException(e.getMessage());
//		} catch (FileNotFoundException e1) {
//			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
//			throw new ConddbWebException(e1.getMessage());
//		}
//	}

	@GET
	@Path("/data/{hash}")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	@ApiOperation(value = "Finds payload data by hash; the payload object contains the real BLOB.", notes = "Select one payload at the time, no regexp searches allowed here", response = StreamingOutput.class)
	public Response getBlob(@Context UriInfo info,
			@ApiParam(value = "hash of the payload", required = true) @PathParam("hash") final String hash)
					throws ConddbWebException {

		Response resp = null;
		this.log.info("PayloadRestController processing request to download payload " + hash);
		try {
			PayloadData entitydata = iovService.getPayloadData(hash);
			Payload entity = iovService.getPayload(hash);

			if (entitydata == null || entity == null) {
				String msg = "Cannot find payload data corresponding to hash " + hash;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			InputStream in = entitydata.getData().getBinaryStream();

			StreamingOutput stream = new StreamingOutput() {
				@Override
				public void write(OutputStream os) throws IOException, WebApplicationException {
					try {
						int read = 0;
						byte[] bytes = new byte[2048];

						while ((read = in.read(bytes)) != -1) {
							os.write(bytes, 0, read);
							log.debug("Copying " + read + " bytes into the output...");
						}
						os.flush();
					} catch (Exception e) {
						throw new WebApplicationException(e);
					} finally {
						log.debug("closing streams...");
						os.close();
						in.close();
					}
				}
			};
			log.debug("Send back the stream....");
			return Response.ok(stream, MediaType.APPLICATION_JSON_TYPE)
					.header("Content-Disposition", "attachment; filename=\"" + hash + "\"")
					//.header("Content-Length", new Long(f.length()).toString())
					.build();
		} catch (ConddbServiceException e) {
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			throw new ConddbWebException(e.getMessage());			
		} catch (SQLException e1) {
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			throw new ConddbWebException(e1.getMessage());			
		}
	}

	@POST
	@Produces({ MediaType.APPLICATION_JSON })
	@Consumes({ MediaType.MULTIPART_FORM_DATA })
	@Path("/hash")
	@ApiOperation(value = "Take an input file and get its hash generated by the server.", notes = "Used for checking hash generation", response = GenericMessageResource.class)
	public Response getBlobHash(@Context UriInfo info, @FormDataParam("file") InputStream uploadedInputStream,
			@FormDataParam("file") FormDataContentDisposition fileDetail) throws ConddbWebException {
		Response resp = null;
		try {
			if (fileDetail != null) {
				String name = fileDetail.getFileName();
				log.info("Uploaded object has name " + name);

				String outfname = name + "-uploaded";
				String uploadedFileLocation = SERVER_UPLOAD_LOCATION_FOLDER + outfname;
				payloadBytesHandler.saveToFile(uploadedInputStream, uploadedFileLocation);

				PayloadData pylddata = new PayloadData();
				pylddata.setUri(uploadedFileLocation);
				PayloadHandler phandler = new PayloadHandler(pylddata);
				PayloadData storable = phandler.getPayloadWithHash();
				String thehash = storable.getHash();
				log.info("Uploaded object has hash " + storable.getHash());
				log.warn("This method does not perform insertions");
				GenericMessageResource responsemessage = new GenericMessageResource("hash", thehash);
				resp = Response.ok(responsemessage).build();
				return resp;
			}
			String msg = "Cannot find payload file ";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
		} catch (IOException e) {
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			throw new ConddbWebException(e.getMessage());
		} catch (PayloadEncodingException e) {
			resp = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
			throw new ConddbWebException(e.getMessage());
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/filter")
	@ApiOperation(value = "Select a payload filtering on metadata...Not well implemented.", notes = "Select one payload at the time, no regexp searches allowed here. "
			+ " This method is for the moment not well implemented.", response = PayloadResource.class)
	public CollectionResource getPayloadFilteredList(@Context UriInfo info,
			@ApiParam(value = "Parameter name: {datasize|objectType|version}", required = true) @DefaultValue("none") @QueryParam("param") final String param,
			@ApiParam(value = "condition: {eq|gt|..}", required = true) @DefaultValue("eq") @QueryParam("if") final String condition,
			@ApiParam(value = "Parameter value: the value of the selected parameter", required = true) @DefaultValue("0") @QueryParam("value") final String value,
			@ApiParam(value = "page: page number for the query, defaults to 0", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: page size, defaults to 25", required = false) @DefaultValue("25") @QueryParam("size") Integer size)
					throws ConddbWebException {
		this.log.info("PayloadRestController processing request for payload filtered list " + param + " " + condition
				+ " " + value);
		Collection<Payload> entitylist;
		try {
			List<Payload> payloadList = iovService.getPayloadList(param, condition, value);
			entitylist = CollectionUtils.iterableToCollection(payloadList);
		} catch (Exception e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (entitylist == null || entitylist.size() == 0) {
			return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.PAYLOAD,
					Collections.emptyList());
		}
		Collection items = new ArrayList(entitylist.size());
		for (Payload pyld : entitylist) {
			pyld.setResId(pyld.getHash());
			items.add(springResourceFactory.getResource("payload", info, pyld));
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.PAYLOAD, items);
	}

}
