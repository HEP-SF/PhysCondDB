/**
 * 
 */
package conddb.svc.dao.exceptions;

/**
 * @author aformic
 *
 */
public class ConddbServiceDataIntegrityException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9002828894716883576L;
	
	public ConddbServiceDataIntegrityException(String string) {
		super(string);
	}

	@Override
	public String getMessage() {
		return "ConddbServiceDataIntegrityException: " + super.getMessage();
	}

}
