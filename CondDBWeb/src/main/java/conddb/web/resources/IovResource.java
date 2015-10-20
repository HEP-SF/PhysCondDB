
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Iov;
import conddb.data.Payload;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class IovResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public IovResource(UriInfo info, Iov iov, TimestampFormat tsformat) {
		super(info, iov);
		build(info, iov, tsformat);
	}

	public IovResource(UriInfo info, Iov iov) {
		super(info, iov);
		build(info, iov, null);
	}

	protected void build(UriInfo info, Iov iov, TimestampFormat tsformat) {
		this.tsformat = tsformat;
		put("id", iov.getId());
		put("since", iov.getSince());
		put("sinceString", iov.getSinceString());
		put("insertionTime", iov.getInsertionTime());
		PayloadResource pyldres = null;
		try {
			log.debug("Determine payload: " + iov.getPayload());
			if (iov.getPayload() != null) {
				Payload payload = iov.getPayload();
				payload.setResId(payload.getHash());
				if (payload.getIovs() != null) {
					payload.setIovs(null);
				}
				pyldres = new PayloadResource(info, (Payload) payload, this.tsformat);
			} 
		} catch (org.hibernate.LazyInitializationException e) {
			log.debug("LazyInitialization Exception from hibernate: maps collection is empty");
		}
		if (pyldres == null) {
			pyldres = (PayloadResource) new Link(info, iov.getPayload());
		}
		put("payload", pyldres);
		Tag atag = iov.getTag();
		if (atag != null) {
			atag.setResId(atag.getName());
		}
		put("tag",new Link(info, atag));
		this.serializeTimestamps(this.tsformat);
	}
}
