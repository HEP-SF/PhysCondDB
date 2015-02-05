/**
 * 
 */
package conddb.dao.exceptions;

/**
 * @author formica
 *
 */
public class ConddbServiceException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8552538724531679765L;

	@Override
	public String getMessage() {
		return "ConddbServiceException: " + super.getMessage();
	}

	
}
