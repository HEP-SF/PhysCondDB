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
@Path(Link.TAGS)
@Controller
public class TagRestController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{tagname}")
	public Response getTag(
			@PathParam("tagname") final String tagname,
			@DefaultValue("off") @QueryParam("trace") final String trace) throws ConddbWebException {
		this.log.info("TagRestController processing request for get tag name"
				+ tagname);
		Response resp = null;
		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(60);
			
			if (tagname.contains("%")) {
				if (trace.equals("on")) {
					// Do not want to trace the full tag list
					throw new ConddbWebException("Cannot activate trace on generic tag pattern");
				}
				List<Tag> entitylist = this.globalTagService.getTagByNameLike(tagname);
				resp = Response.ok(entitylist).header("size", entitylist.size()).cacheControl(control).build();
			} else {
				if (trace.equals("off")) {
					Tag entity = this.globalTagService.getTag(tagname);
					resp = Response.ok(entity).cacheControl(control).build();
				} else {
					Tag entity = this.globalTagService.getTagFetchGlobalTags(tagname);
					resp = Response.ok(entity).cacheControl(control).build();
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
            @DefaultValue("false") @QueryParam("expand") boolean expand,
            @DefaultValue("0") @QueryParam("page") Integer ipage, 
            @DefaultValue("25") @QueryParam("size") Integer size) throws ConddbWebException {
		this.log.info("TagRestController processing request for tag list (expansion = "
				+ expand+")");
		Collection<Tag> tags;
		try {
			// Here we could implement pagination
			PageRequest preq = new PageRequest(ipage,size);
			tags = CollectionUtils.iterableToCollection(globalTagService.findAllTags(preq));
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (tags == null || tags.size() == 0) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGS, Collections.emptyList());
        }
        Collection items = new ArrayList(tags.size());
        for( Tag tag : tags) {
        	tag.setResId(tag.getName());
            if (expand) {
                items.add(springResourceFactory.getResource("tag", info, tag));
            } else {
                items.add(springResourceFactory.getResource("link",info,tag));
            }
        }
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGS, items);
	}

}
