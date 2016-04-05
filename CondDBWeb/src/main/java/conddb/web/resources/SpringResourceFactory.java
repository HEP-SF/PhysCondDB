/**
 * 
 */
package conddb.web.resources;

import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import conddb.annotations.ProfileExecution;
import conddb.data.Entity;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.data.Iov;
import conddb.utils.json.serializers.TimestampFormat;
import conddb.web.resources.generic.GenericPojoResource;

/**
 * @author aformic
 *
 */
@Component
public class SpringResourceFactory {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	TimestampFormat timestampFormat;
	
	public SpringResourceFactory() {
		// TODO Auto-generated constructor stub
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);  
	}

	@ProfileExecution
	public Link getResource(String resourceName, UriInfo info, Entity entity) {
		if (resourceName.equals("generic-gt")) {
			GenericPojoResource<GlobalTag> resource = new GenericPojoResource<GlobalTag>(info,
					entity,2,null);
			log.info("Create generic globaltag resource");
			return resource;			
		} else if (resourceName.equals("generic-gtmap")) {
			GenericPojoResource<GlobalTagMap> resource = new GenericPojoResource<GlobalTagMap>(info,
					entity,1,null);
			log.info("Create generic globaltag map resource");
			return resource;			
		} else if (resourceName.equals("generic-tag")) {
			GenericPojoResource<Tag> resource = new GenericPojoResource<Tag>(info,
					entity,2,null);
			log.info("Create generic tag resource");
			return resource;			
		} else if (resourceName.equals("generic-iov")) {
			GenericPojoResource<Iov> resource = new GenericPojoResource<Iov>(info,
					entity,1,null);
			log.info("Create generic iov resource");
			return resource;			
		} else {
			Link link = new Link(info,entity);
			return link;
		}
	}
	
	@ProfileExecution
	public <T extends Entity> Link getGenericResource(UriInfo info, T entity, int level, T parent) {
		GenericPojoResource<T> resource = new GenericPojoResource<T>(info,entity,level,parent);
		log.info("Created generic resource for type "+entity.getClass().getName());
		return resource;			
	}
	
	public Link getCollectionResource(UriInfo info, String subPath, Collection c) {
		return new CollectionResource(info,subPath,c);
	}
	public Link getCollectionResource(UriInfo info, String subPath, Collection c, int offset, int limit) {
		return new CollectionResource(info,subPath,c,offset,limit);
	}
}
