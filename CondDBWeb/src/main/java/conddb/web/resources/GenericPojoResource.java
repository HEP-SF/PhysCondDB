/**
 * 
 */
package conddb.web.resources;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.Entity;

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

	public GenericPojoResource(UriInfo info, Entity entity) {
		super(info, entity);
		build(info, entity);
	}

	protected void build(UriInfo info, Entity entity) {
		try {
			Map<String, Method> entitymap = getKeysFromEntity(entity);
			for (String akey : entitymap.keySet()) {
				log.debug("Filling map with " + akey + " using method " + entitymap.get(akey).getName());
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
			Class<?> rettype = methods[i].getReturnType();
			if (methods[i].getAnnotation(Column.class) != null && akey.startsWith("get")) {
				String keyname = akey.substring(2);
				String attname = keyname.substring(0, 1).toLowerCase() + keyname.substring(1);
				if (rettype.isAssignableFrom(Entity.class)) {
					log.debug("This is an Entity object in our application...careful to automatic mapping!");
				}
				methmap.put(attname, methods[i]);
				if (methods[i].getAnnotation(Id.class) != null) {
					Object id;
					try {
						id = methmap.get(attname).invoke(entity);
						if (id instanceof Number) {
							entity.setResId(id.toString());
						} else if (id instanceof BigDecimal) {
							entity.setResId(id.toString());
						} else {
							entity.setResId(id.toString());
						}
					} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						log.error("Error in creating generic pojo resource on ID tostring()");
					}

				}
			}
		}
		return methmap;
	}

}
