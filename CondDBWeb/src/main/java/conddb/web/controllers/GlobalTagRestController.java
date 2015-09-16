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
import org.springframework.stereotype.Controller;

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;

/**
 * @author aformic
 *
 */
@Path(Link.GLOBALTAGS)
@Controller
public class GlobalTagRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{gtagname}")
	public Response getGlobalTag(
			@PathParam("gtagname") final String globaltagname,
			@DefaultValue("off") @QueryParam("trace") final String trace) throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for get global tag name"
				+ globaltagname);
		Response resp = null;
		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(60);
			
			if (globaltagname.contains("%")) {
				if (trace.equals("on")) {
					// Do not want to trace the full tag list
					throw new ConddbWebException("Cannot activate trace on generic global tag pattern");
				}
				List<GlobalTag> gtaglist = this.globalTagService.getGlobalTagByNameLike(globaltagname);
				resp = Response.ok(gtaglist).cacheControl(control).build();
			} else {
				if (trace.equals("off")) {
					GlobalTag gtag = this.globalTagService.getGlobalTag(globaltagname);
					resp = Response.ok(gtag).cacheControl(control).build();
				} else {
					GlobalTag gtag = this.globalTagService.getGlobalTagFetchTags(globaltagname);
					resp = Response.ok(gtag).cacheControl(control).build();
				}
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
            @DefaultValue("false") @QueryParam("expand") boolean expand) throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for global tag list (expansion = "
				+ expand+")");
		Collection<GlobalTag> globaltags;
		try {
			// Here we could implement pagination
			globaltags = CollectionUtils.iterableToCollection(globalTagService.findAllGlobalTags());
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (globaltags == null || globaltags.size() == 0) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGS, Collections.emptyList());
        }
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
