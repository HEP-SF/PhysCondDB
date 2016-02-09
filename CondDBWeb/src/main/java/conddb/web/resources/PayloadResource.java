
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Payload;

@SuppressWarnings("unchecked")
public class PayloadResource extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6592964075205126409L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PayloadResource(UriInfo info, Payload payload) {
		super(info, payload);
		build(info, payload);
	}

	protected void build(UriInfo info, Payload payload) {
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
		this.serializeTimestamps();
	}
}
