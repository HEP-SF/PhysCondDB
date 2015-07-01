/**
 * 
 */
package conddb.web.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

import conddb.data.ErrorMessage;

/**
 * @author formica
 *
 */
@Provider
@Component
public class CondDBExceptionMapper implements ExceptionMapper<ConddbWebException> {

	@Override
	public Response toResponse(ConddbWebException ex) {
		return Response.status(ex.getStatus())
				.entity(new ErrorMessage(ex.getMessage()))
				.type(MediaType.APPLICATION_JSON).
				build();
	}
}
