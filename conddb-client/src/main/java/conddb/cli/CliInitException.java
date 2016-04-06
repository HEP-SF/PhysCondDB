/**
 * 
 */
package conddb.cli;

/**
 * @author formica
 * 
 */
public class CliInitException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5233825732730052512L;

	/**
	 * 
	 */
	public CliInitException() {
		super();
	}

	/**
	 * @param message
	 * @param cause
	 */
	public CliInitException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public CliInitException(final String message) {
		super(message);
	}

	/**
	 * @param cause
	 */
	public CliInitException(final Throwable cause) {
		super(cause);
	}

}
