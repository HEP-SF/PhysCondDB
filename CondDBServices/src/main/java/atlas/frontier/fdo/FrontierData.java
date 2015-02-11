/**
 * 
 */
package atlas.frontier.fdo;

import java.io.Serializable;
import java.util.List;

/**
 * @author formica
 *
 */
public class FrontierData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4332489768238044389L;
	List<?> dataList=null;

	/**
	 * @param dataList
	 */
	public FrontierData(List<?> dataList) {
		super();
		this.dataList = dataList;
	}

	/**
	 * @return the dataList
	 */
	public List<?> getDataList() {
		return dataList;
	}

	/**
	 * @param dataList the dataList to set
	 */
	public void setDataList(List<?> dataList) {
		this.dataList = dataList;
	}
	
	
}
