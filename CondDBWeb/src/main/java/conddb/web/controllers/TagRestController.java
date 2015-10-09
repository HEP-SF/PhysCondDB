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
import conddb.data.Tag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.TagResource;
import io.swagger.annotations.Api;

/**
 * @author aformic
 *
 */
@Path(Link.TAGS)
@Controller
@Api(value = Link.TAGS)
public class TagRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{tagname}")
	public Response getTag(@Context UriInfo info, @PathParam("tagname") final String tagname,
			@DefaultValue("off") @QueryParam("trace") final String trace,
			@DefaultValue("true") @QueryParam("expand") final boolean expand) throws ConddbWebException {

		this.log.info("TagRestController processing request for getting tag name" + tagname);

		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(60);

			if (tagname.contains("%")) {
				if (trace.equals("on")) {
					String msg = "Error in input arguments: [tag name] should be unique for tracing ! ";
					throw buildException(msg, msg, Response.Status.BAD_REQUEST);
				}
				List<Tag> entitylist = this.globalTagService.getTagByNameLike(tagname);
				Collection<Tag> taglist = CollectionUtils.iterableToCollection(entitylist);
				CollectionResource collres = listToCollection(taglist, expand, info);
				return created(collres);
			} else {
				Tag entity = null;
				if (trace.equals("off")) {
					entity = this.globalTagService.getTag(tagname);
				} else {
					entity = this.globalTagService.getTagFetchGlobalTags(tagname);
				}
				if (entity == null) {
					String msg = "Tag not found for id "+tagname;
					throw buildException(msg, msg, Response.Status.NOT_FOUND);
				}				
				log.debug("Creating resource....");
				entity.setResId(entity.getName());
				TagResource tagres = (TagResource) springResourceFactory.getResource("tag", info, entity);
				return created(tagres);
			}
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving tag resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	public CollectionResource list(@Context UriInfo info, @DefaultValue("false") @QueryParam("expand") boolean expand,
			@DefaultValue("0") @QueryParam("page") Integer ipage, @DefaultValue("25") @QueryParam("size") Integer size)
					throws ConddbWebException {
		this.log.info("TagRestController processing request for tag list (expansion = " + expand + ")");
		Collection<Tag> tags = null;
		try {
			// Here we could implement pagination
			PageRequest preq = new PageRequest(ipage, size);
			tags = CollectionUtils.iterableToCollection(globalTagService.findAllTags(preq));
		} catch (ConddbServiceException e) {
			String msg = "Error in creation of tags collection";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
		if (tags == null || tags.size() == 0) {
			return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.TAGS,
					Collections.emptyList());
		}
        return listToCollection(tags, expand, info);
	}

	protected CollectionResource listToCollection(Collection<Tag> tags, boolean expand, UriInfo info) {
		Collection items = new ArrayList(tags.size());
		for (Tag tag : tags) {
			tag.setResId(tag.getName());
			if (expand) {
				items.add(springResourceFactory.getResource("tag", info, tag));
			} else {
				items.add(springResourceFactory.getResource("link", info, tag));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.TAGS, items);
	}

}
