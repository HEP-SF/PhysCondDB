
package conddb.web.resources;

import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.SystemDescription;

@SuppressWarnings("unchecked")
public class SystemDescriptionResource extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8897120510350292399L;
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public SystemDescriptionResource(UriInfo info, SystemDescription system) {
		super(info, system);
		build(info, system);
	}

	protected void build(UriInfo info, SystemDescription system) {
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
