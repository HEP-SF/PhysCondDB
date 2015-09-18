/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
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
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.IOVS)
@Controller
public class IovRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private IovService iovService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/find")
	public Response getIovs(
			@DefaultValue("none") @QueryParam("tag") final String tag,
			@DefaultValue("0") @QueryParam("since") final String since,
			@DefaultValue("Inf") @QueryParam("until") final String until,
			@DefaultValue("0") @QueryParam("page") Integer ipage, 
            @DefaultValue("1000") @QueryParam("size") Integer size
			) throws ConddbWebException {
		this.log.info("IovRestController processing request for iovs in tag "
				+ tag);
		Response resp = null;
		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(60);
			BigDecimal sincetime = null;
			BigDecimal untiltime = null;
			if (until.equalsIgnoreCase("INF")) {
				 sincetime = new BigDecimal(since);
				 untiltime = new BigDecimal(Iov.MAX_TIME);
			} else {
				 sincetime = new BigDecimal(since);
				 untiltime = new BigDecimal(until);
			}
			PageRequest preq = new PageRequest(ipage,size);
			Tag atag = globalTagService.getTag(tag);
			
			List<Iov> entitylist = this.iovService.getIovsByTag(atag,preq);	
			resp = Response.ok(entitylist).cacheControl(control).build();
			
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	public Response getIov(
			@PathParam("id") Long id
			) throws ConddbWebException {
		this.log.info("IovRestController processing request for iov id "
				+ id);
		Response resp = null;
		try {
			Iov entity = this.iovService.getIov(id);	
			resp = Response.ok(entity).build();
			
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CollectionResource list(
			@Context UriInfo info,
            @DefaultValue("false") @QueryParam("expand") boolean expand,
            @DefaultValue("0") @QueryParam("page") Integer ipage, 
            @DefaultValue("1000") @QueryParam("size") Integer size) throws ConddbWebException {
		this.log.info("IovRestController processing request for iov list (expansion = "
				+ expand+")");
		Collection<Iov> entitylist;
		try {
			// Here we could implement pagination
			PageRequest preq = new PageRequest(ipage,size);
			entitylist = CollectionUtils.iterableToCollection(iovService.findAll(preq));
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (entitylist == null || entitylist.size() == 0) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.IOVS, Collections.emptyList());
        }
        Collection items = new ArrayList(entitylist.size());
        for( Iov iov : entitylist) {
        	iov.setResId(iov.getId().toString());
        	iov.getPayload().setResId(iov.getPayload().getHash());
        	iov.getTag().setResId(iov.getTag().getName());
            if (expand) {
                items.add(springResourceFactory.getResource("iov", info, iov));
            } else {
                items.add(springResourceFactory.getResource("link",info,iov));
            }
        }
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.IOVS, items);
	}

}
