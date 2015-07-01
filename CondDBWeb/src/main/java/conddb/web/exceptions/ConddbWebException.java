/**
 * 
 */
package conddb.web.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

/**
 * @author formica
 *
 */
public class ConddbWebException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;

	
	/**
	 * 
	 */
	public ConddbWebException() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 * @param enableSuppression
	 * @param writableStackTrace
	 */
	public ConddbWebException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param cause
	 */
	public ConddbWebException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public ConddbWebException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public ConddbWebException(String string) {
		super(string);
	}

	@Override
	public String getMessage() {
		return "ConddbWebException: " + super.getMessage();
	}
	
	public Status getStatus() {
		return Response.Status.BAD_REQUEST;
	}
}
