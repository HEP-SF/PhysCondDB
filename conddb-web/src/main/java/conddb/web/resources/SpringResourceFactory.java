/**
 * 
 */
package conddb.web.resources;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import conddb.data.AfEntity;
import conddb.svc.annotations.ProfileExecution;
import conddb.web.resources.generic.GenericPojoResource;

/**
 * @author aformic
 *
 */
@Component
public class SpringResourceFactory {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	
	public SpringResourceFactory() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);  
	}
	
	@ProfileExecution
	public <T extends AfEntity> GenericPojoResource<T> getGenericResource(UriInfo info, T entity, int level, T parent) {
		GenericPojoResource<T> resource = new GenericPojoResource<>(info,entity,level,parent);
		return resource;			
	}
	
	public <T extends AfEntity> CollectionResource listToCollection(Collection<T> coll, boolean expand,
			UriInfo info, String subPath) {
		Collection<Link> items = new ArrayList<Link>(coll.size());
		for (T entity : coll) {
			if (expand) {
				GenericPojoResource<AfEntity> resource = new GenericPojoResource<>(info, entity);
				items.add(resource);
			} else {
				Link link = new Link(info, entity);
				items.add(link);
			}
		}
		return new CollectionResource(info,subPath,items);
	}
	
	public <T extends AfEntity> CollectionResource listToCollection(Collection<T> coll, boolean expand,
			UriInfo info, String subPath, int level) {
		Collection<Link> items = new ArrayList<Link>(coll.size());
		for (T entity : coll) {
			if (expand) {
				GenericPojoResource<AfEntity> resource = new GenericPojoResource<>(info, entity, level, null);
				items.add(resource);
			} else {
				Link link = new Link(info, entity);
				items.add(link);
			}
		}
		return new CollectionResource(info,subPath,items);
	}
	
	public <T extends AfEntity> CollectionResource listToCollection(Collection<T> coll, boolean expand,
			UriInfo info, String subPath, int level, int offset, int limit) {
		Collection<Link> items = new ArrayList<Link>(coll.size());
		for (T entity : coll) {
			if (expand) {
				GenericPojoResource<AfEntity> resource = new GenericPojoResource<>(info, entity, level, null);
				items.add(resource);
			} else {
				Link link = new Link(info, entity);
				items.add(link);
			}
		}
		return new CollectionResource(info,subPath,items,offset,limit);
	}

	public Link getCollectionResource(UriInfo info, String subPath, Collection<?> c) {
		return new CollectionResource(info,subPath,c);
	}
	public Link getCollectionResource(UriInfo info, String subPath, Collection<?> c, int offset, int limit) {
		return new CollectionResource(info,subPath,c,offset,limit);
	}
}
