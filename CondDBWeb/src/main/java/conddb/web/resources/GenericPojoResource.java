/**
 * 
 */
package conddb.web.resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Entity;
import conddb.utils.json.serializers.TimestampFormat;

/**
 * @author aformic
 *
 */
public class GenericPojoResource<T extends conddb.data.Entity> extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6564278102094146306L;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	public GenericPojoResource(UriInfo info, Entity entity, TimestampFormat tsformat) {
		super(info, entity);
		build(info, entity, tsformat);
	}

	public GenericPojoResource(UriInfo info, Entity entity) {
		super(info, entity);
		build(info, entity, null);
	}

	protected void build(UriInfo info, Entity entity, TimestampFormat tsformat) {
		this.tsformat = tsformat;
		try {
			Map<String, Method> entitymap = getKeysFromEntity(entity);
			for (String akey : entitymap.keySet()) {
				log.debug("Filling map with "+akey+" using method "+entitymap.get(akey).getName());
				put(akey, entitymap.get(akey).invoke(entity));
			}
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Map<String, Method> getKeysFromEntity(Entity entity) {
		// Field fields[] = entity.getClass().getDeclaredFields();
		Method methods[] = entity.getClass().getDeclaredMethods();
		Map<String, Method> methmap = new LinkedHashMap<String, Method>();
		for (int i = 0; i < methods.length; i++) {
			String akey = methods[i].getName();
			if (methods[i].getAnnotation(Column.class) != null && akey.startsWith("get")) {
				String keyname = akey.substring(2);
				String attname = keyname.substring(0, 1).toLowerCase() + keyname.substring(1);
				methmap.put(attname, methods[i]);
			}
		}
		return methmap;
	}

}
