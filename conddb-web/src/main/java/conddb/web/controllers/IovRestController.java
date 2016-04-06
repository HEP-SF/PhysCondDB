/**
 * 
 */
package conddb.web.controllers;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
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

import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.data.view.IovGroups;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.IovService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.generic.GenericPojoResource;
import conddb.web.utils.collections.CollectionUtils;
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
	@ApiOperation(value = "Finds Iovs for a given tag using a specific globaltag for snapshotTime", notes = "This function takes parameters in input like the time range and pagination", response = Iov.class)
	public Response getIovsInTag(@Context UriInfo info,
			@ApiParam(value = "tag: the tagname", required = true) @QueryParam("tag") final String id,
			@ApiParam(value = "globaltag: the globaltag name", required = false) @DefaultValue("none") @QueryParam("globaltag") final String globaltagid,
			@ApiParam(value = "snapshot: the snapshot time", required = false) @DefaultValue("-1") @QueryParam("snapshot") final Long snapt,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "last: {niovs} loads only the last N iovs", required = false) @DefaultValue("-1") @QueryParam("last") final Integer niovs,
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
				log.debug("Setting snapshot time to " + snapshotTime + " for further queries based on globaltag id "
						+ globaltagid);
			} else if (snapt > -1) {
				snapshotTime = new Timestamp(snapt);
				log.debug("Setting snapshot time to " + snapshotTime + " from user input "+snapt);
			}
			if (until.equalsIgnoreCase("INF")) {
				sincetime = new BigDecimal(since);
				untiltime = new BigDecimal(Iov.MAX_TIME);

				if (since.equals("0") && niovs<0) {
					iovlist = this.iovService.getIovsByTag(atag, preq, snapshotTime);
				} 
			} else {
				sincetime = new BigDecimal(since);
				untiltime = new BigDecimal(until);
			}
			if (iovlist == null) {
				if (niovs > 0) {
					log.debug("Ignoring time ranges for the moment...take last " + niovs);
					preq = new PageRequest(ipage, niovs);
					iovlist = this.iovService.getLastNIovsByTag(atag.getId(), preq);
				} else {
					iovlist = this.iovService.getIovsByTagBetween(atag.getId(), sincetime, untiltime, preq,
							snapshotTime);
				}
			}
			entitylist = CollectionUtils.iterableToCollection(iovlist);

			if (entitylist == null || entitylist.size() == 0) {
				String msg = "Iov list is empty";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			CollectionResource collres = listToCollection(entitylist, expand, info);
			return ok(collres);
			
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving iov list resource ";
			throw buildException(msg + " " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
	
	protected CollectionResource listToCollection(Collection<Iov> iovs, boolean expand, UriInfo info) {
		Collection items = new ArrayList(iovs.size());
		for (Iov iov : iovs) {
			if (expand) {
				log.debug("Creating a generic resource from iov "+iov);
				GenericPojoResource<Iov> resource = (GenericPojoResource<Iov>) springResourceFactory.getGenericResource(info, iov, 0, null);
				items.add(resource);
			} else {
				log.debug("Creating a generic link out of the iov "+iov);
				items.add(springResourceFactory.getResource("link", info, iov));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.IOVS, items);
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	@ApiOperation(value = "Finds Iovs by id", notes = "Usage of this method is essentially for href links.", response = Iov.class)
	public Response getIovById(@Context UriInfo info,
			@ApiParam(value = "id: the iovid", required = true) @DefaultValue("1000") @PathParam("id") Long id)
					throws ConddbWebException {
		this.log.info("IovRestController processing request for iov id " + id);
		try {
			Iov entity = this.iovService.getIov(id);
			GenericPojoResource<Iov> resource = (GenericPojoResource<Iov>) springResourceFactory.getGenericResource(info, entity, 1, null);
			return ok(resource);
		} catch (Exception e) {
			String msg = "Error retrieving iov by id " + id;
			throw buildException(msg + " " + e.getMessage(), msg, Response.Status.NOT_FOUND);
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all Iovs", notes = "Usage of this method is essentially for test purposes.", response = Iov.class, responseContainer = "List")
	public Response listIovs(@Context UriInfo info,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "page: the page number", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size", required = false) @DefaultValue("1000") @QueryParam("size") Integer size)
					throws ConddbWebException {
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
			String msg = "Iov list is empty";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
		}
		CollectionResource collres = listToCollection(entitylist, expand, info);
		return ok(collres);
	}

	@SuppressWarnings("unchecked")
	@GET
	@Path("/groups/{tagname}")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all Iovs", notes = "Usage of this method is essentially for test purposes.", response = IovGroups.class, responseContainer = "List")
	public Response listIovGroups(@Context UriInfo info,
			@ApiParam(value = "tagname: the tag name", required = true) @DefaultValue("none") @PathParam("tagname") String tagname)
					throws ConddbWebException {
		this.log.info("IovRestController processing request for iov group list for tag "+tagname);
		Collection<IovGroups> entitylist;
		try {
			entitylist = CollectionUtils.iterableToCollection(iovService.getIovGroupsForTag(tagname));
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (entitylist == null || entitylist.size() == 0) {
			String msg = "Iov Group list is empty";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
		}
		return Response.status(Response.Status.OK).entity(entitylist).build();
	}

}
