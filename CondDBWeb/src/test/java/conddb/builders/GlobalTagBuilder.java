/**
 * 
 */
package conddb.builders;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;

import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
public class GlobalTagBuilder implements ICondBuilder<GlobalTag>{
	
	private String name = new String("TEST_GTAG_01");
	private BigDecimal validity = new BigDecimal(0);
	private String description = new String("This is a test global tag");
	private String release = new String("none");
	private Timestamp instime = Timestamp.from(Instant.now());
	private Timestamp snaptime = Timestamp.from(Instant.now());
	
	public GlobalTagBuilder withName(String name) {
		this.name = name;
		return this;
	}
	public GlobalTagBuilder withValidity(BigDecimal validity) {
		this.validity = validity;
		return this;
	}
	public GlobalTagBuilder withDescription(String description) {
		this.description = description;
		return this;
	}
	public GlobalTagBuilder withRelease(String release) {
		this.release = release;
		return this;
	}
	public GlobalTagBuilder withSnapshotTime(Timestamp snaptime) {
		this.snaptime = snaptime;
		return this;
	}

	public GlobalTag build() {
		return new GlobalTag(name,validity,description,release,instime,snaptime);
	}
}
