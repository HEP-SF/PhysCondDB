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

import conddb.data.GlobalTag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.specifications.GenericSpecBuilder;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.SwaggerGlobalTagCollection;
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
@Path(Link.GLOBALTAGS)
@Controller
@Api(value = Link.GLOBALTAGS)
public class GlobalTagRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String QRY_PATTERN = PropertyConfigurator.getInstance().getQrypattern();

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{name}")
	@ApiOperation(value = "Finds a GlobalTag by name", notes = "This method will search for a global tag with the given name. Only one global tag should be returned."
			+ "Set <trace> parameter to (de)activate tracing, i.e. the retrieval of the associated tags. "
			+ "Set <filter> parameter retrieve the associated tags matching a given patter.", response = GlobalTag.class)
	public Response findGlobalTag(@Context UriInfo info,
			@ApiParam(value = "name of the global tag", required = true) @PathParam("name") final String globaltagname,
			@ApiParam(value = "trace {off|on} allows to retrieve associated tags", required = false) @DefaultValue("on") @QueryParam("trace") final String trace,
			@ApiParam(value = "filter {tag name pattern} allows to retrieve associated tags matching pattern", required = false) @DefaultValue("none") @QueryParam("filter") final String filter,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") final boolean expand)
			throws ConddbWebException {
		this.log.info("GlobalTagRestController processing request for global tag name " + globaltagname);
		try {
			GlobalTag entity = getGlobalTag(globaltagname, trace, filter);
			if (entity == null) {
				String msg = "Global Tag " + globaltagname + " not found.";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			this.log.info("GlobalTagRestController generates resource for " + globaltagname);
			GenericPojoResource<GlobalTag> resource = (GenericPojoResource<GlobalTag>) springResourceFactory.getGenericResource(info, entity, 2, null);
					//new GenericPojoResource<GlobalTag>(info, entity, 2, null);
			return ok(resource);
		} catch (ConddbWebException e1) {
			throw e1;
		} catch (Exception e) {
			String msg = "Error retrieving GlobalTag resource ";
			throw buildException(msg + ": " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "List GlobalTags", notes = "Provide the <by> parameter to filter the list using comma separated list of conditions. "
			+ "The syntax is : by=<param-name><operation><param-value> "
			+ "      <param-name> is the name of one of the fields returned in the output json "
			+ "      <operation> can be [< : >] ; for string use only [:]  "
			+ "      <param-value> depends on the chosen parameter. "
			+ "Set the page number and page size parameters to use pagination. ", response = SwaggerGlobalTagCollection.class)
	public Response listGlobalTags(@Context UriInfo info,
			@ApiParam(value = "page: the page number {0}", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size {1000}", required = false) @DefaultValue("1000") @QueryParam("size") Integer size,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") boolean expand,
			@ApiParam(value = "by", required = true) @DefaultValue("name:%") @QueryParam("by") final String patternsearch)
			throws ConddbWebException {
		log.info("GlobalTagRestController processing request for global tag list (expansion = " + expand
				+ ") (search = " + patternsearch + " )");
		try {
			Page<GlobalTag> entitylist = null;
			PageRequest preq = new PageRequest(ipage, size);

			GenericSpecBuilder<GlobalTag> builder = new GenericSpecBuilder<>();
			String patternstr = QRY_PATTERN;

			Pattern pattern = Pattern.compile(patternstr);
			Matcher matcher = pattern.matcher(patternsearch + ",");
			while (matcher.find()) {
				builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
			}

			Specification<GlobalTag> spec = builder.build();
			entitylist = globalTagService.getGlobalTagRepository().findAll(spec, preq);
			if (entitylist == null || entitylist.getContent().size() == 0) {
				String msg = "Empty globaltags collection";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			Collection<GlobalTag> entitycoll = CollectionUtils.iterableToCollection(entitylist.getContent());
			CollectionResource collres = springResourceFactory.listToCollection(entitycoll, expand, info, Link.GLOBALTAGS, 0, ipage, size);
					//listToCollection(entitycoll, expand, info, Link.GLOBALTAGS, 0, ipage, size);
			return ok(collres);
		} catch (ConddbWebException e1) {
			throw e1;
		} catch (Exception e) {
			String msg = "Error retrieving GlobalTag resource ";
			throw buildException(msg + ": " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	protected GlobalTag getGlobalTag(String globaltagname, String trace, String tagfilter) {
		GlobalTag entity = null;
		try {
			if (trace.equals("off")) {
				log.debug("Search for a globaltag " + globaltagname);
				entity = globalTagService.getGlobalTag(globaltagname);
			} else {
				log.debug("Search for a globaltag " + globaltagname + " and associated tags...");
				if (tagfilter == null || tagfilter.equals("none")) {
					entity = globalTagService.getGlobalTagFetchTags(globaltagname);
				} else {
					entity = globalTagService.getGlobalTagFilterTags(globaltagname, "%" + tagfilter + "%");
				}
				log.debug("Retrieved globaltag entity : ");
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
