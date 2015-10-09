
package conddb.web.resources;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.utils.collections.CollectionUtils;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class GlobalTagResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GlobalTagResource(UriInfo info, GlobalTag globaltag) {
		super(info, globaltag);
		put("name", globaltag.getName());
		put("description", globaltag.getDescription());
		put("insertionTime", globaltag.getInsertionTime());
		put("lockstatus", globaltag.getLockstatus());
		put("release", globaltag.getRelease());
		put("snapshotTime", globaltag.getSnapshotTime());
		put("validity", globaltag.getValidity());
		
		CollectionResource mapsresource = null;
		try {
			log.debug("Loading fetched tags....");
			if (globaltag.getGlobalTagMaps() != null) {
				Collection<GlobalTagMap> globaltagmaps = CollectionUtils.iterableToCollection(globaltag.getGlobalTagMaps());
		        Collection items = new ArrayList(globaltagmaps.size());
		        for( GlobalTagMap globaltagmap : globaltagmaps) {
		        	globaltagmap.setResId(globaltagmap.getId().toString());
		        	globaltagmap.getGlobalTag().setResId(globaltagmap.getGlobalTagName());
		        	globaltagmap.getSystemTag().setResId(globaltagmap.getTagName());
		        	GlobalTagMapResource resource = new GlobalTagMapResource(info,(GlobalTagMap)globaltagmap);
		            items.add(resource);
		        }
				mapsresource = new CollectionResource(info,Link.GLOBALTAGMAPS+"/trace?type=globaltag&id="+globaltag.getName(), items,0,items.size());

			}
		} catch (org.hibernate.LazyInitializationException e) {
			log.debug("LazyInitialization Exception from hibernate: maps collection is empty ");
			mapsresource = new CollectionResource(info,Link.GLOBALTAGMAPS+"/trace?type=globaltag&id="+globaltag.getName(), Collections.emptyList());
		}
		put("globalTagMaps",mapsresource);
	}

	public void serializeTimestamps(TimestampFormat tsformat) {
		this.tsformat = tsformat;
		Timestamp ts = (Timestamp) get("insertionTime");
		if (ts != null) {
			String tsstr = format(ts);
			put("insertionTime", tsstr);
		}
		ts = (Timestamp) get("snapshotTime");
		if (ts != null) {
			String tsstr = format(ts);
			put("snapshotTime", tsstr);
		}		
	}
}
