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

import javax.inject.Inject;
import javax.ws.rs.ApplicationPath;

import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.models.Info;









import org.glassfish.jersey.jackson.JacksonFeature;
//import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.spring.scope.RequestContextFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;

import conddb.utils.json.HibernateAwareObjectMapper;
import conddb.web.controllers.CondAdminWebController;
import conddb.web.controllers.CondExpertWebController;
import conddb.web.controllers.CondWebController;
import conddb.web.exceptions.CondDBExceptionMapper;


/**
 * @author formica
 *
 */
////@ApplicationPath("/")
//@Configuration
//@Component
public class JaxRsApplication extends ResourceConfig {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Inject
	private HibernateAwareObjectMapper hibernateAwareObjectMapper;
	
	// Swagger initialization
	public JaxRsApplication() {
		
		// Probably not needed since it should pick up from annotations
		// NOT TRUE: this is really needed !!!!
		register(CondWebController.class);
		register(CondExpertWebController.class);
		register(CondAdminWebController.class);

		// register json provider
		log.info("Register JacksonJsonProvide using object mapper "+hibernateAwareObjectMapper);
		register(new JacksonJsonProvider(hibernateAwareObjectMapper));

		// register filters
		register(RequestContextFilter.class);
        register(ObjectMapperContextResolver.class);

		// register exception mappers
        register(CondDBExceptionMapper.class);
		// register features
		register(JacksonFeature.class);
		
		initSwagger();

	}

	public ObjectMapper getHibernateAwareObjectMapper() {
		return hibernateAwareObjectMapper;
	}

	public void setHibernateAwareObjectMapper(
			HibernateAwareObjectMapper hibernateAwareObjectMapper) {
		this.hibernateAwareObjectMapper = hibernateAwareObjectMapper;
	}

	protected void initSwagger() {
		BeanConfig beanConfig = new BeanConfig();
		beanConfig.setVersion("1.0.2");
		beanConfig.setSchemes(new String[] { "http" });
		beanConfig.setHost("localhost:8080");
		beanConfig.setBasePath("/conddbweb/rest");
		beanConfig.setResourcePackage("conddb.web.controllers");
		beanConfig.setTitle("PhysCondDB REST API");
		Info info = new Info();
		info.setDescription("REST services for PhysCondDB access.");
		beanConfig.setInfo(info);
		beanConfig.setScan(true);
		
		register(beanConfig);
		register(io.swagger.jaxrs.listing.ApiListingResource.class);
		register(io.swagger.jaxrs.listing.SwaggerSerializers.class);
	}
}
