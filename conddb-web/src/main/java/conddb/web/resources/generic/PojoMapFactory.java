/**
 * 
 */
package conddb.web.resources.generic;

import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.web.exceptions.ConddbWebException;

/**
 * @author aformic
 *
 */
public class PojoMapFactory {

	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private static PojoMapFactory pm = null;

	private Map<String,String> typemap = Collections.unmodifiableMap(Stream.of(
            new AbstractMap.SimpleEntry<>("column", "Gather @Column annotated fields"),
            new AbstractMap.SimpleEntry<>("onetomany", "Gather @OneToMany annotated fields"),
            new AbstractMap.SimpleEntry<>("manytoone", "Gather @ManyToOne annotated fields"))
            .collect(Collectors.toMap((e) -> e.getKey(), (e) -> e.getValue())));
	
	private Map<String,Map<String, Method>> nswentitymap = new LinkedHashMap<String, Map<String, Method>>();
	private Map<String,Map<String, Method>> nswsetmap = new LinkedHashMap<String, Map<String, Method>>();
	private Map<String,Map<String, Method>> entitymap = new LinkedHashMap<String, Map<String, Method>>();
	

	private PojoMapFactory() {
		
	}
	
	public static PojoMapFactory getInstance() {
		if (pm == null) {
			pm = new PojoMapFactory();
		}
		return pm;
	}
	
	public Map<String, Method> getEntityMap(conddb.data.AfEntity entity,String type) throws ConddbWebException {
		String clname = entity.getClass().getName();
		log.debug("Try to find entity map for "+clname+" using type "+type);
		if (type.equals("column") && entitymap.containsKey(entity.getClass().getName())) {
			return entitymap.get(entity.getClass().getName());
		} else if (type.equals("onetomany") && nswentitymap.containsKey(entity.getClass().getName())) {
			return nswentitymap.get(entity.getClass().getName());
		} else if (type.equals("manytoone") && nswsetmap.containsKey(entity.getClass().getName())) {
			return nswsetmap.get(entity.getClass().getName());
		} else {
			if (!typemap.containsKey(type)) {
				throw new ConddbWebException("Cannot get map for type "+type);
			}
			log.debug("Type not found or map does not contain any key for "+clname);
			Map<String, Method> localentitymap = new LinkedHashMap<String, Method>();
			Map<String, Method> localnswentitymap = new LinkedHashMap<String, Method>();
			Map<String, Method> localnswsetmap = new LinkedHashMap<String, Method>();
			fetchKeysFromEntity(entity, localnswentitymap, localnswsetmap, localentitymap);
			log.debug("Keys fetched for "+clname);
			entitymap.put(clname,localentitymap);
			nswentitymap.put(clname,localnswentitymap);
			nswsetmap.put(clname,localnswsetmap);
			return getEntityMap(entity, type);
		}
	}
	
	private void fetchKeysFromEntity(conddb.data.AfEntity entity, 
			Map<String, Method> nswentitymap, 
			Map<String, Method> nswsetmap,
			Map<String, Method> entitymap) {
		// Field fields[] = entity.getClass().getDeclaredFields();
		Method methods[] = entity.getClass().getDeclaredMethods();
		for (int i = 0; i < methods.length; i++) {
			String akey = methods[i].getName();
			
			if (methods[i].getAnnotation(ManyToOne.class) != null && akey.startsWith("get")) {
				String keyname = akey.substring(3);
				String attname = keyname.substring(0, 1).toLowerCase() + keyname.substring(1);
				nswentitymap.put(attname, methods[i]);
			} else if (methods[i].getAnnotation(OneToMany.class) != null && akey.startsWith("get")) {
				String keyname = akey.substring(3);
				String attname = keyname.substring(0, 1).toLowerCase() + keyname.substring(1);
				nswsetmap.put(attname, methods[i]);
			} else if (methods[i].getAnnotation(Column.class) != null && akey.startsWith("get")) {
				String keyname = akey.substring(3);
				String attname = keyname.substring(0, 1).toLowerCase() + keyname.substring(1);
				entitymap.put(attname, methods[i]);
			}
		}
	}

	
}
