/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package conddb.web.config;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.process.Inflector;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import conddb.web.admin.controllers.CondAdminWebController;
import conddb.web.calib.controllers.CalibrationRestController;
import conddb.web.controllers.GlobalTagExpRestController;
import conddb.web.controllers.GlobalTagMapExpRestController;
import conddb.web.controllers.GlobalTagMapRestController;
import conddb.web.controllers.GlobalTagRestController;
import conddb.web.controllers.IovExpRestController;
import conddb.web.controllers.IovRestController;
import conddb.web.controllers.PayloadExpRestController;
import conddb.web.controllers.PayloadRestController;
import conddb.web.controllers.SystemDescriptionExpRestController;
import conddb.web.controllers.SystemDescriptionRestController;
import conddb.web.controllers.TagExpRestController;
import conddb.web.controllers.TagRestController;
import conddb.web.exceptions.CondDBExceptionMapper;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;


/**
 * Configuration for Jersey.
 * In order to integrate Jersey and Spring we have been using the jersey-spring3 module.
 * Nevertheless, autowiring of Spring managed beans is not trivial.
 * In this example, in order for the autowiring to work, we need to declare the
 * HibernateAwareObjectMapper directly in xml configuration.
 * 
 * @author formica
 *
 */
public class JaxRsApplication extends ResourceConfig {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	// This is in case we need to initialize the JacksonJsonProvider with an object mapper...
//	@Inject
//	private HibernateAwareObjectMapper hibernateAwareObjectMapper;
	
	// Swagger initialization
	public JaxRsApplication() {
		
		// Probably not needed since it should pick up from annotations
		// NOT TRUE: this is really needed !!!!
		register(GlobalTagRestController.class);
		register(GlobalTagMapRestController.class);
		register(TagRestController.class);
		register(IovRestController.class);
		register(IovExpRestController.class);
		register(PayloadRestController.class);
		register(PayloadExpRestController.class);
		register(GlobalTagExpRestController.class);
		register(GlobalTagMapExpRestController.class);
		register(TagExpRestController.class);
		register(CalibrationRestController.class);
		register(SystemDescriptionRestController.class);
		register(SystemDescriptionExpRestController.class);
		register(CondAdminWebController.class);

		// register json provider
		register(JacksonJsonProvider.class);
		// Not yet sure if this or the previous one are working now....
//		register(new JacksonJsonProvider(om));

		// register filters
		register(RequestContextFilter.class);
        register(ObjectMapperContextResolver.class);

		// register exception mappers
        register(CondDBExceptionMapper.class);
        
		// register features
		register(JacksonFeature.class);
		register(MultiPartFeature.class);
		
		// Test programmatic resources
		buildResources();
		
		// Test swagger
		initSwagger();
	}
	
	protected void buildResources() {
		final Resource.Builder resourceBuilder = Resource.builder(GlobalTagRestController.class);
		resourceBuilder.addMethod("OPTIONS")
		    .handledBy(new Inflector<ContainerRequestContext, Response>() {
		        @Override
		        public Response apply(ContainerRequestContext containerRequestContext) {
		            return Response.ok("This is a response to an OPTIONS method.").build();
		        }
		    });
		final Resource resource = resourceBuilder.build();
//		registerResources(resource);
	}

	protected void initSwagger() {
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("2.0");
		beanConfig.setSchemes(new String[] { "http" });
		beanConfig.setHost("localhost:8080");
		beanConfig.setBasePath("/physconddb/conddbweb/rest");
		beanConfig.setResourcePackage("conddb.web.controllers");
		beanConfig.setTitle("PhysCondDB REST API setting in JAX-RS");
		Info info = new Info();
		info.setDescription("REST services for PhysCondDB access, initialized by JAX-RS.");
		beanConfig.setInfo(info);
		beanConfig.setScan(true);
		
		register(beanConfig);
		register(io.swagger.jaxrs.listing.ApiListingResource.class);
		register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
	}
}
