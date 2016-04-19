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
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTagMap;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.specifications.GenericSpecBuilder;
import conddb.svc.dao.specifications.MappingsJoinSpecifications;
import conddb.svc.dao.specifications.MappingsSpecBuilder;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.CollectionResource;
import conddb.web.resources.Link;
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

	private String QRY_PATTERN = PropertyConfigurator.getInstance().getQrypattern();


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
			GenericPojoResource<GlobalTagMap> resource = new GenericPojoResource<GlobalTagMap>(info, entity, 1, null);
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
	@ApiOperation(value = "Finds all GlobalTagMaps", notes = "Usage of url argument expand={true|false} in order to see full resource content or href links only", response = GlobalTagMap.class, responseContainer = "List")
	public Response listGlobalTagMaps(@Context UriInfo info,
			@ApiParam(value = "page: the page number {0}", required = false) @DefaultValue("0") @QueryParam("page") Integer ipage,
			@ApiParam(value = "size: the page size {1000}", required = false) @DefaultValue("1000") @QueryParam("size") Integer size,
			@ApiParam(value = "expand {true|false} is for parameter expansion", required = false) @DefaultValue("true") @QueryParam("expand") boolean expand,
			@ApiParam(value = "by", required = true) @DefaultValue("record:") @QueryParam("by") final String patternsearch,
			@ApiParam(value = "map", required = true) @DefaultValue("globalTag_name:,systemTag_name:") @QueryParam("map") final String patternmap)
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


//	protected CollectionResource listToCollection(Collection<GlobalTagMap> globaltagmaps, boolean expand, UriInfo info, Integer ipage, Integer size) {
//        Collection items = new ArrayList(globaltagmaps.size());
//        for( GlobalTagMap globaltagmap : globaltagmaps) {
//            if (expand) {
//                items.add(springResourceFactory.getResource("generic-gtmap", info, globaltagmap));
//            } else {
//                items.add(springResourceFactory.getResource("link",info,globaltagmap));
//            }
//        }
//        if (ipage == null || size ==null) {
//            return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items);
//        }
//        	
//        return (CollectionResource)springResourceFactory.getCollectionResource(info, Link.GLOBALTAGMAPS, items, ipage, size);
//	}

}
