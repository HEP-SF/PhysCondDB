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
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.DateSerializer;

import conddb.utils.json.serializers.TimestampDeserializer;

/**
 * GlobalTag generated by hbm2java
 */
@Entity
@Table(name = "PHCOND_GLOBAL_TAG")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "name")
public class GlobalTag implements java.io.Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 996548260134268579L;
	private String name;
	private BigDecimal validity;
	private String description;
	
	/**
	 * Global tags can be related to a given software release
	 */
	private String release; 
	private String lockstatus;
	private Timestamp insertionTime;
	private Timestamp snapshotTime;
	private Set<GlobalTagMap> globalTagMaps = new HashSet<GlobalTagMap>(0);

	public GlobalTag() {
	}

	/**
	 * @param name
	 */
	public GlobalTag(String name) {
		super();
		this.name = name;
	}

	public GlobalTag(String name, BigDecimal validity, String description,
			String release, Timestamp insertionTime, Timestamp snapshotTime) {
		this.name = name;
		this.validity = validity;
		this.description = description;
		this.release = release;
		this.insertionTime = insertionTime;
		this.snapshotTime = snapshotTime;
		this.lockstatus="unlocked";
	}

	public GlobalTag(String name, BigDecimal validity, String description,
			String release, Timestamp insertionTime, Timestamp snapshotTime,
			Set<GlobalTagMap> globalTagMaps) {
		this.name = name;
		this.validity = validity;
		this.description = description;
		this.release = release;
		this.insertionTime = insertionTime;
		this.snapshotTime = snapshotTime;
		this.globalTagMaps = globalTagMaps;
		this.lockstatus="unlocked";
	}

	@Id
	@Column(name = "NAME", unique = true, nullable = false, length = 100)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "VALIDITY", nullable = false, precision = 22, scale = 0)
	public BigDecimal getValidity() {
		return this.validity;
	}

	public void setValidity(BigDecimal validity) {
		this.validity = validity;
	}

	@Column(name = "DESCRIPTION", nullable = false, length = 4000)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "RELEASE", nullable = false, length = 100)
	public String getRelease() {
		return this.release;
	}

	public void setRelease(String release) {
		this.release = release;
	}

	@Column(name = "LOCKSTATUS", nullable = false, length = 20)
	public String getLockstatus() {
		return lockstatus;
	}

	public void setLockstatus(String lockstatus) {
		this.lockstatus = lockstatus;
	}

	@Transient
	public boolean islocked() {
		if (this.lockstatus.equals("unlocked")) {
			return false; 
		} else {
			return true;
		}
	}
	
	@Transient
	public void lock() {
		this.lockstatus = "locked";
	}
	
	@JsonSerialize(using = DateSerializer.class)
	@JsonDeserialize(using = TimestampDeserializer.class)
	@Column(name = "INSERTION_TIME", nullable = true)
	public Timestamp getInsertionTime() {
		return this.insertionTime;
	}

	public void setInsertionTime(Timestamp insertionTime) {
		this.insertionTime = insertionTime;
	}

	@JsonSerialize(using = DateSerializer.class)
	@JsonDeserialize(using = TimestampDeserializer.class)
	// @Temporal(TemporalType.TIMESTAMP)
	@Column(name = "SNAPSHOT_TIME", nullable = true)
	public Timestamp getSnapshotTime() {
		return this.snapshotTime;
	}

	public void setSnapshotTime(Timestamp snapshotTime) {
		this.snapshotTime = snapshotTime;
	}

	// @//JsonManagedReference(value="gtag-map")
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "globalTag")
	@Column(nullable = true)
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
    }
 
}
