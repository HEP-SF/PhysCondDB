
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
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class GlobalTagResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GlobalTagResource(UriInfo info, GlobalTag globaltag) {
		super(info, globaltag);
		put("name", globaltag.getName());
		put("description", globaltag.getDescription());
		put("insertiontime", globaltag.getInsertionTime());
		put("lockstatus", globaltag.getLockstatus());
		put("release", globaltag.getRelease());
		put("snapshottime", globaltag.getSnapshotTime());
		put("validity", globaltag.getValidity());
		
		CollectionResource mapsresource = new CollectionResource(info,Link.GLOBALTAGMAPS+"/trace?type=globaltag&id="+globaltag.getName(), Collections.emptyList());
		put("maps",mapsresource);
	}

	public void serializeTimestamps(TimestampFormat tsformat) {
		this.tsformat = tsformat;
		Timestamp ts = (Timestamp) get("insertiontime");
		String tsstr = format(ts);
		if (tsstr != null)
			put("insertiontime", format(ts));
		ts = (Timestamp) get("snapshottime");
		tsstr = format(ts);
		if (tsstr != null)
			put("snapshottime", format(ts));
	}
}
