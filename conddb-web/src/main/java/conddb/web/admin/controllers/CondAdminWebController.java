package conddb.web.admin.controllers;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import conddb.data.GlobalTag;
import conddb.data.Tag;
import conddb.data.exceptions.ConversionException;
import conddb.svc.dao.controllers.GlobalTagExpertService;
import conddb.svc.dao.controllers.GlobalTagService;
import conddb.svc.dao.exceptions.ConddbServiceException;
import conddb.web.config.BaseController;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;
import conddb.web.resources.SpringResourceFactory;
import conddb.web.resources.generic.GenericPojoResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * @author formica
 *
 */
@Controller
@Path(Link.ADMIN)
@Api(value = Link.ADMIN)
public class CondAdminWebController extends BaseController {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private GlobalTagExpertService globalTagExpertService;
	@Autowired
	private GlobalTagService globalTagService;
	@Autowired
	private SpringResourceFactory springResourceFactory;

	@POST
	@Path(Link.GLOBALTAGS+"/clone")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Clone a GlobalTag.",
    notes = "Clone a source global tag into a destination global tag. It associates all tags from the source.",
    response=GlobalTag.class)
	public Response cloneGlobalTag(@Context UriInfo info,
			@ApiParam(value = "source: name of the globaltag to be cloned", required = true) 
			@QueryParam(value = "source") String sourcegtag,
			@ApiParam(value = "dest: name of the destination global tag", required = true) 
			@QueryParam(value = "dest") String destgtag) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for cloning " + sourcegtag + " into " + destgtag);
			this.globalTagExpertService.cloneGlobalTag(sourcegtag, destgtag);
			GlobalTag cloned = this.globalTagService.getGlobalTag(destgtag);
			GenericPojoResource<GlobalTag> resource = (GenericPojoResource) springResourceFactory.getGenericResource(info, cloned, 1, null);
			return created(resource);			
		} catch (ConddbServiceException e) {
			String msg = "Error cloning globaltag resource "+sourcegtag;
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}

	@POST
	@Path(Link.TAGS+"/clone")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
	@ApiOperation(value = "Clone a Tag.",
    notes = "Clone a source  tag into a destination  tag. It copies all iovs from the source. A time range selection can be applied.",
    response=Tag.class)
	public Response cloneTag(@Context UriInfo info,
			@ApiParam(value = "source: name of the globaltag to be cloned", required = true) 
			@QueryParam(value = "source") String sourcetag,
			@ApiParam(value = "dest: name of the destination global tag", required = true) 
			@QueryParam(value = "dest") String desttag,
			@ApiParam(value = "from: since time of the iov range to be copied", required = false) 
			@DefaultValue("0") @QueryParam(value = "from") String from,
			@ApiParam(value = "to: until time of the iov range to be copied", required = false) 
			@DefaultValue("Inf") @QueryParam(value = "to") String to,
			@ApiParam(value = "time: definition of time type {time|run|date|timemilli}", required = false) 
			@DefaultValue("time") @QueryParam(value = "time") String timetype
			) throws ConddbWebException {
		try {
			this.log.info("CondAdminWebController processing request for cloning " + sourcetag + " into " + desttag);
			this.globalTagExpertService.cloneTag(sourcetag, desttag,from,to,timetype);
			Tag cloned = this.globalTagService.getTag(desttag);
			GenericPojoResource<Tag> resource = (GenericPojoResource<Tag>) springResourceFactory.getGenericResource(info, cloned, 1, null);
			return created(resource);			
		} catch (ConddbServiceException e) {
			String msg = "Error cloning tag resource "+sourcetag;
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		} catch (ConversionException e) {
			String msg = "Error cloning tag resource "+sourcetag+": conversion error occurred.";
			throw buildException(msg+" "+e.getMessage(), msg, Response.Status.INTERNAL_SERVER_ERROR);
		}
	}
}
