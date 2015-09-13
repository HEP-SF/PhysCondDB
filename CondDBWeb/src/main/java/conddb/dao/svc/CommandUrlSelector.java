/**
 * 
 */
package conddb.dao.svc;

import java.lang.reflect.Method;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import conddb.dao.exceptions.ConddbServiceException;

/**
 * @author formica
 *
 */
@Component
public class CommandUrlSelector {

	@Autowired
	private ConddbClientService conddbClientService;
	
	public CommandUrlSelector() {
		
	}
	
	public Method getCommand(String dataType, String[] args) throws ConddbServiceException {
		
		String action = args[0];
		action = action.substring(0, 1).toUpperCase() + action.substring(1);
		String methname = "get"+dataType+action;
		Method mth = findMethod(methname);
		if (mth == null) throw new ConddbServiceException("Cannot find "+methname);
		return mth;
	}
	
	protected Method findMethod(String methname) {
		Method[] mthds = conddbClientService.getClass().getMethods();
		for (Method method : mthds) {
			if (method.getName().contains(methname)) {
				return method;
			}
		}
		return null;
	}
}
