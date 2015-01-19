package conddb.cool.data;

/**
 * 
 */

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import conddb.cool.migutils.TimestampStringFormatter;


/**
 * @author formica
 * 
 */
@Entity
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class NodeType implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8950148398190695530L;

	@Column(name = "SCHEMA_NAME", length = 30)
	private String schemaName;
	@Column(name = "DBNAME", length = 30)
	private String dbName;

	@Column(name = "NODE_ID", precision = 10, scale = 0)
	private Long nodeId;
	@Column(name = "NODE_NAME", length = 255)
	private String nodeName;

	@Id
	@Column(name = "NODE_FULLPATH", length = 255)
	private String nodeFullpath;
	
	@Column(name = "NODE_DESCRIPTION", length = 255)
	private String nodeDescription;
	@Column(name = "NODE_ISLEAF", precision = 5, scale = 0)
	private Integer nodeIsleaf;
	@Column(name = "NODE_INSTIME", length = 255)
	private String nodeInstime;

	@Column(name = "IOV_BASE", length = 255)
	private String nodeIovBase;
	@Column(name = "IOV_TYPE", length = 255)
	private String nodeIovType;

	@Column(name = "NODE_TINSTIME")
	private Timestamp nodeTinstime;

	@Column(name = "LASTMOD_DATE", length = 255)
	private String lastmodDate;
	@Column(name = "FOLDER_VERSIONING", precision = 10, scale = 0)
	private Integer folderVersioning;
	@Column(name = "FOLDER_PAYLOADSPEC", length = 4000)
	private String folderPayloadSpec;
	@Column(name = "FOLDER_IOVTABLENAME", length = 255)
	private String folderIovtablename;
	@Column(name = "FOLDER_TAGTABLENAME", length = 255)
	private String folderTagtablename;
	@Column(name = "FOLDER_CHANNELTABLENAME", length = 255)
	private String folderChanneltablename;


	/**
	 * 
	 */
	@Transient
	@XmlElement(name = "iov", type = CoolIovType.class)
	private List<CoolIovType> iovList = null;


	/**
	 * @return the nodeId
	 */
	public Long getNodeId() {
		return nodeId;
	}

	/**
	 * @param nodeId
	 *            the nodeId to set
	 */
	public void setNodeId(final Long nodeId) {
		this.nodeId = nodeId;
	}

	/**
	 * @return the nodeName
	 */
	public String getNodeName() {
		return nodeName;
	}

	/**
	 * @param nodeName
	 *            the nodeName to set
	 */
	public void setNodeName(final String nodeName) {
		this.nodeName = nodeName;
	}

	/**
	 * @return the nodeFullpath
	 */
	public String getNodeFullpath() {
		return nodeFullpath;
	}

	/**
	 * @param nodeFullpath
	 *            the nodeFullpath to set
	 */
	public void setNodeFullpath(final String nodeFullpath) {
		this.nodeFullpath = nodeFullpath;
	}

	/**
	 * @return the nodeDescription
	 */
	public String getNodeDescription() {
		return nodeDescription;
	}

	/**
	 * @param nodeDescription
	 *            the nodeDescription to set
	 */
	public void setNodeDescription(final String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}

	/**
	 * @return the nodeIsleaf
	 */
	public Integer getNodeIsleaf() {
		return nodeIsleaf;
	}

	/**
	 * @param nodeIsleaf
	 *            the nodeIsleaf to set
	 */
	public void setNodeIsleaf(final Integer nodeIsleaf) {
		this.nodeIsleaf = nodeIsleaf;
	}

	/**
	 * @return the nodeInstime
	 */
	public String getNodeInstime() {
		return nodeInstime;
	}

	/**
	 * @param nodeInstime
	 *            the nodeInstime to set
	 */
	public void setNodeInstime(final String nodeInstime) {
		this.nodeInstime = nodeInstime;
	}

	/**
	 * @return the nodeTinstime
	 */
	public Timestamp getNodeTinstime() {
		return nodeTinstime;
	}

	/**
	 * @param nodeTinstime
	 *            the nodeTinstime to set
	 */
	public void setNodeTinstime(final Timestamp nodeTinstime) {
		this.nodeTinstime = nodeTinstime;
	}

	/**
	 * @return the lastmodDate
	 */
	public String getLastmodDate() {
		return lastmodDate;
	}

	/**
	 * @param lastmodDate
	 *            the lastmodDate to set
	 */
	public void setLastmodDate(final String lastmodDate) {
		this.lastmodDate = lastmodDate;
	}

	/**
	 * @return the folderIovtablename
	 */
	public String getFolderIovtablename() {
		return folderIovtablename;
	}

	/**
	 * @param folderIovtablename
	 *            the folderIovtablename to set
	 */
	public void setFolderIovtablename(final String folderIovtablename) {
		this.folderIovtablename = folderIovtablename;
	}

	/**
	 * @return the folderTagtablename
	 */
	public String getFolderTagtablename() {
		return folderTagtablename;
	}

	/**
	 * @param folderTagtablename
	 *            the folderTagtablename to set
	 */
	public void setFolderTagtablename(final String folderTagtablename) {
		this.folderTagtablename = folderTagtablename;
	}

	/**
	 * @return the folderChanneltablename
	 */
	public String getFolderChanneltablename() {
		return folderChanneltablename;
	}

	/**
	 * @param folderChanneltablename
	 *            the folderChanneltablename to set
	 */
	public void setFolderChanneltablename(final String folderChanneltablename) {
		this.folderChanneltablename = folderChanneltablename;
	}

	/**
	 * @return the schemaName
	 */
	public String getSchemaName() {
		return schemaName;
	}

	/**
	 * @param schemaName
	 *            the schemaName to set
	 */
	public void setSchemaName(final String schemaName) {
		this.schemaName = schemaName;
	}

	/**
	 * @return the dbName
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * @param dbName
	 *            the dbName to set
	 */
	public void setDbName(final String dbName) {
		this.dbName = dbName;
	}

	/**
	 * @return the nodeIovBase
	 */
	public String getNodeIovBase() {
		return nodeIovBase;
	}

	/**
	 * @param nodeIovBase
	 *            the nodeIovBase to set
	 */
	public void setNodeIovBase(final String nodeIovBase) {
		this.nodeIovBase = nodeIovBase;
	}

	/**
	 * @return the nodeIovType
	 */
	public String getNodeIovType() {
		return nodeIovType;
	}

	/**
	 * @param nodeIovType
	 *            the nodeIovType to set
	 */
	public void setNodeIovType(final String nodeIovType) {
		this.nodeIovType = nodeIovType;
	}

	/**
	 * @return
	 */
	public String getNodeTinstimeStr() {
		if (nodeTinstime == null) {
			return "";
		}
		final String ret = TimestampStringFormatter.format("yyyy:MM:dd hh:mm:ss",
				nodeTinstime);
		return ret;

	}

	/**
	 * @return
	 */
	public void setNodeTinstimeStr(final String tinstimstr) {
		// Ignore this method
	}

	/**
	 * @return the folderVersioning
	 */
	public Integer getFolderVersioning() {
		return folderVersioning;
	}

	/**
	 * @param folderVersioning
	 *            the folderVersioning to set
	 */
	public void setFolderVersioning(final Integer folderVersioning) {
		this.folderVersioning = folderVersioning;
	}

	/**
	 * @return the folderPayloadSpec
	 */
	public String getFolderPayloadSpec() {
		return folderPayloadSpec;
	}

	/**
	 * @param folderPayloadSpec
	 *            the folderPayloadSpec to set
	 */
	public void setFolderPayloadSpec(final String folderPayloadSpec) {
		this.folderPayloadSpec = folderPayloadSpec;
	}

	/**
	 * @return the iovList
	 */
	public List<CoolIovType> getIovList() {
		return iovList;
	}

	/**
	 * @param iovList
	 *            the iovList to set
	 */
	public void setIovList(final List<CoolIovType> iovList) {
		this.iovList = iovList;
	}

}
