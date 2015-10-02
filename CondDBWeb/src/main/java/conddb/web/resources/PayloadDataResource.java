
package conddb.web.resources;

import java.sql.Timestamp;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.PayloadData;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class PayloadDataResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PayloadDataResource(UriInfo info, PayloadData payloaddata) {
		super(info, payloaddata);
		put("hash", payloaddata.getHash());
		put("uri",payloaddata.getUri());
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
