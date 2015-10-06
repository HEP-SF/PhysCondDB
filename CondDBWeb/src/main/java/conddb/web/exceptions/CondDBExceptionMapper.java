/**
 * 
 */
package conddb.web.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author formica
 *
 */
@Provider
@Component
public class CondDBExceptionMapper implements ExceptionMapper<ConddbWebException> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Response toResponse(ConddbWebException ex) {
		
		log.debug("Calling Exception mapper on exception "+ex.getMessage());
		return Response.status(ex.getStatus())
				.entity(ex.getErrMessage())
				.type(MediaType.APPLICATION_JSON).
				build();
	}
}
