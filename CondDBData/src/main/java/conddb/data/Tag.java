package conddb.data;

// Generated Aug 25, 2014 4:52:00 PM by Hibernate Tools 3.4.0.CR1

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import conddb.utils.json.serializers.TimestampDeserializer;

/**
 * Tag generated by hbm2java
 */
@Entity
@Table(name = "TAG")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Tag implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1912445397860376582L;

	private Long id;

	private String name;
	private String timeType;
	private String objectType;
	private String synchronization;
	private String description;
	private BigDecimal lastValidatedTime;
	private BigDecimal endOfValidity;
	private Timestamp insertionTime;
	private Timestamp modificationTime;
	private Set<Iov> iovs = new HashSet<Iov>(0);
	private Set<GlobalTagMap> globalTagMaps = new HashSet<GlobalTagMap>(0);

	/**
	 * Default ctor.
	 */
	public Tag() {
	}

	/**
	 * Constructor using tag name.
	 * @param name
	 * 	The tag name.
	 */
	public Tag(String name) {
		super();
		this.name = name;
	}

	/**
	 * @param name
	 * 	The tag name.
	 * @param timeType
	 * 	The time type.
	 * @param objectType
	 * 	The object type.
	 * @param synchronization
	 * 	The synchronization parameter.
	 * @param description
	 * 	The tag description.
	 * @param lastValidatedTime
	 * 	The last validated time.
	 * @param endOfValidity
	 * 	The end of validity.
	 */
	public Tag(String name, String timeType, String objectType,
			String synchronization, String description, BigDecimal lastValidatedTime,
			BigDecimal endOfValidity) {
		this.name = name;
		this.timeType = timeType;
		this.objectType = objectType;
		this.synchronization = synchronization;
		this.description = description;
		this.lastValidatedTime = lastValidatedTime;
		this.endOfValidity = endOfValidity;
	}

	/**
	 * @param name
	 * 	The tag name.
	 * @param timeType
	 * 	The time type.
	 * @param objectType
	 * 	The object type.
	 * @param synchronization
	 * 	The synchronization parameter.
	 * @param description
	 * 	The tag description.
	 * @param lastValidatedTime
	 * 	The last validated time.
	 * @param endOfValidity
	 * 	The end of validity.
	 * @param modificationTime
	 * 	The modification time.
	 * @param insertionTime
	 * 	The insertion time.
	 * @param iovs
	 * 	The list of iovs associated.
	 * @param globalTagMaps
	 * 	The list of tags associated.
	 */
	public Tag(String name, String timeType, String objectType,
			String synchronization, String description, BigDecimal lastValidatedTime,
			BigDecimal endOfValidity, Timestamp insertionTime,
			Timestamp modificationTime, Set<Iov> iovs,
			Set<GlobalTagMap> globalTagMaps) {
		this.name = name;
		this.timeType = timeType;
		this.objectType = objectType;
		this.synchronization = synchronization;
		this.description = description;
		this.lastValidatedTime = lastValidatedTime;
		this.endOfValidity = endOfValidity;
		this.insertionTime = insertionTime;
		this.modificationTime = modificationTime;
		this.iovs = iovs;
		this.globalTagMaps = globalTagMaps;
	}

	/**
	 * @return
	 * 	The ID.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "TAG_ID")
	public Long getId() {
		return this.id;
	}

	/**
	 * @param id
	 * 	the ID to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return
	 * 	The tag name.
	 */
	@Column(name = "NAME", unique = true, nullable = false, length = 255)
	public String getName() {
		return this.name;
	}

	/**
	 * @param name
	 * 	The tag name to set.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return
	 * 	The time type.
	 */
	@Column(name = "TIME_TYPE", nullable = false, length = 20)
	public String getTimeType() {
		return this.timeType;
	}

	/**
	 * @param timeType
	 * 	The time type to set.
	 */
	public void setTimeType(String timeType) {
		this.timeType = timeType;
	}

	/**
	 * @return
	 * 	The object type used in the BLOB.
	 */
	@Column(name = "OBJECT_TYPE", nullable = false, length = 255)
	public String getObjectType() {
		return this.objectType;
	}

	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@Column(name = "SYNCHRONIZATION", nullable = false, length = 255)
	public String getSynchronization() {
		return this.synchronization;
	}

	public void setSynchronization(String synchronization) {
		this.synchronization = synchronization;
	}

	@Column(name = "DESCRIPTION", nullable = false, length = 4000)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}


	@Column(name = "LAST_VALIDATED_TIME", nullable = false, precision = 22, scale = 0)
	public BigDecimal getLastValidatedTime() {
		return this.lastValidatedTime;
	}

	public void setLastValidatedTime(BigDecimal lastValidatedTime) {
		this.lastValidatedTime = lastValidatedTime;
	}

	@Column(name = "END_OF_VALIDITY", nullable = false, precision = 22, scale = 0)
	public BigDecimal getEndOfValidity() {
		return this.endOfValidity;
	}

	public void setEndOfValidity(BigDecimal endOfValidity) {
		this.endOfValidity = endOfValidity;
	}

//	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "INSERTION_TIME", nullable = true)
	@JsonDeserialize(using = TimestampDeserializer.class)
	public Timestamp getInsertionTime() {
		return this.insertionTime;
	}

	public void setInsertionTime(Timestamp insertionTime) {
		this.insertionTime = insertionTime;
	}

//	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "MODIFICATION_TIME", nullable = true)
	@JsonDeserialize(using = TimestampDeserializer.class)
	public Timestamp getModificationTime() {
		return this.modificationTime;
	}

	public void setModificationTime(Timestamp modificationTime) {
		this.modificationTime = modificationTime;
	}

//	@JsonManagedReference
//	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "tag")
	public Set<Iov> getIovs() {
		return this.iovs;
	}

	public void setIovs(Set<Iov> iovs) {
		this.iovs = iovs;
	}

	// @//JsonManagedReference(value="tag-map")
//	@JsonIgnore
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "systemTag")
	public Set<GlobalTagMap> getGlobalTagMaps() {
		return this.globalTagMaps;
	}

	public void setGlobalTagMaps(Set<GlobalTagMap> globalTagMaps) {
		this.globalTagMaps = globalTagMaps;
	}

	@PrePersist
    public void prePersist() {
        Timestamp now = new Timestamp(new Date().getTime());
        this.insertionTime = now;
        this.modificationTime = now;
    }

	@PreUpdate
    public void preUpdate() {
        Timestamp now = new Timestamp(new Date().getTime());
        this.modificationTime = now;
    }

}
