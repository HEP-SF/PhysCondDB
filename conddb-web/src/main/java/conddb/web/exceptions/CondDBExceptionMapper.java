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
		
		if (ex == null) {
			ErrorMessage errorMessage = new ErrorMessage("Unknown exception...is null");		
			errorMessage.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return Response.status(errorMessage.getCode())
					.entity(errorMessage)
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		log.debug("Calling Exception mapper on exception "+ex.getMessage());
		Throwable nested = ex.getCause();
		setHttpStatus(nested, ex.getErrMessage());

		return Response.status(ex.getErrMessage().getCode())
				.entity(ex.getErrMessage())
				.type(MediaType.APPLICATION_JSON).
				build();
	}
	
	private void setHttpStatus(Throwable ex, ErrorMessage errorMessage) {
		log.debug("CondDBExceptionMapper: received exception "+ex);
		if (ex == null)
			return;
		if (ex instanceof ConddbServiceDataIntegrityException) {
			log.debug("CondDBExceptionMapper: exception of instance ConddbServiceDataIntegrityException "+ex);
			errorMessage.setCode(Response.Status.CONFLICT.getStatusCode()); //defaults to data integrity server error 409
			errorMessage.setUserMessage("Data integrity violation inside conditions DB");
		} else if (ex instanceof ConddbWebException) {
			log.debug("CondDBExceptionMapper: exception of instance ConddbWebException "+ex);
			errorMessage.setCode(((ConddbWebException) ex).getStatus().getStatusCode()); //
			errorMessage.setUserMessage(((ConddbWebException) ex).getErrMessage().getUserMessage());
		} else {
			log.debug("CondDBExceptionMapper: exception of instance unknown "+ex);
			errorMessage.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()); //defaults to internal server error 500
			errorMessage.setUserMessage("Internal server exception.");
		}
	}
}
