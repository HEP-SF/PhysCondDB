
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Payload;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class PayloadResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PayloadResource(UriInfo info, Payload payload, TimestampFormat tsformat) {
		super(info, payload);
		build(info, payload, tsformat);
	}

	public PayloadResource(UriInfo info, Payload payload) {
		super(info, payload);
		build(info, payload, null);
	}

	protected void build(UriInfo info, Payload payload, TimestampFormat tsformat) {
		put("hash", payload.getHash());
		put("backendInfo", payload.getBackendInfo());
		put("objectType", payload.getObjectType());
		put("streamerInfo", payload.getStreamerInfo());
		put("version", payload.getVersion());
		put("datasize", payload.getDatasize());
		put("insertionTime", payload.getInsertionTime());
//		if (payload.getData() != null) {
			put("data", new Link(info,Link.PAYLOADDATA + "/"+payload.getHash()));
//		}
		this.serializeTimestamps(tsformat);
	}
}
