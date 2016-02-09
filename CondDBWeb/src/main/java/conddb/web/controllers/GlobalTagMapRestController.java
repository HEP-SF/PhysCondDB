/**
 * 
 */
package conddb.web.controllers;

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

import conddb.data.GlobalTagMap;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.utils.collections.CollectionUtils;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.GlobalTagMapResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author aformic
 *
 */
@Path(Link.GLOBALTAGMAPS)
@Controller
@Api(value = Link.GLOBALTAGMAPS)
public class GlobalTagMapRestController  extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/trace")
	@ApiOperation(value = "Finds Tags to GlobalTag's mappings by name either of the tag or of the global tag",
    notes = "Usage of % allows to select based on patterns",
    response = CollectionResource.class,
    responseContainer = "List")
	public Response getGlobalTagMapTrace(
			@Context UriInfo info,
			@ApiParam(value = "type: {globaltag|tag}", required = true)
			@DefaultValue("globaltag") @QueryParam("type") final String type,
			@ApiParam(value = "id: the name either of the tag or the global tag, patterns not allowed", required = true)
			@DefaultValue("none") @QueryParam("id") final String name,
			@DefaultValue("true") @QueryParam("expand") final boolean expand
			) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for type "
				+ type + " and name "+name);
		try {
			List<GlobalTagMap> list  = null;
			if (type.equals("globaltag")) {
				list = this.globalTagService.getGlobalTagMapByGlobalTagName(name);
				log.debug("Controller has executed query for globaltag search...");
			} else if (type.equals("tag")) {
				list = this.globalTagService.getGlobalTagMapByTagName(name);
				log.debug("Controller has executed query for tag search...");
			} else {
				String msg = "Error in input arguments: [type] should be either globaltag or tag ! ";
				throw buildException(msg, msg, Response.Status.BAD_REQUEST);
			}
			if (list == null || list.isEmpty()) {
				String msg = "Associations not found for "+type+" and id "+name;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			log.debug("Controller has retrieved a list of size "+list.size());
			Collection<GlobalTagMap> globaltagmaps = CollectionUtils.iterableToCollection(list);
			CollectionResource collres = listToCollection(globaltagmaps, expand, info,null,null);
			return created(collres);
			
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving association resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/find")
	@ApiOperation(value = "Finds Tags to GlobalTag's mappings by name  of the tag and of the global tag",
    notes = "Usage of % is forbidden, names should be complete, only one object should be returned",
    response = CollectionResource.class,
    responseContainer = "List")
	public Response getGlobalTagMap(
			@Context UriInfo info,
			@ApiParam(value = "globaltag: the global tag name, patterns not allowed", required = true)
			@DefaultValue("none") @QueryParam("globaltag") final String globaltagname,
			@ApiParam(value = "tag: the tag name, patterns not allowed", required = true)
			@DefaultValue("none") @QueryParam("tag") final String tagname,
			@DefaultValue("true") @QueryParam("expand") final boolean expand
			) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for global tag "
				+ globaltagname + " and tag "+tagname);
		try {
			GlobalTagMap entity = null;
			if (!globaltagname.contains("%") && !tagname.contains("%")) {
				entity = this.globalTagService.getGlobalTagMapByTagAndGlobalTag(globaltagname, tagname);
				log.debug("Controller has executed query for globaltag search...");
			} else {
				String msg = "Error in input arguments: globaltag or tag names contain pattern string !";
				throw buildException(msg, msg, Response.Status.BAD_REQUEST);
			}
			if (entity == null) {
				String msg = "Associations not found for "+globaltagname+" and "+tagname;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			log.debug("Controller has retrieved one map object "+entity);
			
			entity.setResId(entity.getId().toString());
			GlobalTagMapResource gtagres = (GlobalTagMapResource) springResourceFactory.getResource("globaltagmap", info, entity);
			return created(gtagres);
			
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving association resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	@ApiOperation(value = "Finds Tags to GlobalTag's mappings by id (integer)",
    notes = "This is ment essentially for internal usage when finding dependencies",
    response = GlobalTagMapResource.class)
	public Response getGlobalTagMapById(
			@Context UriInfo info,
			@ApiParam(value = "id: id of the association", required = true)
			@PathParam("id") Long id) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for "+ id);

		ConddbWebException ex = new ConddbWebException();
		try {
			GlobalTagMap entity = null;
			entity = this.globalTagService.getGlobalTagMap(id);
			if (entity == null) {
				String msg = "Associations not found for id "+id;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			entity.setResId(entity.getId().toString());
			GlobalTagMapResource gtagres = (GlobalTagMapResource) springResourceFactory.getResource("globaltagmap", info, entity);
			return created(gtagres);
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving association resource for id "+id;
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all Tags to GlobalTag's mappings",
    notes = "Usage of pagination and parameter expand to get full output of the association or link only",
    response = CollectionResource.class,
    responseContainer = "List")
	public CollectionResource listGlobalTagMaps(
			@Context UriInfo info,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
            @DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "page: page number for the query, defaults to 0", required = false)
            @DefaultValue("0") @QueryParam("page") Integer ipage, 
			@ApiParam(value = "size: size of the page, defaults to 25", required = false)
            @DefaultValue("25") @QueryParam("size") Integer size) throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for global tag list (expansion = "
				+ expand+")");
		Collection<GlobalTagMap> globaltagmaps;
		try {
			// Here we could implement pagination
			PageRequest preq = new PageRequest(ipage,size);
			globaltagmaps = CollectionUtils.iterableToCollection(globalTagService.findAllGlobalTagMaps(preq));
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (globaltagmaps == null || globaltagmaps.size() == 0) {
			String msg = "Associations not found";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
        }
		return listToCollection(globaltagmaps, expand, info, ipage, size);
	}

	protected CollectionResource listToCollection(Collection<GlobalTagMap> globaltagmaps, boolean expand, UriInfo info, Integer ipage, Integer size) {
        Collection items = new ArrayList(globaltagmaps.size());
        for( GlobalTagMap globaltagmap : globaltagmaps) {
        	globaltagmap.setResId(globaltagmap.getId().toString());
        	globaltagmap.getGlobalTag().setResId(globaltagmap.getGlobalTagName());
        	globaltagmap.getSystemTag().setResId(globaltagmap.getTagName());
            if (expand) {
                items.add(springResourceFactory.getResource("globaltagmap", info, globaltagmap));
            } else {
                items.add(springResourceFactory.getResource("link",info,globaltagmap));
            }
        }
        if (ipage == null || size ==null) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items);
        }
        	
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items, ipage, size);
	}

}
