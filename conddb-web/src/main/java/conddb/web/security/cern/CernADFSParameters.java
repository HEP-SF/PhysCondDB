/**
 * 
 */
package conddb.web.security.cern;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author aformic
 *
 */
public class CernADFSParameters {

	private Map<String,String> ADFSmap = new HashMap<String,String>();
	private List<String> ADFSgroups = null;

	public CernADFSParameters() {
		super();
	}
	
	public void setParam(String key, String val) {
		ADFSmap.put(key, val);
		if (key.equals("ADFS_GROUP")) {
			initgroups();
		}
	}
	
	public String getParam(String key) {
		if (ADFSmap.containsKey(key)) {
			return ADFSmap.get(key);
		}
		return null;
	}
	
	protected void initgroups() {
		if (ADFSgroups == null) {
			 ADFSgroups = new ArrayList<String>();
		}
		if (ADFSgroups.isEmpty() && ADFSmap.containsKey("ADFS_GROUP")) {
			String adfsgrplist = ADFSmap.get("ADFS_GROUP");
			String[] adfsgrparr = adfsgrplist.split(";");
			for (int i=0; i<adfsgrparr.length; i++) {
				if (adfsgrparr[i].contains("atlas")) {
					ADFSgroups.add(adfsgrparr[i]);
				}
			}
		}
	}
	
	public List<String> getGroups() {
		return ADFSgroups;
	}

	@Override
	public String toString() {
		StringBuffer buf = new StringBuffer();
		Set<String> keys = ADFSmap.keySet();
		for (String akey : keys) {
			buf.append(" - "+akey+" : "+ADFSmap.get(akey)+"\n");
		}
		if (ADFSgroups == null) {
			return buf.toString();
		}
		buf.append("User belong to groups : \n");
		for (String group : ADFSgroups) {
			buf.append(group+"\n");
		}
		return buf.toString();
	}
	
	
}
