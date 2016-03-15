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

import conddb.data.GlobalTag;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.utils.collections.CollectionUtils;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.TagResource;
import conddb.web.resources.generic.GenericPojoResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
	@ApiOperation(value = "Finds Tags by name",
    notes = "Usage of % allows to select based on patterns",
    response = Tag.class,
    responseContainer = "List")
	public Response getTag(@Context UriInfo info, 
			@ApiParam(value = "name pattern for the search", required = true)
			@PathParam("tagname") final String tagname,
			@ApiParam(value = "trace {off|on} allows to retrieve associated global tags", required = false)
			@DefaultValue("off") @QueryParam("trace") final String trace,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
			@DefaultValue("true") @QueryParam("expand") final boolean expand) throws ConddbWebException {

		this.log.info("TagRestController processing request for getting tag name " + tagname);

		Response result = doTask(tagname, expand, trace, info);
		return result;	
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all Tags",
    notes = "Retrieval is implemented via pagination",
    response = Tag.class,
    responseContainer = "List")
	public Response listTags(@Context UriInfo info, 
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
			@DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "page: page number for the query, defaults to 0", required = false)
			@DefaultValue("0") @QueryParam("page") Integer ipage, 
			@ApiParam(value = "size: size of the page, defaults to 25", required = false)
			@DefaultValue("25") @QueryParam("size") Integer size)
					throws ConddbWebException {
		this.log.info("TagRestController processing request for tag list (expansion = " + expand + ")");

		PageRequest preq = new PageRequest(ipage, size);
		Collection<Tag> tags = getTagList(null,preq);
		if (tags == null || tags.size() == 0) {
			String msg = "Empty tags collection";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
		}
		CollectionResource resource = listToCollection(tags, expand, info);
		return ok(resource);
	}

	protected Response doTask(String tagname, boolean expand, String trace, UriInfo info) throws ConddbWebException {
		Response result = Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
		if (tagname.contains("%")) {
			Collection<Tag> taglist = getTagList(tagname, null);
			if (taglist == null) {
				String msg = "Empty tags collection";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			CollectionResource collres = listToCollection(taglist, expand, info);
			result = ok(collres);
		} else {
			Tag entity = getTag(tagname, trace);
			if (entity == null) {
				String msg = "Tag "+tagname+" not found.";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			GenericPojoResource<Tag> resource = (GenericPojoResource) springResourceFactory.getResource("generic-tag", info,
					entity);
			result = ok(resource);
		}
		return result;
	}

	protected Collection<Tag> getTagList(String tagname, PageRequest preq) {
		Collection<Tag> taglist = null;
		try {
			if (tagname == null) {
				taglist = CollectionUtils.iterableToCollection(globalTagService.findAllTags(preq));
			} else {
				taglist = CollectionUtils.iterableToCollection(globalTagService.getTagByNameLike(tagname));
			}
		} catch (ConddbServiceException e) {
			log.error("Cannot retrieve global tag list for pattern " + tagname);
		}
		return taglist;
	}

	protected Tag getTag(String tagname, String trace) {
		Tag entity = null;
		try {
			if (trace.equals("off")) {
				log.debug("Search for a tag " + tagname);
				entity = this.globalTagService.getTag(tagname);
			} else {
				log.debug("Search for a tag " + tagname + " and associated global tags...");
				entity = this.globalTagService.getTagFetchGlobalTags(tagname);
				log.debug("Retrieved tag entity : ");
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


	protected CollectionResource listToCollection(Collection<Tag> tags, boolean expand, UriInfo info) {
		Collection items = new ArrayList(tags.size());
		for (Tag tag : tags) {
			tag.setResId(tag.getName());
			if (expand) {
				GenericPojoResource<Tag> resource = (GenericPojoResource<Tag>) springResourceFactory.getGenericResource(info, tag, 1, null);
				items.add(resource);
			} else {
				items.add(springResourceFactory.getResource("link", info, tag));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.TAGS, items);
	}

}
