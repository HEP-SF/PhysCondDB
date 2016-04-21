/**
 * 
 */
package conddb.web.controllers;

import java.util.Collection;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Controller;

import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.specifications.GenericSpecBuilder;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SwaggerTagCollection;
import conddb.web.resources.generic.GenericPojoResource;
import conddb.web.utils.PropertyConfigurator;
import conddb.web.utils.collections.CollectionUtils;
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

	private String QRY_PATTERN = PropertyConfigurator.getInstance().getQrypattern();

	@Autowired
	private GlobalTagService globalTagService;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{name}")
	@ApiOperation(value = "Finds a Tag by name", notes = "This method will search for a tag with the given name. Only one tag should be returned."
			+ "Set <trace> parameter to (de)activate tracing, i.e. the retrieval of the associated global tags. ", response = Tag.class)
	public Response findTag(@Context UriInfo info,
			@ApiParam(value = "name of the tag", required = true) @PathParam("name") final String tagname,
			@ApiParam(value = "trace {off|on} allows to retrieve associated global tags", required = false) @DefaultValue("off") @QueryParam("trace") final String trace,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") final boolean expand)
			throws ConddbWebException {

		this.log.info("TagRestController processing request for getting tag name " + tagname);
		try {
			Tag entity = getTag(tagname, trace);
			if (entity == null) {
				String msg = "Tag " + tagname + " not found.";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			GenericPojoResource<Tag> resource = new GenericPojoResource<Tag>(info, entity, 2, null);
			return ok(resource);
		} catch (ConddbWebException e1) {
			throw e1;
		} catch (Exception e) {
			String msg = "Error retrieving Tag resource ";
			throw buildException(msg + ": " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "List Tags", notes = "Provide the <by> parameter to filter the list using comma separated list of conditions. "
			+ "The syntax is : by=<param-name><operation><param-value> "
			+ "      <param-name> is the name of one of the fields returned in the output json "
			+ "      <operation> can be [< : >] ; for string use only [:]  "
			+ "      <param-value> depends on the chosen parameter. "
			+ "Set the page number and page size parameters to use pagination. ", response = SwaggerTagCollection.class)
	public Response listTags(@Context UriInfo info,
			@ApiParam(value = "page: the page number {0}", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size {1000}", required = false) @DefaultValue("1000") @QueryParam("size") Integer size,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") boolean expand,
			@ApiParam(value = "by", required = true) @DefaultValue("name:") @QueryParam("by") final String patternsearch)
			throws ConddbWebException {
		log.info("TagRestController processing request for tag list (expansion = " + expand + ") (search = "
				+ patternsearch + " )");
		try {
			Page<Tag> entitylist = null;
			PageRequest preq = new PageRequest(ipage, size);

			GenericSpecBuilder<Tag> builder = new GenericSpecBuilder<>();
			String patternstr = QRY_PATTERN;

			Pattern pattern = Pattern.compile(patternstr);
			Matcher matcher = pattern.matcher(patternsearch + ",");
			while (matcher.find()) {
				builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
			}

			Specification<Tag> spec = builder.build();
			entitylist = globalTagService.getTagRepository().findAll(spec, preq);
			if (entitylist == null || entitylist.getContent().size() == 0) {
				String msg = "Empty tags collection";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			Collection<Tag> entitycoll = CollectionUtils.iterableToCollection(entitylist.getContent());
			CollectionResource collres = listToCollection(entitycoll, expand, info, Link.TAGS, 0, ipage, size);
			return ok(collres);
		} catch (ConddbWebException e1) {
			throw e1;
		} catch (Exception e) {
			String msg = "Error retrieving Tag resource ";
			throw buildException(msg + ": " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
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

}
