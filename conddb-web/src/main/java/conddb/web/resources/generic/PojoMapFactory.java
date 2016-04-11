/**
 * 
 */
package conddb.web.resources.generic;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.AfEntity;
import conddb.data.annotations.Linkit;
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
			fetchKeysFromEntityFields(entity, localnswentitymap, localnswsetmap, localentitymap);
			log.debug("Keys fetched for "+clname);
			entitymap.put(clname,localentitymap);
			nswentitymap.put(clname,localnswentitymap);
			nswsetmap.put(clname,localnswsetmap);
			return getEntityMap(entity, type);
		}
	}
	
	/**
	 * This method only fills maps if annotations are on methods members of the input class
	 * @param entity
	 * @param nswentitymap
	 * @param nswsetmap
	 * @param entitymap
	 */
	private void fetchKeysFromEntityMethods(AfEntity entity, 
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

	/**
	 * This method looks both in Fields and Methods for annotations
	 * @param entity
	 * @param nswentitymap
	 * @param nswsetmap
	 * @param entitymap
	 */
	private void fetchKeysFromEntityFields(AfEntity entity, 
			Map<String, Method> nswentitymap, 
			Map<String, Method> nswsetmap,
			Map<String, Method> entitymap) {

		List<Field> fields = new ArrayList<Field>(Arrays.asList(entity.getClass().getDeclaredFields()));
		List<Method> methods = Arrays.asList(entity.getClass().getDeclaredMethods()).stream().filter(p -> p.getName().startsWith("get")).collect(Collectors.toList());
		
		for (Field afield : fields) {
			String akey = afield.getName();
			Method getmeth = getMethodFromField(afield,methods);
			if (getmeth == null) {
				log.debug("Cannot find method get for field "+afield.getName()+"....skip it");
				continue;
			}
			if (afield.getAnnotation(ManyToOne.class) != null || getmeth.getAnnotation(ManyToOne.class) != null) {
				nswentitymap.put(akey, getmeth);
			} else if (afield.getAnnotation(OneToMany.class) != null || getmeth.getAnnotation(OneToMany.class) != null) {
				nswsetmap.put(akey, getmeth);
			} else if (afield.getAnnotation(Column.class) != null || getmeth.getAnnotation(Column.class) != null) {
				entitymap.put(akey, getmeth);
			} else if (afield.getAnnotation(Linkit.class) != null || getmeth.getAnnotation(Linkit.class) != null) {
				entitymap.put(akey, getmeth);
			}
		}
	}

	private Method getMethodFromField(Field f, List<Method> methods) {
		String akey = f.getName();
		List<Method> methList = methods.stream().filter(p -> (p.getName()).matches("get."+akey.substring(1))).limit(1).collect(Collectors.toList());
		if (methList.size() != 1) {
			return null; 
		}
		return methList.get(0);
	}
	
	public static void main(String args[]) {
		Date now = new Date();
		System.out.println("TEST PojoMapFactory ");
		@Entity
		class TestPerson extends AfEntity implements Serializable {
//			@Id
//			@Column(name="NAME")
			private String name;
//			@Column(name="AGE")
			private Integer age;
			
			public TestPerson() { name = "pippo"; age = 10;}
			
			@Id
			@Column(name="NAME")
			public String getName() {
				return name;
			}

			public void setName(String name) {
				this.name = name;
			}


			@Column(name="AGE")
			public Integer getAge() {
				return age;
			}


			public void setAge(Integer age) {
				this.age = age;
			}


			@Override
			public boolean equals(Object obj) {
				// TODO Auto-generated method stub
				return false;
			}

			@Override
			public int hashCode() {
				// TODO Auto-generated method stub
				return 0;
			}
			
		}
		TestPerson a = new TestPerson();
		PojoMapFactory pm = PojoMapFactory.getInstance();
		try {
			Map<String,Method> map = pm.getEntityMap(a, "column");
			System.out.println("Map of size "+map.size());
			for (String string : map.keySet()) {
				System.out.println("Entry "+string+" val "+map.get(string).getName());
			}
		} catch (ConddbWebException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date end = new Date();
		System.out.println(" time "+(end.getTime()-now.getTime()));
	}

/*	private void fetchKeysFromEntity(conddb.data.AfEntity entity, 
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
	}*/

	
}
