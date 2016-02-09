
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.PayloadData;

@SuppressWarnings("unchecked")
public class PayloadDataResource extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4104866798448462505L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public PayloadDataResource(UriInfo info, PayloadData payloaddata) {
		super(info, payloaddata);
		build(info, payloaddata);
	}

	protected void build(UriInfo info, PayloadData payloaddata) {
		put("hash", payloaddata.getHash());
		put("uri", payloaddata.getUri());
	}

}
