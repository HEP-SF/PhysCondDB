
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
import conddb.data.SystemDescription;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class SystemDescriptionResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public SystemDescriptionResource(UriInfo info, SystemDescription system) {
		super(info, system);
		put("id", system.getId());
		put("href", system.getResId());
		put("nodeFullpath", system.getNodeFullpath());
		put("nodeDescription", system.getNodeDescription());
		put("schemaName", system.getSchemaName());
		put("groupSize", system.getGroupSize());
		put("tagNameRoot", system.getTagNameRoot());
		if (system.getTags() != null) {
			put("tags", system.getTags());
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
