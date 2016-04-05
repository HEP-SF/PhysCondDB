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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTag;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.controllers.SystemNodeService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.utils.collections.CollectionUtils;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.generic.GenericPojoResource;
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

	@Autowired
	private SystemNodeService systemNodeService;
	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{tagname}")
	@ApiOperation(value = "Get a system description entry.", notes = "Use the input tagNameRoot string to search for information about a given system.", response = SystemDescription.class)
	public Response getSystemDescription(@Context UriInfo info,
			@ApiParam(value = "tagname: root name of the tag associated to the system. Regexp % can be used.", required = true) @PathParam("tagname") final String id,
			@ApiParam(value = "trace {off|on} allows to retrieve associated tags [not implemented]", required = false) @DefaultValue("off") @QueryParam("trace") final String trace,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") final boolean expand)
			throws ConddbWebException {

		this.log.info("SystemDescriptionRestController processing request to get system using id " + id);

		try {
			if (id.contains("%")) {
				Collection<SystemDescription> syslist = getSystemsList(id);
				if (syslist == null) {
					String msg = "Empty systems collection";
					throw buildException(msg, msg, Response.Status.NOT_FOUND);
				}
				CollectionResource collres = listToCollection(syslist, expand, info);
				return ok(collres);
			} else {
				SystemDescription entity = this.systemNodeService.getSystemNodesByTagname(id);
				GenericPojoResource<SystemDescription> resource = (GenericPojoResource<SystemDescription>) springResourceFactory
						.getGenericResource(info, entity, 1, null);
				return ok(resource);
			}
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving system resource ";
			throw buildException(msg + " " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/find")
	@ApiOperation(value = "Find a system description entry using search on tag, node or schema name.", notes = "Use the input string to search for information about a given system.", response = SystemDescription.class)
	public Response findSystemDescription(@Context UriInfo info,
			@ApiParam(value = "name pattern for the search", required = true) @DefaultValue("none") @QueryParam("name") final String system,
			@ApiParam(value = "trace {off|on} allows to retrieve systems.", required = false) @DefaultValue("off") @QueryParam("trace") String trace,
			@ApiParam(value = "Select the search field : tag, node or schema.", required = true) @DefaultValue("tag") @QueryParam("by") String type,
			@ApiParam(value = "page: the page number {0}", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size {1000}", required = false) @DefaultValue("1000") @QueryParam("size") Integer size)
			throws ConddbWebException {

		this.log.info("SystemDescriptionRestController processing request to get systems using name " + system
				+ " as type " + type);

		try {
			if (system.equals("none")) {
				String msg = "Wrong options; 'by' field is mandatory [tag|node|schema] ";
				throw buildException(msg, msg, Response.Status.BAD_REQUEST);
			}

			PageRequest preq = new PageRequest(ipage, size);
			if (system.contains("%")) {
				List<SystemDescription> sdlist = null;
				Page<SystemDescription> pagelist = null;
				if (type.equals("tag")) {
					pagelist = this.systemNodeService.findSystemNodesByTagnameLike(system, preq);
				} else if (type.equals("node")) {
					pagelist = this.systemNodeService.findSystemNodesByNodeFullpathLike(system, preq);
				} else if (type.equals("schema")) {
					pagelist = this.systemNodeService.findSystemNodesBySchemaNameLike(system, preq);
				} else {
					String msg = "Wrong options; 'by' field should be one of [tag|node|schema] ";
					throw buildException(msg, msg, Response.Status.BAD_REQUEST);
				}
				if (pagelist == null) {
					log.debug("Controller could not retrieve any entity list....");
					String msg = "Cannot find system using by=" + type;
					throw buildException(msg, msg, Response.Status.NOT_FOUND);
				}
				log.debug("Retrieved list of systems " + pagelist.getNumberOfElements());
				sdlist = pagelist.getContent();
				Collection<SystemDescription> syslist = CollectionUtils.iterableToCollection(sdlist);
				CollectionResource resource = listToCollection(syslist, true, info);
				return ok(resource);
				
			} else {
				SystemDescription entity = null;
				if (type.equals("tag")) {
					SystemDescription sd = this.systemNodeService.getSystemNodesByTagname(system);
					entity = sd;
				} else if (type.equals("node")) {
					SystemDescription sd = this.systemNodeService.getSystemNodesByNodeFullpath(system);
					entity = sd;
				}
				if (entity == null) {
					log.debug("Controller could not retrieve any entity....");
					String msg = "Cannot find system using by=" + system;
					throw buildException(msg, msg, Response.Status.NOT_FOUND);
				}
				if (trace.equals("on")) {
					List<Tag> tags = this.globalTagService.getTagByNameLike(entity.getTagNameRoot() + "%");
					entity.setTags(tags);
				}
				log.debug("Controller creating resource from entity...." + entity);
				GenericPojoResource<SystemDescription> resource = (GenericPojoResource<SystemDescription>) springResourceFactory
						.getGenericResource(info, entity, 1, null);

				return ok(resource);
			}
		} catch (ConddbServiceException e) {
			log.debug("Generate exception using an ConddbService exception..." + e.getMessage());
			String msg = "Error updating association resource: internal server exception !";
			throw buildException(msg + " \n " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@SuppressWarnings("unchecked")
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Retrieve the full system description list.", notes = "Retrieve all systems in the DB.", response = SystemDescription.class)
	public Response list(@Context UriInfo info,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("false") @QueryParam("expand") boolean expand)
			throws ConddbWebException {
		this.log.info(
				"SystemDescriptionRestController processing request for system list (expansion = " + expand + ")");
		Collection<SystemDescription> systems = null;
		// Here we could implement pagination
		systems = getSystemsList(null);
		if (systems == null || systems.size() == 0) {
			String msg = "Empty systems collection";
			throw buildException(msg, msg, Response.Status.NOT_FOUND);
		}
		CollectionResource resource = listToCollection(systems, expand, info);
		return ok(resource);
	}

	protected Collection<SystemDescription> getSystemsList(String tagnameroot) {
		Collection<SystemDescription> syslist = null;
		try {
			if (tagnameroot == null) {
				syslist = CollectionUtils.iterableToCollection(systemNodeService.findAllSystemNodes());
			} else {
				syslist = CollectionUtils
						.iterableToCollection(systemNodeService.findSystemNodesByTagNameRootLike(tagnameroot));
			}
		} catch (ConddbServiceException e) {
			log.error("Cannot retrieve system list for tag name root pattern " + tagnameroot);
		}
		return syslist;
	}

	protected CollectionResource listToCollection(Collection<SystemDescription> systems, boolean expand, UriInfo info) {
		Collection items = new ArrayList(systems.size());
		for (SystemDescription system : systems) {
			system.setResId(system.getTagNameRoot());
			if (expand) {
				GenericPojoResource<SystemDescription> resource = (GenericPojoResource<SystemDescription>) springResourceFactory
						.getGenericResource(info, system, 0, null);
				items.add(resource);
			} else {
				items.add(springResourceFactory.getResource("link", info, system));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.SYSTEMS, items);
	}

}
