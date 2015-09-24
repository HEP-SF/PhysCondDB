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

import conddb.data.Entity;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.data.Iov;
import conddb.data.Payload;
import conddb.utils.json.serializers.TimestampFormat;

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

	public Link getResource(String resourceName, UriInfo info, Entity entity) {
		if (resourceName.equals("globaltag")) {
			GlobalTagResource resource = new GlobalTagResource(info,(GlobalTag)entity);
			log.info("Create global tag resource: timestamp format is "+timestampFormat);
			resource.serializeTimestamps(timestampFormat);
			return resource;
		} else if (resourceName.equals("globaltagmap")) {
			GlobalTagMapResource resource = new GlobalTagMapResource(info,(GlobalTagMap)entity);
			log.info("Create global tag map resource");
			return resource;			
		} else if (resourceName.equals("tag")) {
			TagResource resource = new TagResource(info,(Tag)entity);
			log.info("Create tag resource");
			resource.serializeTimestamps(timestampFormat);
			return resource;			
		} else if (resourceName.equals("iov")) {
			IovResource resource = new IovResource(info,(Iov)entity);
			log.info("Create iov resource");
			resource.serializeTimestamps(timestampFormat);
			return resource;			
		} else if (resourceName.equals("payload")) {
			PayloadResource resource = new PayloadResource(info,(Payload)entity);
			log.info("Create payload resource");
			resource.serializeTimestamps(timestampFormat);
			return resource;			
		} else {
			Link link = new Link(info,entity);
			return link;
		}
	}
	
	public Link getCollectionResource(UriInfo info, String subPath, Collection c) {
		return new CollectionResource(info,subPath,c);
	}
	public Link getCollectionResource(UriInfo info, String subPath, Collection c, int offset, int limit) {
		return new CollectionResource(info,subPath,c,offset,limit);
	}

	public TimestampFormat getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(TimestampFormat timestampFormat) {
		log.info("Update tsformat using autowiring: "+timestampFormat.getPattern());
		this.timestampFormat = timestampFormat;
	}
}
