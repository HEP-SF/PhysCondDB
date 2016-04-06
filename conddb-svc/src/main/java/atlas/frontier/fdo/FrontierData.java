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
	/**
	 * 
	 */
	List<?> dataList=null;

	/**
	 * @param dataList
	 * 	The list of data to transfer.
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
	 * @param dataList 
	 * 	The dataList to set
	 */
	public void setDataList(List<?> dataList) {
		this.dataList = dataList;
	}
	
}
