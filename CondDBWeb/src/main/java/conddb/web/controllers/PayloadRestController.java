/**
 * 
 */
package conddb.web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.IovService;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.PayloadData;
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
					entity.setData(entitydata);
					log.debug("Payload contains "+entity.toString());
					log.debug("  - data :"+entity.getData().toString());
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
