/**
 * 
 */
package conddb.web.controllers;

import java.util.ArrayList;
import java.util.Collection;

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
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTag;
import conddb.svc.dao.controllers.GlobalTagService;
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
@Path(Link.GLOBALTAGS)
@Controller
@Api(value = Link.GLOBALTAGS)
public class GlobalTagRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{gtagname}")
	@ApiOperation(value = "Finds GlobalTags by name", notes = "Usage of % allows to select based on patterns", response = GlobalTag.class, responseContainer = "List")
	public Response findGlobalTag(@Context UriInfo info,
			@ApiParam(value = "name pattern for the search", required = true) @PathParam("gtagname") final String globaltagname,
			@ApiParam(value = "trace {off|on} allows to retrieve associated global tags", required = false) @DefaultValue("off") @QueryParam("trace") final String trace,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") final boolean expand)
					throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for get global tag name " + globaltagname);

		Response result = doTask(globaltagname, expand, trace, info);
		return result;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all GlobalTags", notes = "Usage of url argument expand={true|false} in order to see full resource content or href links only", response = GlobalTag.class, responseContainer = "List")
	public Response listGlobalTags(@Context UriInfo info,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") boolean expand)
					throws ConddbWebException {

		this.log.info("GlobalTagRestController processing request for global tag list (expansion = " + expand + ")");

		Collection<GlobalTag> globaltags = getGlobalTagList(null);
		if (globaltags == null || globaltags.size() == 0) {
			String msg = "Empty globaltags collection";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
		}
		CollectionResource resource = listToCollection(globaltags, expand, info);
		return ok(resource);
	}

	protected Response doTask(String globaltagname, boolean expand, String trace, UriInfo info) throws ConddbWebException {
		Response result = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		if (globaltagname.contains("%")) {
			Collection<GlobalTag> gtaglist = getGlobalTagList(globaltagname);
			if (gtaglist == null) {
				String msg = "Empty globaltags collection";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			CollectionResource collres = listToCollection(gtaglist, expand, info);
			result = ok(collres);
		} else {
			GlobalTag entity = getGlobalTag(globaltagname, trace);
			if (entity == null) {
				String msg = "Global Tag "+globaltagname+" not found.";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			GenericPojoResource<GlobalTag> resource = (GenericPojoResource) springResourceFactory.getGenericResource(info, entity, 2, null);
			result = ok(resource);
		}
		return result;
	}

	protected Collection<GlobalTag> getGlobalTagList(String globaltagname) {
		Collection<GlobalTag> gtaglist = null;
		try {
			if (globaltagname == null) {
				gtaglist = CollectionUtils.iterableToCollection(globalTagService.findAllGlobalTags());
			} else {
				gtaglist = CollectionUtils.iterableToCollection(globalTagService.getGlobalTagByNameLike(globaltagname));
			}
		} catch (ConddbServiceException e) {
			log.error("Cannot retrieve global tag list for pattern " + globaltagname);
		}
		return gtaglist;
	}

	protected GlobalTag getGlobalTag(String globaltagname, String trace) {
		GlobalTag entity = null;
		try {
			if (trace.equals("off")) {
				log.debug("Search for a globaltag " + globaltagname);
				entity = globalTagService.getGlobalTag(globaltagname);
			} else {
				log.debug("Search for a globaltag " + globaltagname + " and associated tags...");
				entity = globalTagService.getGlobalTagFetchTags(globaltagname);
				log.debug("Retrieved globaltag entity : ");
				log.debug("                   content : " + entity);
			}
			if (entity != null) {
				entity.setResId(entity.getName());
			}
		} catch (ConddbServiceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entity;
	}

	protected CollectionResource listToCollection(Collection<GlobalTag> globaltags, boolean expand, UriInfo info) {
		Collection items = new ArrayList(globaltags.size());
		for (GlobalTag globaltag : globaltags) {
			globaltag.setResId(globaltag.getName());
			if (expand) {
				GenericPojoResource<GlobalTag> resource = (GenericPojoResource<GlobalTag>) springResourceFactory.getGenericResource(info, globaltag, 1, null);
				items.add(resource);
			} else {
				items.add(springResourceFactory.getResource("link", info, globaltag));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.GLOBALTAGS, items);
	}
}
