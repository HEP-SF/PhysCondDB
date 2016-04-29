/**
 * 
 */
package conddb.web.controllers;

import java.util.Collection;
import java.util.List;
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

import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.specifications.GenericSpecBuilder;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.SwaggerSystemsCollection;
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
@Path(Link.SYSTEMS)
@Controller
@Api(value = Link.SYSTEMS)
public class SystemDescriptionRestController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private String QRY_PATTERN = PropertyConfigurator.getInstance().getQrypattern();

	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{tagname}")
	@ApiOperation(value = "Find a system description using the tag name root.", notes = "This method will search for a system with the given tag name root. Only one system should be returned."
			+ "Set <trace> parameter to (de)activate tracing, i.e. the retrieval of the associated tags. ", response = SystemDescription.class)
	public Response findSystem(@Context UriInfo info,
			@ApiParam(value = "tagname: root name of the tag associated to the system.", required = true) @PathParam("tagname") final String id,
			@ApiParam(value = "trace {off|on} allows to retrieve systems.", required = false) @DefaultValue("off") @QueryParam("trace") String trace,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") final boolean expand)
			throws ConddbWebException {

		this.log.info("SystemDescriptionRestController processing request to get system using id " + id);
		try {
			SystemDescription entity = systemNodeService.getSystemNodesByTagname(id);
			if (entity == null) {
				String msg = "System tagname " + id + " not found.";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			if (trace.equals("on")) {
				List<Tag> tags = this.globalTagService.getTagByNameLike(entity.getTagNameRoot() + "%");
				entity.setTags(tags);
			}
			GenericPojoResource<SystemDescription> resource = new GenericPojoResource<SystemDescription>(info, entity,
					1, null);
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
	@ApiOperation(value = "List Systems", notes = "Provide the <by> parameter to filter the list using comma separated list of conditions. "
			+ "The syntax is : by=<param-name><operation><param-value> "
			+ "      <param-name> is the name of one of the fields returned in the output json "
			+ "      <operation> can be [< : >] ; for string use only [:]  "
			+ "      <param-value> depends on the chosen parameter. "
			+ "Set the page number and page size parameters to use pagination. ", response = SwaggerSystemsCollection.class)
	public Response listSystems(@Context UriInfo info,
			@ApiParam(value = "page: the page number {0}", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size {1000}", required = false) @DefaultValue("1000") @QueryParam("size") Integer size,
			@ApiParam(value = "by", required = true) @DefaultValue("schemaName:") @QueryParam("by") final String patternsearch)
			throws ConddbWebException {

		this.log.info("SystemDescriptionRestController processing request to get systems using " + patternsearch);

		try {
			Page<SystemDescription> entitylist = null;
			PageRequest preq = new PageRequest(ipage, size);

			GenericSpecBuilder<SystemDescription> builder = new GenericSpecBuilder<>();
			String patternstr = QRY_PATTERN;

			Pattern pattern = Pattern.compile(patternstr);
			Matcher matcher = pattern.matcher(patternsearch + ",");
			while (matcher.find()) {
				builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
			}

			Specification<SystemDescription> spec = builder.build();
			entitylist = systemNodeService.getSystemNodeRepository().findAll(spec, preq);
			if (entitylist == null || entitylist.getContent().size() == 0) {
				String msg = "Empty systems collection";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			log.debug("Retrieved list of systems " + entitylist.getNumberOfElements());

			Collection<SystemDescription> entitycoll = CollectionUtils.iterableToCollection(entitylist.getContent());
			CollectionResource collres = springResourceFactory.listToCollection(entitycoll, true, info, Link.SYSTEMS, 0, ipage, size);
			return ok(collres);
		} catch (ConddbWebException e1) {
			throw e1;
		} catch (Exception e) {
			String msg = "Error retrieving systems resource ";
			throw buildException(msg + ": " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

}
