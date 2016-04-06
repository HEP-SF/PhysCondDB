/**
 * 
 */
package conddb.cli;

/**
 * @author formica
 * 
 */
public class HelpException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3992870693796346759L;

	/**
	 * 
	 */

	/**
	 * 
	 */
	public HelpException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public HelpException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public HelpException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public HelpException(final Throwable cause) {
		super(cause);
	}

}
