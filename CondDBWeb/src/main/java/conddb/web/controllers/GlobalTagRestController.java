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
import conddb.data.ErrorMessage;
import conddb.data.GlobalTag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.GlobalTagResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import io.swagger.annotations.Api;

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
	public Response getGlobalTag(
			@Context UriInfo info,
			@PathParam("gtagname") final String globaltagname,
			@DefaultValue("off") @QueryParam("trace") final String trace) throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for get global tag name"
				+ globaltagname);
		Response resp = null;
		ConddbWebException ex = new ConddbWebException();
		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(600);
			if (globaltagname.contains("%")) {
				if (trace.equals("on")) {
					ErrorMessage error = new ErrorMessage("Error in input arguments: [globaltag name] should be unique for tracing ! ");
					error.setCode(Response.Status.BAD_REQUEST.getStatusCode());
					error.setInternalMessage("Cannot use global tag name "+ globaltagname + " for tracing ");
					ex.setStatus(Response.Status.BAD_REQUEST);
					ex.setErrMessage(error);
					throw ex;
				}
				Collection<GlobalTag> gtaglist = CollectionUtils.iterableToCollection(this.globalTagService.getGlobalTagByNameLike(globaltagname));
				CollectionResource collres = listToCollection(gtaglist, false, info);
				return created(collres);
			} else {
				GlobalTag gtag = null;
				if (trace.equals("off")) {
					gtag = this.globalTagService.getGlobalTag(globaltagname);
				} else {
					gtag = this.globalTagService.getGlobalTagFetchTags(globaltagname);
				}
				gtag.setResId(gtag.getName());
				GlobalTagResource gtagres = (GlobalTagResource) springResourceFactory.getResource("globaltag", info, gtag);
				return created(gtagres);
			}
		} catch (ConddbServiceException e) {
			ErrorMessage error = new ErrorMessage("Error retrieving globaltag resource ");
			error.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			error.setInternalMessage("Cannot creating an globaltag resource :"+e.getMessage());
			ex.setStatus(Response.Status.INTERNAL_SERVER_ERROR);
			ex.setErrMessage(error);
			throw ex;
		}
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
