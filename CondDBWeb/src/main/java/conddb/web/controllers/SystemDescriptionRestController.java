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

import conddb.dao.controllers.GlobalTagService;
import conddb.dao.controllers.SystemNodeService;
import conddb.dao.exceptions.ConddbServiceException;
import conddb.data.GlobalTag;
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.utils.collections.CollectionUtils;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.SystemDescriptionResource;

/**
 * @author aformic
 *
 */
@Path(Link.SYSTEMS)
@Controller
public class SystemDescriptionRestController {

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
	public Response getSystemDescription(@PathParam("tagname") final String id,
			@DefaultValue("off") @QueryParam("trace") final String trace) throws ConddbWebException {
		this.log.info("SystemDescriptionRestController processing request to get system using id " + id);
		Response resp = null;
		try {
			if (id.contains("%")) {
				List<SystemDescription> gtaglist = this.systemNodeService.findSystemNodesByTagNameRootLike(id);
				resp = Response.ok(gtaglist).build();
			} else {
				SystemDescription node = this.systemNodeService.getSystemNodesByTagname(id);
				resp = Response.ok(node).build();
			}
		} catch (Exception e) {
			resp = Response.status(Response.Status.NOT_FOUND).build();
			throw new ConddbWebException(e.getMessage());
		}
		return resp;
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/find")
	public Response findSystemDescription(@Context UriInfo info,
			@DefaultValue("none") @QueryParam("name") final String system,
			@DefaultValue("off") @QueryParam("trace") String trace, @DefaultValue("tag") @QueryParam("by") String type,
			@DefaultValue("0") @QueryParam("page") Integer ipage,
			@DefaultValue("1000") @QueryParam("size") Integer size) throws ConddbWebException {
		this.log.info("SystemDescriptionRestController processing request to get systems using name " + system
				+ " as type " + type);
		Response resp = null;
		try {
			CacheControl control = new CacheControl();
			control.setMaxAge(600);

			if (system.equals("none")) {
				String help = "{ 'options' : [ { 'by': 'tag,node,schema' }, { 'name' : 'a string representing the search you want to perform' } ]}";
				resp = Response.status(Response.Status.BAD_REQUEST).entity(help).build();
				return resp;
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
				}
				log.debug("Retrieved list of systems " + pagelist.getNumberOfElements());
				sdlist = pagelist.getContent();
				resp = Response.ok(sdlist).cacheControl(control).build();
			} else {
				if (trace.equals("off")) {
					SystemDescription entity = null;
					if (type.equals("tag")) {
						SystemDescription sd = this.systemNodeService.getSystemNodesByTagname(system);
						entity = sd;
					} else if (type.equals("node")) {
						SystemDescription sd = this.systemNodeService.getSystemNodesByNodeFullpath(system);
						entity = sd;
					}
					SystemDescriptionResource resource = (SystemDescriptionResource) springResourceFactory
							.getResource("system", info, entity);
					resp = Response.ok(resource).cacheControl(control).build();
				} else {
					SystemDescription entity = null;
					if (type.equals("tag")) {
						SystemDescription sd = this.systemNodeService.getSystemNodesByTagname(system);
						List<Tag> tags = this.globalTagService.getTagByNameLike(sd.getTagNameRoot() + "%");
						sd.setTags(tags);
						entity = sd;
					} else if (type.equals("node")) {
						SystemDescription sd = this.systemNodeService.getSystemNodesByNodeFullpath(system);
						List<Tag> tags = this.globalTagService.getTagByNameLike(sd.getTagNameRoot() + "%");
						sd.setTags(tags);
						entity = sd;
					}
					SystemDescriptionResource resource = (SystemDescriptionResource) springResourceFactory
							.getResource("system", info, entity);
					resp = Response.ok(resource).cacheControl(control).build();
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
	public CollectionResource list(@Context UriInfo info, @DefaultValue("false") @QueryParam("expand") boolean expand)
			throws ConddbWebException {
		this.log.info(
				"SystemDescriptionRestController processing request for system list (expansion = " + expand + ")");
		Collection<SystemDescription> systems;
		try {
			// Here we could implement pagination
			systems = CollectionUtils.iterableToCollection(systemNodeService.findAllSystemNodes());
		} catch (ConddbServiceException e) {
			throw new ConddbWebException(e.getMessage());
		}
		if (systems == null || systems.size() == 0) {
			return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.SYSTEMS,
					Collections.emptyList());
		}
		Collection items = new ArrayList(systems.size());
		for (SystemDescription system : systems) {
			system.setResId(system.getTagNameRoot());
			if (expand) {
				items.add(springResourceFactory.getResource("system", info, system));
			} else {
				items.add(springResourceFactory.getResource("link", info, system));
			}
		}
		return (CollectionResource) springResourceFactory.getCollectionResource(info, Link.SYSTEMS, items);
	}

}
