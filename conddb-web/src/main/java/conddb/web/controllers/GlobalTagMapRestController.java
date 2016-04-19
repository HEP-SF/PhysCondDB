/**
 * 
 */
package conddb.web.controllers;

import java.util.ArrayList;
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
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.svc.dao.specifications.GenericSpecBuilder;
import conddb.svc.dao.specifications.MappingsJoinSpecifications;
import conddb.svc.dao.specifications.MappingsSpecBuilder;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
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
@Path(Link.GLOBALTAGMAPS)
@Controller
@Api(value = Link.GLOBALTAGMAPS)
public class GlobalTagMapRestController  extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;
	private String QRY_PATTERN = PropertyConfigurator.getInstance().getQrypattern();

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/trace")
	@ApiOperation(value = "Finds Tags to GlobalTag's mappings by name either of the tag or of the global tag",
    notes = "Usage of % allows to select based on patterns",
    response = CollectionResource.class,
    responseContainer = "List")
	public Response getGlobalTagMapTrace(
			@Context UriInfo info,
			@ApiParam(value = "type: {globaltag|tag}", required = true)
			@DefaultValue("globaltag") @QueryParam("type") final String type,
			@ApiParam(value = "id: the name either of the tag or the global tag, patterns not allowed", required = true)
			@DefaultValue("none") @QueryParam("id") final String name,
			@DefaultValue("true") @QueryParam("expand") final boolean expand
			) throws ConddbWebException {
		this.log.info("GlobalTagMapRestController processing request for type "
				+ type + " and name "+name);
		try {
			List<GlobalTagMap> list  = null;
			if (type.equals("globaltag")) {
				list = this.globalTagService.getGlobalTagMapByGlobalTagName(name);
				log.debug("Controller has executed query for globaltag search...");
			} else if (type.equals("tag")) {
				list = this.globalTagService.getGlobalTagMapByTagName(name);
				log.debug("Controller has executed query for tag search...");
			} else {
				String msg = "Error in input arguments: [type] should be either globaltag or tag ! ";
				throw buildException(msg, msg, Response.Status.BAD_REQUEST);
			}
			if (list == null || list.isEmpty()) {
				String msg = "Associations not found for "+type+" and id "+name;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			log.debug("Controller has retrieved a list of size "+list.size());
			Collection<GlobalTagMap> globaltagmaps = CollectionUtils.iterableToCollection(list);
			CollectionResource collres = listToCollection(globaltagmaps, expand, info,null,null);
			return created(collres);
			
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving association resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/find")
	@ApiOperation(value = "Finds Tags to GlobalTag's mappings by name  of the tag and of the global tag",
    notes = "Usage of % is forbidden, names should be complete, only one object should be returned",
    response = CollectionResource.class,
    responseContainer = "List")
	public Response getGlobalTagMap(
			@Context UriInfo info,
			@ApiParam(value = "globaltag: the global tag name, patterns not allowed", required = true)
			@DefaultValue("none") @QueryParam("globaltag") final String globaltagname,
			@ApiParam(value = "tag: the tag name, patterns not allowed", required = true)
			@DefaultValue("none") @QueryParam("tag") final String tagname,
			@DefaultValue("true") @QueryParam("expand") final boolean expand
			) throws ConddbWebException {

		this.log.info("GlobalTagMapRestController processing request for global tag "
				+ globaltagname + " and tag "+tagname);
		try {
			GlobalTagMap entity = null;
			if (!globaltagname.contains("%") && !tagname.contains("%")) {
				entity = this.globalTagService.getGlobalTagMapByTagAndGlobalTag(globaltagname, tagname);
				log.debug("Controller has executed query for globaltag search...");
			} else {
				String msg = "Error in input arguments: globaltag or tag names contain pattern string !";
				throw buildException(msg, msg, Response.Status.BAD_REQUEST);
			}
			if (entity == null) {
				String msg = "Associations not found for "+globaltagname+" and "+tagname;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			log.debug("Controller has retrieved one map object "+entity);
			
			GenericPojoResource<GlobalTagMap> resource = (GenericPojoResource) springResourceFactory.getResource("generic-gtmap", info,
					entity);
			return ok(resource);
			
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving association resource ";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@Path("/{id}")
	@ApiOperation(value = "Finds Tags to GlobalTag's mappings by id (integer)",
    notes = "This is ment essentially for internal usage when finding dependencies",
    response = GlobalTagMap.class)
	public Response getGlobalTagMapById(
			@Context UriInfo info,
			@ApiParam(value = "id: id of the association", required = true)
			@PathParam("id") Long id) throws ConddbWebException {
		
		this.log.info("GlobalTagMapRestController processing request to retrieve map with id "+ id);

		try {
			GlobalTagMap entity = null;
			entity = this.globalTagService.getGlobalTagMapFetchChildren(id);
			if (entity == null) {
				String msg = "Associations not found for id "+id;
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
			log.debug("Creating resource from retrieved entity "+entity);
			GenericPojoResource<GlobalTagMap> resource = (GenericPojoResource) springResourceFactory.getResource("generic-gtmap", info,
					entity);

			return ok(resource);
		} catch (ConddbServiceException e) {
			String msg = "Error retrieving association resource for id "+id;
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

//	@GET
//	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
//	@ApiOperation(value = "Finds all Tags to GlobalTag's mappings",
//    notes = "Usage of pagination and parameter expand to get full output of the association or link only",
//    response = CollectionResource.class,
//    responseContainer = "List")
//	public Response listGlobalTagMaps(
//			@Context UriInfo info,
//			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false)
//            @DefaultValue("false") @QueryParam("expand") boolean expand,
//			@ApiParam(value = "page: page number for the query, defaults to 0", required = false)
//            @DefaultValue("0") @QueryParam("page") Integer ipage, 
//			@ApiParam(value = "size: size of the page, defaults to 25", required = false)
//            @DefaultValue("25") @QueryParam("size") Integer size) throws ConddbWebException {
//		this.log.info("GlobalTagMapRestController processing request for global tag 2 tag mapping list (expansion = "
//				+ expand+")");
//		Collection<GlobalTagMap> globaltagmaps;
//		try {
//			// Here we could implement pagination
//			PageRequest preq = new PageRequest(ipage,size);
//			globaltagmaps = CollectionUtils.iterableToCollection(globalTagService.findAllGlobalTagMaps(preq));
//		} catch (ConddbServiceException e) {
//			String msg = "Associations not found, internal error";
//			throw buildException(msg, e.getMessage(), Response.Status.NOT_FOUND);
//		}
//		if (globaltagmaps == null || globaltagmaps.size() == 0) {
//			String msg = "Associations not found";
//			throw buildException(msg, msg, Response.Status.NOT_FOUND);
//        }
//		CollectionResource resource = listToCollection(globaltagmaps, expand, info, ipage, size);
//		return ok(resource);
//
//	}
	
	@GET
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Finds all GlobalTagMaps", notes = "Usage of url argument expand={true|false} in order to see full resource content or href links only", response = GlobalTagMap.class, responseContainer = "List")
	public Response listGlobalTagMaps(@Context UriInfo info,
			@ApiParam(value = "page: the page number {0}", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size {1000}", required = false) @DefaultValue("1000") @QueryParam("size") Integer size,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") boolean expand,
			@ApiParam(value = "by", required = true) @DefaultValue("record:") @QueryParam("by") final String patternsearch,
			@ApiParam(value = "map", required = true) @DefaultValue("globalTag.name:,systemTag.name:") @QueryParam("map") final String patternmap)
					throws ConddbWebException {
		log.info("GlobalTagMapRestController processing request for global tag list (expansion = " + expand + ") (search = "+patternsearch+" )");
		try {
			List<GlobalTagMap> entitylist = null;
			// TODO: the pagination does not work with JOIN
//			PageRequest preq = new PageRequest(ipage, size);

			GenericSpecBuilder<GlobalTagMap> builder = new GenericSpecBuilder<>();
			String patternstr = QRY_PATTERN;

			Pattern pattern = Pattern.compile(patternstr);
			Matcher matcher = pattern.matcher(patternsearch + ",");
			while (matcher.find()) {
				builder.with(matcher.group(1), matcher.group(2), matcher.group(3));
			}
			Specification<GlobalTagMap> spec = builder.build();
			////////////////
	        Specification<GlobalTagMap> result = spec;
			MappingsSpecBuilder<GlobalTagMap> builderjoin = new MappingsSpecBuilder<>();
			String patternstrjoin = QRY_PATTERN;
			log.debug("Mappings builder for join created "+builderjoin);
			
			Pattern patternjoin = Pattern.compile(patternstrjoin);
			Matcher matcherjoin = patternjoin.matcher(patternmap + ",");
			while (matcherjoin.find()) {
				log.debug("Found matching gorup "+matcherjoin.group(1)+" "+matcherjoin.group(3));
				builderjoin.with(matcherjoin.group(1), matcherjoin.group(2), matcherjoin.group(3));
			}
			Specification<GlobalTagMap> specjoin = builderjoin.build();
			result = Specifications.where(specjoin).and(result);
			log.debug("Specifications : "+result.toString());
			///////////////
	        result = Specifications.where(new MappingsJoinSpecifications<GlobalTagMap>()).and(spec).and(specjoin);

			
	    	entitylist = globalTagService.getGlobalTagMapRepository().findAll(result);
	    	if (entitylist == null || entitylist.size() == 0) {
				String msg = "Empty globaltagmaps collection";
				throw buildException(msg, msg, Response.Status.NOT_FOUND);
			}
//	    	if (entitylist == null || entitylist.getContent().size() == 0) {
//				String msg = "Empty globaltagmaps collection";
//				throw buildException(msg, msg, Response.Status.NOT_FOUND);
//			}
			Collection<GlobalTagMap> entitycoll = CollectionUtils.iterableToCollection(entitylist);
			CollectionResource collres = listToCollection(entitycoll, expand, info, Link.GLOBALTAGMAPS,1);
			return ok(collres);
		} catch (ConddbWebException e1) {
			throw e1;
		} catch (Exception e) {
			String msg = "Error retrieving GlobalTagMap resource ";
			throw buildException(msg + ": " + e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}


	protected CollectionResource listToCollection(Collection<GlobalTagMap> globaltagmaps, boolean expand, UriInfo info, Integer ipage, Integer size) {
        Collection items = new ArrayList(globaltagmaps.size());
        for( GlobalTagMap globaltagmap : globaltagmaps) {
            if (expand) {
                items.add(springResourceFactory.getResource("generic-gtmap", info, globaltagmap));
            } else {
                items.add(springResourceFactory.getResource("link",info,globaltagmap));
            }
        }
        if (ipage == null || size ==null) {
            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items);
        }
        	
        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items, ipage, size);
	}

}
