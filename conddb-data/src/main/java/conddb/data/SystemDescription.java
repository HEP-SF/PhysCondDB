/**
 * 
 */
package conddb.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import conddb.data.annotations.Href;

/**
 * @author formica
 *
 */
@Entity
@Table(name = "PHCOND_SYSTEM_NODE", uniqueConstraints = { @UniqueConstraint(columnNames = {
		"NODE_FULLPATH"}) })
public class SystemDescription extends conddb.data.AfEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2590694477609404759L;
	private Long id;
	private String nodeFullpath;
	private String schemaName;
	/**
	 * This is very similar to the usage of record in CMS
	 */
	private String tagNameRoot;
	private String nodeDescription;
	private BigDecimal groupSize=new BigDecimal(1000000000L);
	
	private List<Tag> tags;
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
	@Column(name = "NODE_FULLPATH", unique = true, updatable = false, nullable = false, length = 500)
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
	@Href
	@Column(name = "TAG_NAME_ROOT", unique = true, updatable = false, nullable = false, length = 1000)
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
	@Column(name = "IOVGROUP_SIZE", nullable = false, precision = 22, scale = 0)
	public BigDecimal getGroupSize() {
		return groupSize;
	}
	
	/**
	 * @param groupSize
	 * 	The group size to set.
	 */
	public void setGroupSize(BigDecimal groupSize) {
		this.groupSize = groupSize;
	}
	
	@Transient
	public List<Tag> getTags() {
		return tags;
	}
	
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	
	@Override
	public String toString() {
		return "SystemDescription [id=" + id + ", nodeFullpath=" + nodeFullpath + ", schemaName=" + schemaName
				+ ", tagNameRoot=" + tagNameRoot + ", nodeDescription=" + nodeDescription + ", groupSize=" + groupSize
				+ "]";
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((tagNameRoot == null) ? 0 : tagNameRoot.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SystemDescription other = (SystemDescription) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (tagNameRoot == null) {
			if (other.tagNameRoot != null)
				return false;
		} else if (!tagNameRoot.equals(other.tagNameRoot))
			return false;
		return true;
	}

	
}
