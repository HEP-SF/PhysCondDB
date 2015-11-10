
package conddb.web.resources;

import java.sql.Timestamp;
import java.util.Collections;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class GlobalTagMapResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap, TimestampFormat tsformat) {
		super(info, globaltagmap);
		build(info,globaltagmap,tsformat);
	}
	
	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap) {
		super(info, globaltagmap);
		build(info,globaltagmap,null);
	}
	
	protected void build(UriInfo info, GlobalTagMap globaltagmap, TimestampFormat tsformat) {
		this.tsformat = tsformat;
		put("globalTagName", globaltagmap.getGlobalTagName());
		put("tagName", globaltagmap.getTagName());
		put("label", globaltagmap.getLabel());
		put("record", globaltagmap.getRecord());
		TagResource tagres = null;
		try {
			log.debug("Determine tag: " + globaltagmap.getSystemTag());
			if (globaltagmap.getSystemTag() != null) {
				Tag systag = globaltagmap.getSystemTag();
				systag.setResId(systag.getName());
				if (systag.getGlobalTagMaps() != null) {
					systag.setGlobalTagMaps(null);
				}
				//tagres = new TagResource(info, (Tag) systag, this.tsformat);
			} 
		} catch (org.hibernate.LazyInitializationException e) {
			log.debug("LazyInitialization Exception from hibernate: map does not have a system tag loaded");
		}
		if (tagres == null) {
			Tag systag = globaltagmap.getSystemTag();
			systag.setResId(systag.getName());
			put("systemTag", new Link(getFullyQualifiedContextPath(info), systag));		
		} else {
			// This is for the moment never accessed...we probably should remove it
			// FIXME: remove dead code
			put("systemTag", tagres);
		}
		GlobalTag gtag = globaltagmap.getGlobalTag();
		gtag.setResId(gtag.getName());
		put("globalTag", new Link(getFullyQualifiedContextPath(info), gtag));		
		this.serializeTimestamps(this.tsformat);
	}
}
