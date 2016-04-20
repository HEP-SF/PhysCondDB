/**
 * 
 */
package conddb.web.resources;

import java.util.Set;

import conddb.data.AfEntity;

/**
 * @author aformic
 *
 */
public class SwaggerCollectionResource<T extends AfEntity> {

	private Integer offset;
	
	private Integer limit;
	
	private Set<T> items;

	/**
	 * @return the offset
	 */
	public Integer getOffset() {
		return offset;
	}

	/**
	 * @param offset the offset to set
	 */
	public void setOffset(Integer offset) {
		this.offset = offset;
	}

	/**
	 * @return the limit
	 */
	public Integer getLimit() {
		return limit;
	}

	/**
	 * @param limit the limit to set
	 */
	public void setLimit(Integer limit) {
		this.limit = limit;
	}

	/**
	 * @return the items
	 */
	public Set<T> getItems() {
		return items;
	}

	/**
	 * @param items the items to set
	 */
	public void setItems(Set<T> items) {
		this.items = items;
	}
	
	
}
