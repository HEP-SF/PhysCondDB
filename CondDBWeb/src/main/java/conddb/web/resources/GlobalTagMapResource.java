
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Entity;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class GlobalTagMapResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap, TimestampFormat tsformat) {
		super(info, globaltagmap);
		build(info, globaltagmap, tsformat);
	}

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap, Entity parent, TimestampFormat tsformat) {
		super(info, globaltagmap,parent,tsformat);
		build(info, globaltagmap, tsformat);
	}

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap) {
		super(info, globaltagmap);
		build(info, globaltagmap, null);
	}

	protected void build(UriInfo info, GlobalTagMap globaltagmap, TimestampFormat tsformat) {
		this.tsformat = tsformat;
		put("globalTagName", globaltagmap.getGlobalTagName());
		put("tagName", globaltagmap.getTagName());
		put("label", globaltagmap.getLabel());
		put("record", globaltagmap.getRecord());
		
		if (parent != null && parent instanceof Tag) {
			GlobalTagResource gtagres = null;
			try {
				GlobalTag gtag = globaltagmap.getGlobalTag();
				gtag.setResId(gtag.getName());
				gtagres = new GlobalTagResource(info, (GlobalTag) gtag, this.tsformat);
				// remove the map object since
				// we are already filling it
				// from maps.
				gtagres.remove("globalTagMaps"); 

			} catch (org.hibernate.LazyInitializationException e) {
				log.debug("LazyInitialization Exception from hibernate: map does not have a global tag loaded");
			}
			if (gtagres == null) {
				GlobalTag gtag = globaltagmap.getGlobalTag();
				gtag.setResId(gtag.getName());
				put("globalTag", new Link(getFullyQualifiedContextPath(info), gtag));
			} else {
				put("globalTag", gtagres);
			}			
			Tag systag = globaltagmap.getSystemTag();
			systag.setResId(systag.getName());
			put("systemTag", new Link(getFullyQualifiedContextPath(info), systag));

		} else if (parent != null && parent instanceof GlobalTag) {
			TagResource tagres = null;
			try {
				log.debug("Determine tag: " + globaltagmap.getSystemTag());
				if (globaltagmap.getSystemTag() != null) {
					Tag systag = globaltagmap.getSystemTag();
					systag.setResId(systag.getName());
					if (systag.getGlobalTagMaps() != null) {
						systag.setGlobalTagMaps(null);
					}
					tagres = new TagResource(info, (Tag) systag, this.tsformat);
					// remove the map object since
					// we are already filling it
					// from maps.
					tagres.remove("globalTagMaps"); 
				}
			} catch (org.hibernate.LazyInitializationException e) {
				log.debug("LazyInitialization Exception from hibernate: map does not have a system tag loaded");
			}
			if (tagres == null) {
				Tag systag = globaltagmap.getSystemTag();
				systag.setResId(systag.getName());
				put("systemTag", new Link(getFullyQualifiedContextPath(info), systag));
			} else {
				put("systemTag", tagres);
			}
			GlobalTag gtag = globaltagmap.getGlobalTag();
			gtag.setResId(gtag.getName());
			put("globalTag", new Link(getFullyQualifiedContextPath(info), gtag));
		}
		
		this.serializeTimestamps(this.tsformat);
	}
}
