/**
 * 
 */
package conddb.web.exceptions;

/**
 * @author formica
 *
 */
public class ConddbWebException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;

	public ConddbWebException(String string) {
		super(string);
	}

	@Override
	public String getMessage() {
		return "ConddbWebException: " + super.getMessage();
	}

	
}
