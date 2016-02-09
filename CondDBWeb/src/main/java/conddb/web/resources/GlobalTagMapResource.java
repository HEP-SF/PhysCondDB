
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Entity;
import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Tag;

@SuppressWarnings("unchecked")
public class GlobalTagMapResource extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5000165341237066181L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap) {
		super(info, globaltagmap);
		build(info, globaltagmap);
	}

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap, Entity parent) {
		super(info, globaltagmap,parent);
		build(info, globaltagmap);
	}

	protected void build(UriInfo info, GlobalTagMap globaltagmap) {
		put("globalTagName", globaltagmap.getGlobalTagName());
		put("tagName", globaltagmap.getTagName());
		put("label", globaltagmap.getLabel());
		put("record", globaltagmap.getRecord());
		GlobalTagResource gtagres = null;
		TagResource tagres = null;
		if (parent == null) {
			// Add systemTag and globalTag resources because we are asking directly for maps.
			gtagres = getGlobalTagResource(info, globaltagmap);
			tagres = getTagResource(info, globaltagmap);
			if (gtagres == null) {
				GlobalTag gtag = globaltagmap.getGlobalTag();
				gtag.setResId(gtag.getName());
				put("globalTag", new Link(getFullyQualifiedContextPath(info), gtag));
			} else {
				put("globalTag", gtagres);
			}
			if (tagres == null) {
				Tag systag = globaltagmap.getSystemTag();
				systag.setResId(systag.getName());
				put("systemTag", new Link(getFullyQualifiedContextPath(info), systag));
			} else {
				put("systemTag", tagres);
			}
			
		} else if (parent != null && parent instanceof Tag) {
			gtagres = getGlobalTagResource(info, globaltagmap);
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
			tagres = getTagResource(info, globaltagmap);
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
		
		this.serializeTimestamps();
	}
	
	private GlobalTagResource getGlobalTagResource(UriInfo info, GlobalTagMap globaltagmap) {
		GlobalTagResource gtagres = null;
		try {
			GlobalTag gtag = globaltagmap.getGlobalTag();
			gtag.setResId(gtag.getName());
			gtagres = new GlobalTagResource(info, (GlobalTag) gtag);
			// remove the map object since
			// we are already filling it
			// from maps.
			gtagres.remove("globalTagMaps"); 

		} catch (org.hibernate.LazyInitializationException e) {
			log.debug("LazyInitialization Exception from hibernate: map does not have a global tag loaded");
		}	
		return gtagres;
	}
	
	private TagResource getTagResource(UriInfo info, GlobalTagMap globaltagmap) {
		TagResource tagres = null;
		try {
			log.debug("Determine tag: " + globaltagmap.getSystemTag());
			if (globaltagmap.getSystemTag() != null) {
				Tag systag = globaltagmap.getSystemTag();
				systag.setResId(systag.getName());
				if (systag.getGlobalTagMaps() != null) {
					systag.setGlobalTagMaps(null);
				}
				tagres = new TagResource(info, (Tag) systag);
				// remove the map object since
				// we are already filling it
				// from maps.
				tagres.remove("globalTagMaps"); 
			}
		} catch (org.hibernate.LazyInitializationException e) {
			log.debug("LazyInitialization Exception from hibernate: map does not have a system tag loaded");
		}
		return tagres;
	}

}
