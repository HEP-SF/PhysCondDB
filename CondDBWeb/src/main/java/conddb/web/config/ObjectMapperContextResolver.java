/**
 * 
 */
package conddb.web.config;

import javax.inject.Named;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;

import conddb.utils.json.HibernateAwareObjectMapper;

/**
 * @author formica
 *
 */
@Provider
@Named
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
    private HibernateAwareObjectMapper hibernateAwareObjectMapper;

    public ObjectMapperContextResolver() {
        super();
        log.info("Creating objectMapperContextResolver using HibernateAwareObjectMapper "+hibernateAwareObjectMapper);
//        if (hibernateAwareObjectMapper == null) {
//        	hibernateAwareObjectMapper = (HibernateAwareObjectMapper) createDefaultMapper();
//        }
    }
    
    private static ObjectMapper createDefaultMapper() {
        final HibernateAwareObjectMapper result = new HibernateAwareObjectMapper();
        return result;
    }
    
    public HibernateAwareObjectMapper getHibernateAwareObjectMapper() {
		return hibernateAwareObjectMapper;
	}

	public void setHibernateAwareObjectMapper(HibernateAwareObjectMapper hibernateAwareObjectMapper) {
		log.info("Setting hibernateAwareObjectMapper in contextresolver");
		this.hibernateAwareObjectMapper = hibernateAwareObjectMapper;
	}

	@Override
    public ObjectMapper getContext(Class<?> type) {
    	log.info("Return context objectMapper "+hibernateAwareObjectMapper);
        return hibernateAwareObjectMapper;
    }

}
