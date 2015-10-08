
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.GlobalTagMap;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class GlobalTagMapResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	TimestampFormat tsformat = null;

	public GlobalTagMapResource(UriInfo info, GlobalTagMap globaltagmap) {
		super(info, globaltagmap);
		put("globalTagName", globaltagmap.getGlobalTagName());
		put("tagName", globaltagmap.getTagName());
		put("label", globaltagmap.getLabel());
		put("record", globaltagmap.getRecord());
		
		put("globalTag",new Link(getFullyQualifiedContextPath(info), globaltagmap.getGlobalTag()));
		put("systemTag",new Link(getFullyQualifiedContextPath(info), globaltagmap.getSystemTag()));
	}
}
