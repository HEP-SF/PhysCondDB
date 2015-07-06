package conddb.data;

// Generated Aug 25, 2014 4:52:00 PM by Hibernate Tools 3.4.0.CR1

//import conddb.data.deserialiser.*;
//import conddb.mappers.deserializers.PayloadDeserializer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

/**
 * Payload generated by hbm2java
 */
@Entity
@Table(name = "PHCOND_PAYLOAD_DATA")
@JsonIdentityInfo(generator=ObjectIdGenerators.PropertyGenerator.class, property="hash")
public class PayloadData implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6319755970273574624L;
	private String hash;
	private byte[]  data;

	public PayloadData() {
		this.data = "EMPTY".getBytes();
	}

	public PayloadData(String hash, byte[] data) {
		this.hash = hash;
		this.data = data;
	}

	@Id
	@Column(name = "HASH", unique = true, nullable = false, length = 256)
	public String getHash() {
		return this.hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Column(name = "DATA", nullable = false)
	@Lob 
	public byte[] getData() {
		return this.data;
	}

	public void setData(byte[]  data) {
		this.data = data;
	}
	
}
