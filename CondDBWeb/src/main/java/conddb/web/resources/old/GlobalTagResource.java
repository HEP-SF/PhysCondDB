
package conddb.web.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.utils.collections.CollectionUtils;

@SuppressWarnings("unchecked")
public class GlobalTagResource extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = 999263344224387632L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GlobalTagResource(UriInfo info, GlobalTag globaltag) {
		super(info, globaltag);
		build(info, globaltag);
	}

	protected void build(UriInfo info, GlobalTag globaltag) {
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
				Collection<GlobalTagMap> globaltagmaps = CollectionUtils
						.iterableToCollection(globaltag.getGlobalTagMaps());
				Collection<GlobalTagMapResource> items = new ArrayList<GlobalTagMapResource>(globaltagmaps.size());
				for (GlobalTagMap globaltagmap : globaltagmaps) {
					globaltagmap.setResId(globaltagmap.getId().toString());
					globaltagmap.getGlobalTag().setResId(globaltagmap.getGlobalTagName());
					globaltagmap.getSystemTag().setResId(globaltagmap.getTagName());
					GlobalTagMapResource resource = new GlobalTagMapResource(info, (GlobalTagMap) globaltagmap, globaltag);
					resource.remove("globalTag");
					items.add(resource);
				}
				mapsresource = new CollectionResource(info,
						Link.GLOBALTAGMAPS + "/trace?type=globaltag&id=" + globaltag.getName(), items, 0, items.size());
			}
		} catch (org.hibernate.LazyInitializationException e) {
			log.debug("LazyInitialization Exception from hibernate: maps collection is empty ");
		}
		if (mapsresource == null) {
			mapsresource = new CollectionResource(info,
					Link.GLOBALTAGMAPS + "/trace?type=globaltag&id=" + globaltag.getName(), Collections.emptyList());
		}
		put("globalTagMaps", mapsresource);
		this.serializeTimestamps();
	}

}
