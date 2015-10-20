
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.SystemDescription;
import conddb.utils.json.serializers.TimestampFormat;

@SuppressWarnings("unchecked")
public class SystemDescriptionResource extends Link {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public SystemDescriptionResource(UriInfo info, SystemDescription system, TimestampFormat tsformat) {
		super(info, system);
		build(info, system, tsformat);
	}

	public SystemDescriptionResource(UriInfo info, SystemDescription system) {
		super(info, system);
		build(info, system, null);
	}

	protected void build(UriInfo info, SystemDescription system, TimestampFormat tsformat) {
		this.tsformat = tsformat;
		put("id", system.getId());
		put("nodeFullpath", system.getNodeFullpath());
		put("nodeDescription", system.getNodeDescription());
		put("schemaName", system.getSchemaName());
		put("groupSize", system.getGroupSize());
		put("tagNameRoot", system.getTagNameRoot());
		if (system.getTags() != null) {
			put("tags", system.getTags());
		}
	}
}
