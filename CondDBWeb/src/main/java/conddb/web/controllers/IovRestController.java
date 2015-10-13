/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.IovService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author aformic
 *
 */
@Path(Link.IOVS)
@Controller
@Api(value = Link.IOVS)
public class IovRestController extends BaseController {

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
	@ApiOperation(value = "Finds Iovs for a given tag using a specific globaltag for snapshotTime", 
	notes = "This function takes parameters in input like the time range and pagination",
	response=Iov.class)
	public CollectionResource getIovsInTag(@Context UriInfo info,
			@ApiParam(value = "tag: the tagname", required = true) @QueryParam("tag") final String id,
			@ApiParam(value = "globaltag: the globaltag name", required = false) @DefaultValue("none") @QueryParam("globaltag") final String globaltagid,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "since: the string representing since time", required = false) @DefaultValue("0") @QueryParam("since") final String since,
			@ApiParam(value = "until: the string representing until time", required = false) @DefaultValue("Inf") @QueryParam("until") final String until,
			@ApiParam(value = "page: the page number", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size", required = false) @DefaultValue("1000") @QueryParam("size") Integer size)
					throws ConddbWebException {
		this.log.info("IovRestController processing request for iovs in tag " + id);
		Collection<Iov> entitylist;
		try {
			BigDecimal sincetime = null;
			BigDecimal untiltime = null;
			PageRequest preq = new PageRequest(ipage, size);
			Tag atag = globalTagService.getTag(id);
			List<Iov> iovlist = null;
			Timestamp snapshotTime = null;
			if (!globaltagid.equals("none")) {
				GlobalTag gtag = globalTagService.getGlobalTag(globaltagid);
				snapshotTime = gtag.getSnapshotTime();
				log.debug("Setting snapshot time to " + snapshotTime);
			}
			if (until.equalsIgnoreCase("INF")) {
				sincetime = new BigDecimal(since);
				untiltime = new BigDecimal(Iov.MAX_TIME);

				if (since.equals("0")) {
					iovlist = this.iovService.getIovsByTag(atag, preq, snapshotTime);
				}
			} else {
				sincetime = new BigDecimal(since);
				untiltime = new BigDecimal(until);
			}
			if (iovlist == null) {
				iovlist = this.iovService.getIovsByTagBetween(atag.getId(), sincetime, untiltime, preq, snapshotTime);
			}
			entitylist = CollectionUtils.iterableToCollection(iovlist);

			if (entitylist == null || entitylist.size() == 0) {
				String msg = "Iov list is empty";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			Collection items = new ArrayList(entitylist.size());
			for (Iov iov : entitylist) {
				iov.setResId(iov.getId().toString());
				iov.getPayload().setResId(iov.getPayload().getHash());
				iov.getTag().setResId(iov.getTag().getName());
				if (expand) {
					items.add(springResourceFactory.getResource("iov", info, iov));
				} else {
					items.add(springResourceFactory.getResource("link", info, iov));
				}
			}
			return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.IOVS, items);

		} catch (ConddbServiceException e) {
			String msg = "Error retrieving iov list resource ";
			throw buildException(msg + " " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	@ApiOperation(value = "Finds Iovs by id",
    notes = "Usage of this method is essentially for href links.",
    response=Iov.class)
	public Response getIovById(@ApiParam(value = "id: the iovid", required = true) @DefaultValue("1000")@PathParam("id") Long id) throws ConddbWebException {
		this.log.info("IovRestController processing request for iov id " + id);
		Response resp = null;
		try {
			Iov entity = this.iovService.getIov(id);
			entity.getPayload().setResId(entity.getPayload().getHash());
			resp = Response.ok(entity).build();

		} catch (Exception e) {
			String msg = "Error retrieving iov by id "+id;
			throw buildException(msg + " " + e.getMessage(), msg, Response.Status.NOT_FOUND);
		}
		return resp;
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all Iovs",
    notes = "Usage of this method is essentially for test purposes.",
    response = Iov.class,
    responseContainer = "List")
	public CollectionResource listIovs(@Context UriInfo info, 
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
			@DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "page: the page number", required = false)
			@DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size", required = false)
			@DefaultValue("1000") @QueryParam("size") Integer size) throws ConddbWebException {
		this.log.info("IovRestController processing request for iov list (expansion = " + expand + ")");
		Collection<Iov> entitylist;
		try {
			// Here we could implement pagination
			PageRequest preq = new PageRequest(ipage, size);
			entitylist = CollectionUtils.iterableToCollection(iovService.findAll(preq));
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (entitylist == null || entitylist.size() == 0) {
			return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.IOVS,
					Collections.emptyList());
		}
		Collection items = new ArrayList(entitylist.size());
		for (Iov iov : entitylist) {
			iov.setResId(iov.getId().toString());
			iov.getPayload().setResId(iov.getPayload().getHash());
			iov.getTag().setResId(iov.getTag().getName());
			if (expand) {
				items.add(springResourceFactory.getResource("iov", info, iov));
			} else {
				items.add(springResourceFactory.getResource("link", info, iov));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.IOVS, items);
	}

}
