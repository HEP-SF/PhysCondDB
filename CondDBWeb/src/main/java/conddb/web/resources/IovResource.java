
package conddb.web.resources;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTag;
import conddb.data.Iov;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class IovResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public IovResource(UriInfo info, Iov iov) {
		super(info, iov);
		put("id", iov.getId());
		put("since", iov.getSince());
		put("sincestr", iov.getSinceString());
		put("insertiontime", iov.getInsertionTime());
		
		put("payload",new Link(getFullyQualifiedContextPath(info), iov.getPayload()));
		put("tag",new Link(getFullyQualifiedContextPath(info), iov.getTag()));
	}

	public void serializeTimestamps(TimestampFormat tsformat) {
		this.tsformat = tsformat;
		Timestamp ts = (Timestamp) get("insertiontime");
		String tsstr = format(ts);
		if (tsstr != null)
			put("insertiontime", format(ts));
		ts = (Timestamp) get("modificationtime");
		tsstr = format(ts);
		if (tsstr != null)
			put("modificationtime", format(ts));
	}
}
