
package conddb.web.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTagMap;
import conddb.data.Tag;

@SuppressWarnings("unchecked")
public class TagResource extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5874322478554484276L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public TagResource(UriInfo info, Tag tag) {
		super(info, tag);
		build(info, tag);
	}

	protected void build(UriInfo info, Tag tag) {
		put("id", tag.getId());
		put("name", tag.getName());
		put("description", tag.getDescription());
		put("insertionTime", tag.getInsertionTime());
		put("endOfValidity", tag.getEndOfValidity());
		put("lastValidatedTime", tag.getLastValidatedTime());
		put("modificationTime", tag.getModificationTime());
		put("synchronization", tag.getSynchronization());
		put("objectType", tag.getObjectType());
		put("timeType", tag.getTimeType());
		CollectionResource mapsresource = null;
		CollectionResource iovsresource = null;
		try {
			log.debug("Loading iovs....");
			if (tag.getIovs() != null && tag.getIovs().size() > 0) {
				iovsresource = new CollectionResource(info, Link.IOVS, tag.getIovs());
			}
		} catch (org.hibernate.LazyInitializationException e) {
			// e.printStackTrace();
			log.debug("LazyInitialization Exception from hibernate: iov list is empty");
		}
		if (iovsresource == null) {
			iovsresource = new CollectionResource(info, Link.IOVS + "/find?tag=" + tag.getName(),
					Collections.emptyList());
		}
		try {
			log.debug("Loading maps....");
			if (tag.getGlobalTagMaps() != null && tag.getGlobalTagMaps().size() > 0) {
				Set<GlobalTagMap> maps = tag.getGlobalTagMaps();
				Collection<GlobalTagMapResource> items = new ArrayList<GlobalTagMapResource>(maps.size());
				for (GlobalTagMap globaltagmap : maps) {
					globaltagmap.setResId(globaltagmap.getId().toString());
					globaltagmap.getGlobalTag().setResId(globaltagmap.getGlobalTagName());
					globaltagmap.getSystemTag().setResId(globaltagmap.getTagName());
					GlobalTagMapResource mapresource = new GlobalTagMapResource(info, globaltagmap, tag);
					items.add(mapresource);
				}
				mapsresource = new CollectionResource(info, Link.GLOBALTAGMAPS, items);
				mapsresource.remove("systemTag");
			}
		} catch (org.hibernate.LazyInitializationException e) {
			// e.printStackTrace();
			log.debug("LazyInitialization Exception from hibernate: maps collection is empty");
		}
		if (mapsresource == null) {
			mapsresource = new CollectionResource(info, Link.GLOBALTAGMAPS + "/trace?type=tag&id=" + tag.getName(),
					Collections.emptyList());
		}
		put("globalTagMaps", mapsresource);
		put("iovs", iovsresource);
		this.serializeTimestamps();
	}
}
