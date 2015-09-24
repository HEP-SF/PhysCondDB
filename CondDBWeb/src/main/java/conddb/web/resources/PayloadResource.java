
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
import conddb.data.Payload;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class PayloadResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PayloadResource(UriInfo info, Payload payload) {
		super(info, payload);
		put("hash", payload.getHash());
		put("backendInfo", payload.getBackendInfo());
		put("objectType", payload.getObjectType());
		put("streamerInfo", payload.getStreamerInfo());
		put("version", payload.getVersion());
		put("datasize", payload.getDatasize());
		put("insertiontime", payload.getInsertionTime());
		if (payload.getData() != null) {
			put("data", payload.getData().getData());
		}
	}

	public void serializeTimestamps(TimestampFormat tsformat) {
		this.tsformat = tsformat;
		Timestamp ts = (Timestamp) get("insertiontime");
		String tsstr = format(ts);
		if (tsstr != null)
			put("insertiontime", format(ts));
	}
}
