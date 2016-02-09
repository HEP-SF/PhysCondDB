
package conddb.web.resources;

import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericMessageResource extends LinkedHashMap<String, String> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3628156906456364025L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GenericMessageResource(String key, String message) {
		put(key,message);
	}
}
