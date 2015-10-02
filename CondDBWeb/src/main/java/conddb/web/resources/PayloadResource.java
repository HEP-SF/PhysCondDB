
package conddb.web.resources;

import java.sql.Timestamp;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Payload;
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
			put("data",new Link(getFullyQualifiedContextPath(info), payload.getData()));
		}
	}

	public void serializeTimestamps(TimestampFormat tsformat) {
		this.tsformat = tsformat;
		Timestamp ts = (Timestamp) get("insertiontime");
		if (ts != null) {
			String tsstr = format(ts);
			put("insertiontime", tsstr);
		}
	}
}
