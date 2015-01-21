/**
 * 
 */
package conddb.cool.data;

import java.io.Serializable;


/**
 * <p>
 * This POJO represents the association between a global tag and a leaf tag in a
 * given node. Cool tags are defined in the _TAGS table of a COOL schema for
 * every folder (node), as well as inside the main _TAGS table, where global
 * tags are stored.
 * </p>
 * <p>
 * The Queries defined for this POJO are:<br>
 * 
 * <b>QUERY_FINDGTAGS_TAGS_TRACE [cool_select_pkg]</b><br>
 * This query takes as arguments the SCHEMA, DB, GTAG and retrieves a list of
 * matching nodes/tags; it uses internally the function
 * cool_select_pkg.f_getall_tagsforgtag(.....).<br>
 * For every node/tag associated with the given gtag, there is one line with
 * information on schema and db, gtag informations, tag informations, and node
 * informations.<br>
 * 
 * <b>QUERY_FINDGTAGS_TAGS_FULLTRACE [cool_select_pkg]</b><br>
 * This query takes as arguments the SCHEMA, DB, GTAG and retrieves a list of
 * matching nodes/tags; it uses internally the function
 * cool_select_pkg.f_getall_branchtagsforgtag(.....).<br>
 * For every node/tag associated with the given gtag, there is one line with
 * information on schema and db, gtag informations, tag informations, and node
 * informations. The result contains all intermediate branch level tags.<br>
 * 
 * <b>QUERY_FINDGTAGS_FORTAG [cool_select_pkg]</b><br>
 * This query takes as arguments the SCHEMA, DB, GTAG, TAG, NODE and retrieves a
 * list of matching nodes/tags/gtags; it uses internally the function
 * cool_select_pkg.f_getall_tagsforgtag(.....).<br>
 * For every node/tag associated with the given gtag, there is one line with
 * information on schema and db, gtag informations, tag informations, and node
 * informations.<br>
 * 
 * <b>QUERY_FINDGTAG_DOUBLEFLD [cool_select_pkg]</b><br>
 * This query takes as arguments the SCHEMA, DB, GTAG and retrieves a list of
 * matching nodes/tags/gtags; it uses internally the function
 * cool_select_pkg.f_getall_doubletagsforgtag(.....).<br>
 * It is used to search for folders which are associated twice to a given global
 * tag.
 * 
 * <b>QUERY_COMA_FINDGTAGS_TAGS_TRACE [coma_select_pkg]</b><br>
 * This query takes as arguments the SCHEMA, DB, GTAG and retrieves a list of
 * matching nodes/tags/gtags; it uses internally the function
 * cool_select_pkg.f_getall_tagsforgtag(.....).<br>
 * It is the same as the first query described, but the info is retrieved from
 * COMA.
 * </p>
 * 
 * @author formica
 * 
 * @since 2014/12/01.
 * 
 * @version 1.0
 * 
 */
public class GtagTagType implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2990371156354454615L;

	private Long gtagId;
	private String gtagName;
	private String gtagDescription;
	private Integer gtagLockStatus;
	private Long tagId;
	private String tagName;
	private String tagDescription;
	private Integer tagLockStatus;
	private String sysInstime;

	private String schemaName;
	private String nodeFullpath;
	private String nodeDescription;

	/**
	 * @return the gtagId
	 */
	public final Long getGtagId() {
		return gtagId;
	}

	/**
	 * @param gtagId
	 *            the gtagId to set
	 */
	public final void setGtagId(final Long gtagId) {
		this.gtagId = gtagId;
	}

	/**
	 * @return the gtagName
	 */
	public final String getGtagName() {
		return gtagName;
	}

	/**
	 * @param gtagName
	 *            the gtagName to set
	 */
	public final void setGtagName(final String gtagName) {
		this.gtagName = gtagName;
	}

	/**
	 * @return the gtagDescription
	 */
	public final String getGtagDescription() {
		return gtagDescription;
	}

	/**
	 * @param gtagDescription
	 *            the gtagDescription to set
	 */
	public final void setGtagDescription(final String gtagDescription) {
		this.gtagDescription = gtagDescription;
	}

	/**
	 * @return the gtagLockStatus
	 */
	public final Integer getGtagLockStatus() {
		return gtagLockStatus;
	}

	/**
	 * @param gtagLockStatus
	 *            the gtagLockStatus to set
	 */
	public final void setGtagLockStatus(final Integer gtagLockStatus) {
		this.gtagLockStatus = gtagLockStatus;
	}

	/**
	 * @return the tagId
	 */
	public final Long getTagId() {
		return tagId;
	}

	/**
	 * @param tagId
	 *            the tagId to set
	 */
	public final void setTagId(final Long tagId) {
		this.tagId = tagId;
	}

	/**
	 * @return the tagName
	 */
	public final String getTagName() {
		return tagName;
	}

	/**
	 * @param tagName
	 *            the tagName to set
	 */
	public final void setTagName(final String tagName) {
		this.tagName = tagName;
	}

	/**
	 * @return the tagDescription
	 */
	public final String getTagDescription() {
		return tagDescription;
	}

	/**
	 * @return the tagLockStatus
	 */
	public final Integer getTagLockStatus() {
		return tagLockStatus;
	}

	/**
	 * @param tagLockStatus
	 *            the tagLockStatus to set
	 */
	public final void setTagLockStatus(final Integer tagLockStatus) {
		this.tagLockStatus = tagLockStatus;
	}

	/**
	 * @param tagDescription
	 *            the tagDescription to set
	 */
	public final void setTagDescription(final String tagDescription) {
		this.tagDescription = tagDescription;
	}

	/**
	 * @return the sysInstime
	 */
	public final String getSysInstime() {
		return sysInstime;
	}

	/**
	 * @param sysInstime
	 *            the sysInstime to set
	 */
	public final void setSysInstime(final String sysInstime) {
		this.sysInstime = sysInstime;
	}


	/**
	 * @return the nodeFullpath
	 */
	public final String getNodeFullpath() {
		return nodeFullpath;
	}

	/**
	 * @param nodeFullpath
	 *            the nodeFullpath to set
	 */
	public final void setNodeFullpath(final String nodeFullpath) {
		this.nodeFullpath = nodeFullpath;
	}

	/**
	 * @return
	 */
	public String getNodeDescription() {
		return nodeDescription;
	}

	/**
	 * @param nodeDescription
	 */
	public void setNodeDescription(String nodeDescription) {
		this.nodeDescription = nodeDescription;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

}
