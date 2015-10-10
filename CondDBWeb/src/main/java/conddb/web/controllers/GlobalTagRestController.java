/**
 * 
 */
package conddb.web.controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.GlobalTagResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
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
	@ApiOperation(value = "Finds GlobalTags by name",
    notes = "Usage of % allows to select based on patterns",
    response = GlobalTagResource.class,
    responseContainer = "List")
	public Response getGlobalTag(
			@Context UriInfo info,
			@ApiParam(value = "name pattern for the search", required = true)
			@PathParam("gtagname") final String globaltagname,
			@ApiParam(value = "trace {off|on} allows to retrieve associated global tags", required = false)
			@DefaultValue("off") @QueryParam("trace") final String trace,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
			@DefaultValue("true") @QueryParam("expand") final boolean expand) throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for get global tag name "
				+ globaltagname);

		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(600);
			if (globaltagname.contains("%")) {
				if (trace.equals("on")) {
					String msg = "Error in input arguments: [globaltag name] should be unique for tracing ! ";
					throw buildException(msg, msg, Response.Status.BAD_REQUEST);
				}
				Collection<GlobalTag> gtaglist = CollectionUtils.iterableToCollection(this.globalTagService.getGlobalTagByNameLike(globaltagname));
				CollectionResource collres = listToCollection(gtaglist, expand, info);
				return created(collres);
			} else {
				GlobalTag entity = null;
				if (trace.equals("off")) {
					log.debug("Search for a globaltag "+globaltagname);
					entity = this.globalTagService.getGlobalTag(globaltagname);
				} else {
					log.debug("Search for a globaltag "+globaltagname+" and associated tags...");
					entity = this.globalTagService.getGlobalTagFetchTags(globaltagname);
					log.debug("Retrieved globaltag entity : ");
					log.debug("                   content : "+entity);
				}
				if (entity == null) {
					String msg = "GlobalTag not found for id "+globaltagname;
					throw buildException(msg, msg, Response.Status.NOT_FOUND);
				}				
				entity.setResId(entity.getName());
				GlobalTagResource gtagres = (GlobalTagResource) springResourceFactory.getResource("globaltag", info, entity);
				return created(gtagres);
			}
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving globaltag resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all GlobalTags",
    notes = "Usage of url argument expand={true|false} in order to see full resource content or href links only",
    response = CollectionResource.class,
    responseContainer = "List")
	public CollectionResource list(
			@Context UriInfo info,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
            @DefaultValue("false") @QueryParam("expand") boolean expand) throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for global tag list (expansion = "
				+ expand+")");
		Collection<GlobalTag> globaltags = null;
		try {
			// Here we could implement pagination
			globaltags = CollectionUtils.iterableToCollection(globalTagService.findAllGlobalTags());
		} catch (ConddbServiceException e) {
			String msg = "Error in creation of globaltags collection";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (globaltags == null || globaltags.size() == 0) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGS, Collections.emptyList());
        }
        return listToCollection(globaltags, expand, info);
	}

	protected CollectionResource listToCollection(Collection<GlobalTag> globaltags, boolean expand, UriInfo info) {
        Collection items = new ArrayList(globaltags.size());
        for( GlobalTag globaltag : globaltags) {
        	globaltag.setResId(globaltag.getName());
            if (expand) {
                items.add(springResourceFactory.getResource("globaltag", info, globaltag));
            } else {
                items.add(springResourceFactory.getResource("link",info,globaltag));
            }
        }
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGS, items);		
	}
}
