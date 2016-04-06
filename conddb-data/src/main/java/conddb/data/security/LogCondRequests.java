/**
 * 
 */
package conddb.data.security;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import conddb.data.utils.json.serializers.TimestampDeserializer;
import conddb.data.utils.json.serializers.TimestampSerializer;

/**
 * @author aformic
 *
 */
@Entity
@Table(name = "PHCOND_SECURITY_LOGREQUESTS")
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = LogCondRequests.class)
public class LogCondRequests  extends conddb.data.Entity implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8833064058310492100L;
	
	
	private Long id;
	private String userName;
	private String userRole;
	private String serverHost;
	private String remoteHost;
	private String requestUrl;
	private String requestHeader;
	private String httpMethod;
	private Timestamp start;
	private Timestamp end;
	private Timestamp insertionTime;
	private Long lengthMilli;
	
	
	public LogCondRequests() {
		super();
	}


	public LogCondRequests(String userName, String userRole, String serverHost, String remoteHost, String requestUrl, String requestHeader,
			String httpMethod, Timestamp start, Timestamp end) {
		super();
		this.userName = userName;
		this.userRole = userRole;
		this.serverHost = serverHost;
		this.remoteHost = remoteHost;
		this.requestUrl = requestUrl;
		this.requestHeader = requestHeader;
		this.httpMethod = httpMethod;
		this.start = start;
		this.end = end;
		this.lengthMilli = end.getTime() - start.getTime();
	}


	/**
	 * @return the ID of the map.
	 */
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@Column(name = "REQ_ID", nullable=false, precision = 22, scale = 0)
	public Long getId() {
		return id;
	}


	public void setId(Long id) {
		this.id = id;
		this.setResId(id.toString());
	}


	@Column(name = "USER_NAME", nullable = false, length = 20)
	public String getUserName() {
		return userName;
	}


	public void setUserName(String userName) {
		this.userName = userName;
	}


	@Column(name = "USER_ROLE", nullable = true, length = 20)
	public String getUserRole() {
		return userRole;
	}


	public void setUserRole(String userRole) {
		this.userRole = userRole;
	}


	@Column(name = "SERVER_HOST", nullable = true, length = 100)
	public String getServerHost() {
		return serverHost;
	}


	public void setServerHost(String serverHost) {
		this.serverHost = serverHost;
	}


	@Column(name = "REMOTE_HOST", nullable = true, length = 50)
	public String getRemoteHost() {
		return remoteHost;
	}


	public void setRemoteHost(String remoteHost) {
		this.remoteHost = remoteHost;
	}


	@Column(name = "REQUEST_URL", nullable = false, length = 1000)
	public String getRequestUrl() {
		return requestUrl;
	}


	public void setRequestUrl(String requestUrl) {
		this.requestUrl = requestUrl;
	}


	@Column(name = "REQUEST_HEADER", nullable = true)
	@Lob @Basic(fetch=FetchType.LAZY)
	public String getRequestHeader() {
		return requestHeader;
	}


	public void setRequestHeader(String requestHeader) {
		this.requestHeader = requestHeader;
	}


	@Column(name = "HTTP_METHOD", nullable = false, length = 20)
	public String getHttpMethod() {
		return httpMethod;
	}


	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}


	@JsonDeserialize(using = TimestampDeserializer.class)
	@JsonSerialize(using = TimestampSerializer.class)
	@Column(name = "START_TIME", nullable = false)
	public Timestamp getStart() {
		return start;
	}


	public void setStart(Timestamp start) {
		this.start = start;
	}


	@JsonDeserialize(using = TimestampDeserializer.class)
	@JsonSerialize(using = TimestampSerializer.class)
	@Column(name = "END_TIME", nullable = false)
	public Timestamp getEnd() {
		return end;
	}


	public void setEnd(Timestamp end) {
		this.end = end;
	}

	@Column(name = "REQ_TIME_MILLI", nullable = true)
	public Long getLengthMilli() {
		return lengthMilli;
	}


	public void setLengthMilli(Long lengthMilli) {
		this.lengthMilli = lengthMilli;
	}


	@Column(name = "INSERTION_TIME", nullable = true,updatable=false)
  	@JsonSerialize(using = TimestampSerializer.class)
  	@JsonDeserialize(using = TimestampDeserializer.class)
	public Timestamp getInsertionTime() {
		return this.insertionTime;
	}

	public void setInsertionTime(Timestamp insertionTime) {
		this.insertionTime = insertionTime;
	}

	@PrePersist
    public void prePersist() {
        Timestamp now = new Timestamp(new Date().getTime());
        this.insertionTime = now;
        if (start != null && end != null) {
            this.lengthMilli = end.getTime()-start.getTime();
        }
    }

	@PreUpdate
    public void preUpdate() {
    }



	@Override
	public String toString() {
		return "LogCondRequests [id=" + id + ", userName=" + userName + ", userRole=" + userRole + ", remoteHost="
				+ remoteHost + ", requestUrl=" + requestUrl + ", requestHeader=" + requestHeader + ", httpMethod="
				+ httpMethod + ", start=" + start + ", end=" + end + "]";
	}
	
}
