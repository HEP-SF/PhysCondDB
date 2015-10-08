package conddb.web.config;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import conddb.web.controllers.GlobalTagRestController;
import conddb.web.controllers.IovRestController;
import conddb.web.controllers.SystemDescriptionRestController;
import conddb.web.controllers.TagRestController;

public class SwaggerApplication extends Application {

	@Override
	public Set<Class<?>> getClasses() {

		Set<Class<?>> resources = new HashSet();

		resources.add(GlobalTagRestController.class);
		resources.add(TagRestController.class);
		resources.add(IovRestController.class);
		resources.add(SystemDescriptionRestController.class);
		resources.add(io.swagger.jaxrs.listing.ApiListingResource.class);
		resources.add(io.swagger.jaxrs.listing.SwaggerSerializers.class);

		return resources;
	}

}
