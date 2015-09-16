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
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.GLOBALTAGMAPS)
@Controller
public class GlobalTagMapRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/trace")
	public Response getGlobalTag(
			@DefaultValue("globaltag") @QueryParam("type") final String type,
			@DefaultValue("none") @QueryParam("id") final String name
			) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for type "
				+ type + " and name "+name);
		Response resp = null;
		try {
			if (type.equals("globaltag")) {
				List<GlobalTagMap> list = this.globalTagService.getGlobalTagMapByGlobalTagName(name);
				resp = Response.ok(list).build();
			} else if (type.equals("tag")) {
				List<GlobalTagMap> list = this.globalTagService.getGlobalTagMapByTagName(name);
				resp = Response.ok(list).build();	
			} else {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	public Response getGlobalTag(
			@PathParam("id") Long id) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for "+ id);
		Response resp = null;
		try {
			if (id != null) {
				GlobalTagMap entity = this.globalTagService.getGlobalTagMap(id);
				resp = Response.ok(entity).build();	
			} else {
				resp = Response.status(Response.Status.BAD_REQUEST).build();
			}
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
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items, ipage, size);
	}

}
