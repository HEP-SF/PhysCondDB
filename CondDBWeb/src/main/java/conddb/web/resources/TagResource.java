
package conddb.web.resources;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class TagResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public TagResource(UriInfo info, Tag tag) {
		super(info, tag);
		put("name", tag.getName());
		put("description", tag.getDescription());
		put("insertiontime", tag.getInsertionTime());
		put("lockstatus", tag.getEndOfValidity());
		put("lastvalidatedtime", tag.getLastValidatedTime());
		put("modificationtime", tag.getModificationTime());
		CollectionResource mapsresource = null;
		CollectionResource iovsresource = null;
		try {
			log.debug("Loading iovs....");
			if (tag.getIovs() != null) {
				iovsresource = new CollectionResource(info, Link.IOVS, tag.getIovs());
			} else {
				iovsresource = new CollectionResource(info, Link.IOVS + "/find?tag=" + tag.getName(),
						Collections.emptyList());
			}
		} catch (org.hibernate.LazyInitializationException e) {
			//e.printStackTrace();
			log.debug("LazyInitialization Exception from hibernate: iov list is empty");
			iovsresource = new CollectionResource(info, Link.IOVS + "/find?tag=" + tag.getName(),
					Collections.emptyList());
		}
		try {
			log.debug("Loading maps....");
			if (tag.getGlobalTagMaps() != null) {
				Set<GlobalTagMap> maps = tag.getGlobalTagMaps();
		        Collection items = new ArrayList(maps.size());
		        for( GlobalTagMap globaltagmap : maps) {
		        	globaltagmap.setResId(globaltagmap.getId().toString());
		        	globaltagmap.getGlobalTag().setResId(globaltagmap.getGlobalTagName());
		        	globaltagmap.getSystemTag().setResId(globaltagmap.getTagName());
		            items.add(new GlobalTagMapResource(info, globaltagmap));
		        }
				mapsresource = new CollectionResource(info, Link.GLOBALTAGMAPS, items);
			}
		} catch (org.hibernate.LazyInitializationException e) {
			//e.printStackTrace();
			log.debug("LazyInitialization Exception from hibernate: maps collection is empty");
			mapsresource = new CollectionResource(info, Link.GLOBALTAGMAPS + "/trace?type=tag&id=" + tag.getName(),
					Collections.emptyList());
		}
		put("globalTagMaps", mapsresource);
		put("iovs", iovsresource);
	}

	public void serializeTimestamps(TimestampFormat tsformat) {
		this.tsformat = tsformat;
		Timestamp ts = (Timestamp) get("insertiontime");
		if (ts != null) {
			String tsstr = format(ts);
			put("insertiontime", tsstr);
		}
		ts = (Timestamp) get("modificationtime");
		if (ts != null) {
			String tsstr = format(ts);
			put("modificationtime", tsstr);
		}
	}
}
