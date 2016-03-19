/**
 * 
 */
package conddb.web.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import conddb.data.ErrorMessage;

/**
 * @author aformic
 *
 */
public class GlobalControllerExceptionMapper implements ExceptionMapper<Throwable> { 
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public Response toResponse(Throwable ex) {
		if (ex == null) {
			ErrorMessage errorMessage = new ErrorMessage("Unknown exception...is null");		
			errorMessage.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
			return Response.status(errorMessage.getCode())
					.entity(errorMessage)
					.type(MediaType.APPLICATION_JSON)
					.build();
		}
		log.info("Exception mapper has catched exception "+ex.getMessage());
		ErrorMessage errorMessage = new ErrorMessage(ex.getMessage());		
		setHttpStatus(ex, errorMessage);
		errorMessage.setInternalMessage(ex.getMessage());

		return Response.status(errorMessage.getCode())
				.entity(errorMessage)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}
	
	private void setHttpStatus(Throwable ex, ErrorMessage errorMessage) {
		if(ex instanceof WebApplicationException ) {
			errorMessage.setCode(((WebApplicationException)ex).getResponse().getStatus());
		} else if (ex instanceof DataIntegrityViolationException) {
			errorMessage.setCode(Response.Status.CONFLICT.getStatusCode()); //defaults to data integrity server error 409
			errorMessage.setUserMessage("Data integrity violation");
		} else if (ex instanceof ConddbWebException) {
			errorMessage.setCode(((ConddbWebException) ex).getStatus().getStatusCode()); //defaults to data integrity server error 409
			errorMessage.setUserMessage(((ConddbWebException) ex).getErrMessage().getUserMessage());
		} else {
			errorMessage.setCode(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()); //defaults to internal server error 500
//			StringWriter errorStackTrace = new StringWriter();
//			ex.printStackTrace(new PrintWriter(errorStackTrace));
//			errorMessage.setUserMessage(errorStackTrace.toString());
			errorMessage.setUserMessage("Internal server exception");
			log.debug("Dump stack trace ");
			ex.printStackTrace();
		}
	}
}