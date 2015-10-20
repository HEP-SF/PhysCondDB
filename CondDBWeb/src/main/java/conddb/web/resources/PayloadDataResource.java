
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.PayloadData;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class PayloadDataResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PayloadDataResource(UriInfo info, PayloadData payloaddata, TimestampFormat tsformat) {
		super(info, payloaddata);
		build(info, payloaddata, tsformat);
	}

	public PayloadDataResource(UriInfo info, PayloadData payloaddata) {
		super(info, payloaddata);
		build(info, payloaddata, null);
	}

	protected void build(UriInfo info, PayloadData payloaddata, TimestampFormat tsformat) {
		this.tsformat = tsformat;
		put("hash", payloaddata.getHash());
		put("uri", payloaddata.getUri());
	}

}
