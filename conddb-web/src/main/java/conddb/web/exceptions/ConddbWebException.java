/**
 * 
 */
package conddb.web.exceptions;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import conddb.data.ErrorMessage;

/**
 * @author formica
 *
 */
public class ConddbWebException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;
	Response.Status status=Response.Status.OK;
	ErrorMessage errMessage = null;
	
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

	public Status setStatus(Response.Status status) {
		return this.status = status;
	}

	public Status getStatus() {
		return this.status;
	}

	public ErrorMessage getErrMessage() {
		return errMessage;
	}

	public void setErrMessage(ErrorMessage errMessage) {
		this.errMessage = errMessage;
	}
	
}
