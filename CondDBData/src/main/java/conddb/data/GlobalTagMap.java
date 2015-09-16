package conddb.data;

// Generated Aug 25, 2014 4:52:00 PM by Hibernate Tools 3.4.0.CR1

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * GlobalTagMap generated by hbm2java
 */
@Entity
@Table(name = "PHCOND_GLOBAL_TAG_MAP",
	uniqueConstraints=@UniqueConstraint(columnNames={"TAG_ID", "GLOBAL_TAG_NAME"}))
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="id", scope = GlobalTagMap.class)
public class GlobalTagMap extends conddb.data.Entity implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5242844765295102068L;
	private Long id;
	private Tag systemTag;
	private GlobalTag globalTag;
	private String record;
	private String label;
	public GlobalTagMap() {
	}

	public GlobalTagMap(GlobalTag gtag, Tag stag) {
		this.globalTag=gtag;
		this.systemTag=stag;
		this.record="none";
		this.label="none";
	}

	/**
	 * @param systemTag
	 * 	The system tag.
	 * @param globalTag
	 * 	The global tag.
	 * @param record
	 * 	The record.
	 * @param label
	 * 	The label.
	 */
	public GlobalTagMap(GlobalTag globalTag, Tag systemTag, String record,
			String label) {
		super();
		this.systemTag = systemTag;
		this.globalTag = globalTag;
		this.record = record;
		this.label = label;
	}

	/**
	 * @return the ID of the map.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	public Long getId() {
		return this.id;
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
	 * 	The global tag object.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "GLOBAL_TAG_NAME", nullable=false)
	public GlobalTag getGlobalTag() {
		return this.globalTag;
	}
	
	
	/**
	 * @param globalTag
	 * 	The global tag to set.
	 */
	public void setGlobalTag(GlobalTag globalTag) {
		this.globalTag = globalTag;
	}
	
	/**
	 * @return
	 * 	The tag object.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "TAG_ID", nullable=false)
	public Tag getSystemTag() {
		return systemTag;
	}
	
	/**
	 * @param systemTag
	 * 	The system tag to set.
	 */
	public void setSystemTag(Tag systemTag) {
		this.systemTag = systemTag;
	}

	/**
	 * @return
	 * 	The record.
	 */
	@Column(name = "RECORD", nullable = false, length = 100)
	public String getRecord() {
		return this.record;
	}

	/**
	 * @param record
	 * 	The record to set.
	 */
	public void setRecord(String record) {
		this.record = record;
	}

	/**
	 * @return
	 * 	The label.
	 */
	@Column(name = "LABEL", nullable = false, length = 100)
	public String getLabel() {
		return this.label;
	}

	/**
	 * @param label
	 * 	The label to set.
	 */
	public void setLabel(String label) {
		this.label = label;
	}

	/**
	 * @return
	 * 	The tag name.
	 */
	@Transient
	public String getTagName() {
		return this.systemTag.getName();
	}
	
	/**
	 * @return
	 * 	The global tag name.
	 */
	@Transient
	public String getGlobalTagName() {
		return this.globalTag.getName();
	}


}
