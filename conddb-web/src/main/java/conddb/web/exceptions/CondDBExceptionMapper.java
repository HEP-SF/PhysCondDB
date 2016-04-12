/**
 * 
 */
package conddb.web.exceptions;

import javax.inject.Singleton;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.ErrorMessage;
import conddb.svc.dao.exceptions.ConddbServiceDataIntegrityException;

/**
 * @author formica
 *
 */
@Provider
//@Component
@Singleton
public class CondDBExceptionMapper implements ExceptionMapper<ConddbWebException> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Response toResponse(ConddbWebException ex) {
		
		log.debug("Calling Exception mapper on exception "+ex.getMessage());
		Throwable nested = ex.getCause();
		setHttpStatus(nested, ex.getErrMessage());

		return Response.status(ex.getErrMessage().getCode())
				.entity(ex.getErrMessage())
				.type(MediaType.APPLICATION_JSON).
				build();
	}
	
	private void setHttpStatus(Throwable ex, ErrorMessage errorMessage) {
		
		if (ex instanceof ConddbServiceDataIntegrityException) {
			errorMessage.setCode(Response.Status.CONFLICT.getStatusCode()); //defaults to data integrity server error 409
			errorMessage.setUserMessage("Data integrity violation inside conditions DB");
		} else {
			errorMessage.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()); //defaults to internal server error 500
//			StringWriter errorStackTrace = new StringWriter();
//			ex.printStackTrace(new PrintWriter(errorStackTrace));
//			errorMessage.setUserMessage(errorStackTrace.toString());
			errorMessage.setUserMessage("Internal server exception.");
		}
	}
}
