/**
 * 
 */
package conddb.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * @author formica
 *
 */
@Entity
@Table(name = "SYSTEM_NODE", uniqueConstraints = { @UniqueConstraint(columnNames = {
		"NODE_FULLPATH"}) })
public class SystemDescription {

	private Long id;
	private String nodeFullpath;
	private String schemaName;
	/**
	 * This is very similar to the usage of record in CMS
	 */
	private String tagNameRoot;
	private String nodeDescription;
	private Integer groupSize=1000;
	/**
	 * 
	 */
	public SystemDescription() {
		super();
	}
	/**
	 * @param nodeFullpath
	 * 	The node full path taken from COOL.
	 * @param schemaName
	 * 	The schema name taken from COOL.
	 * @param nodeDescription
	 * 	The node description taken from COOL.
	 */
	public SystemDescription(String nodeFullpath, String schemaName,
			String nodeDescription) {
		super();
		this.nodeFullpath = nodeFullpath;
		this.schemaName = schemaName;
		this.nodeDescription = nodeDescription;
	}

	/**
	 * @return
	 * 	The object ID.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return id;
	}
	/**
	 * @param id
	 * 	The ID to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return
	 * 	The node description.
	 */
	@Column(name = "NODE_DESCRIPTION", nullable = false, length = 500)
	public String getNodeDescription() {
		return nodeDescription;
	}
	/**
	 * @param nodeDescription
	 * 	The node description to set.
	 */
	public void setNodeDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}
	
	/**
	 * @return
	 * 	The node full path.
	 */
	@Column(name = "NODE_FULLPATH", nullable = false, length = 500)
	public String getNodeFullpath() {
		return nodeFullpath;
	}

	/**
	 * @param nodeFullpath
	 * 	The node full path to set.
	 */
	public void setNodeFullpath(String nodeFullpath) {
		this.nodeFullpath = nodeFullpath;
	}

	/**
	 * @return
	 * 	The schema name.
	 */
	@Column(name = "SCHEMA_NAME", nullable = false, length = 255)
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName
	 * 	The schema name to set.
	 */
	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return
	 * 	The tag name root for this system.
	 */
	@Column(name = "TAG_NAME_ROOT", nullable = false, length = 1000)
	public String getTagNameRoot() {
		return tagNameRoot;
	}
	
	/**
	 * @param tagNameRoot
	 * 	The tag name root to set.
	 */
	public void setTagNameRoot(String tagNameRoot) {
		this.tagNameRoot = tagNameRoot;
	}
	
	/**
	 * @return
	 * 	The iov group size.
	 */
	@Column(name = "IOVGROUP_SIZE", nullable = false)
	public Integer getGroupSize() {
		return groupSize;
	}
	
	/**
	 * @param groupSize
	 * 	The group size to set.
	 */
	public void setGroupSize(Integer groupSize) {
		this.groupSize = groupSize;
	}

	
}
