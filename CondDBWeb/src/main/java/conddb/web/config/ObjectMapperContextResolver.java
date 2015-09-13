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
@Named
@Provider
public class ObjectMapperContextResolver implements ContextResolver<ObjectMapper> {

	private Logger log = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private HibernateAwareObjectMapper objectMapper;

    public ObjectMapperContextResolver() {
        super();
        log.info("Creating objectMapperContextResolver using HibernateAwareObjectMapper "+objectMapper);
    }

    @Override
    public ObjectMapper getContext(Class<?> type) {
    	log.info("Return context objectMapper "+objectMapper);
        return objectMapper;
    }

}
