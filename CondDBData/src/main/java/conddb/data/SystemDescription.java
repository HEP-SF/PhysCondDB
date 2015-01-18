/**
 * 
 */
package conddb.data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
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
	private String tagNameRoot;
	private String nodeDescription;
	/**
	 * 
	 */
	public SystemDescription() {
		super();
	}
	/**
	 * @param nodeFullpath
	 * @param schemaName
	 * @param nodeDescription
	 */
	public SystemDescription(String nodeFullpath, String schemaName,
			String nodeDescription) {
		super();
		this.nodeFullpath = nodeFullpath;
		this.schemaName = schemaName;
		this.nodeDescription = nodeDescription;
	}

	@Id
	@GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	@Column(name = "NODE_DESCRIPTION", nullable = false, length = 500)
	public String getNodeDescription() {
		return nodeDescription;
	}
	public void setNodeDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}
	
	@Column(name = "NODE_FULLPATH", nullable = false, length = 500)
	public String getNodeFullpath() {
		return nodeFullpath;
	}

	public void setNodeFullpath(String nodeFullpath) {
		this.nodeFullpath = nodeFullpath;
	}

	@Column(name = "SCHEMA_NAME", nullable = false, length = 255)
	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	@Column(name = "TAG_NAME_ROOT", nullable = false, length = 1000)
	public String getTagNameRoot() {
		return tagNameRoot;
	}
	
	public void setTagNameRoot(String tagNameRoot) {
		this.tagNameRoot = tagNameRoot;
	}

	
}
