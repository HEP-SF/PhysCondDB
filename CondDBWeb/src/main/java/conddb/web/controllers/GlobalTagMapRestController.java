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
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.ErrorMessage;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.GlobalTagMapResource;
import conddb.web.resources.GlobalTagResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.GLOBALTAGMAPS)
@Controller
public class GlobalTagMapRestController  extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/trace")
	public Response getGlobalTag(
			@Context UriInfo info,
			@DefaultValue("globaltag") @QueryParam("type") final String type,
			@DefaultValue("none") @QueryParam("id") final String name
			) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for type "
				+ type + " and name "+name);
		ConddbWebException ex = new ConddbWebException();
		try {
			List<GlobalTagMap> list  = null;
			if (type.equals("globaltag")) {
				list = this.globalTagService.getGlobalTagMapByGlobalTagName(name);
				log.debug("Controller has executed query for globaltag search...");
			} else if (type.equals("tag")) {
				list = this.globalTagService.getGlobalTagMapByTagName(name);
				log.debug("Controller has executed query for tag search...");
			} else {
				ErrorMessage error = new ErrorMessage("Error in input arguments: [type] should be [globaltag|tag]! ");
				error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
				error.setInternalMessage("Cannot use type "+ type + " for searching associations ");
				ex.setStatus(Response.Status.BAD_REQUEST);
				ex.setErrMessage(error);
				throw ex;
			}
			if (list == null || list.isEmpty()) {
				return Response.ok((CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, Collections.emptyList())).build();	
			}
			log.debug("Controller has retrieved a list of size "+list.size());
			Collection<GlobalTagMap> globaltagmaps = CollectionUtils.iterableToCollection(list);
			CollectionResource collres = listToCollection(globaltagmaps, true, info,null,null);
			return created(collres);
			
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error retrieving association resource ");
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot creating an association resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	public Response getGlobalTag(
			@Context UriInfo info,
			@PathParam("id") Long id) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for "+ id);
		Response resp = null;
		try {
			GlobalTagMap entity = null;
			if (id != null) {
				entity = this.globalTagService.getGlobalTagMap(id);
			} else {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
				return resp;
			}
			entity.setResId(entity.getId().toString());
			GlobalTagMapResource gtagres = (GlobalTagMapResource) springResourceFactory.getResource("globaltagmap", info, entity);
			resp = Response.ok(gtagres).build();
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
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, Collections.emptyList());
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
