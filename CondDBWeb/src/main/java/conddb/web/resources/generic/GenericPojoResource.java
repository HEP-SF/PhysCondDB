/**
 * 
 */
package conddb.web.resources.generic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.ws.rs.core.UriInfo;

import org.hibernate.LazyInitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;


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

	private int level = 0;
	private conddb.data.Entity parent = null;
	
//	Map<String, Method> entitymap = new LinkedHashMap<String, Method>();
	Map<String, Method> nswentitymap = null;
	Map<String, Method> nswsetmap = null;
	Map<String, Method> entitymap = null;

	public GenericPojoResource(UriInfo info, conddb.data.Entity entity) {
		super(info, entity);
		initmaps(entity);
		try {
			build(info, entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public GenericPojoResource(UriInfo info, conddb.data.Entity entity, int level, conddb.data.Entity parent) {
		super(info, entity);
		if (parent != null)
			log.debug("Creating new resource using level "+level+" and parent class "+parent.getClass().getName());
		else 
			log.debug("Creating new resource using level "+level+" and parent class null");

		this.level = level;
		this.parent = parent;
		initmaps(entity);
		try {
		build(info, entity, level);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void build(UriInfo info, conddb.data.Entity entity, int level) throws Exception {
		build(info,entity);
		log.debug("======= Now loop on ManyToOne if level greater than 0 : "+level+ " ======= ");
		if (level>0) {
			log.debug("Loop over ManyToOne annotated fields..."+nswsetmap.size());
			for (String akey : nswsetmap.keySet()) {
				log.debug("Loop over map with "+akey);
				Method mth = nswsetmap.get(akey);
				Class<?> subentitysetclass = mth.getReturnType();
				Set<conddb.data.Entity> subentityset;
				try {
					subentityset = (Set<conddb.data.Entity>) nswsetmap.get(akey).invoke(entity);
					List<GenericPojoResource> relist = new ArrayList<GenericPojoResource>();
					for (conddb.data.Entity nswEntity : subentityset) {
						log.debug("Mapping subset entry: "+nswEntity);
						GenericPojoResource<conddb.data.Entity> gpr = new GenericPojoResource<conddb.data.Entity>(info, nswEntity, level-1, entity);
						relist.add(gpr);
					}
					log.debug("3) Filling map with "+akey+" using method "+nswsetmap.get(akey).getName());
					put(akey, relist);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LazyInitializationException e1) {
					log.error("Trying to access an object which has not been initialized from hibernate...ignore it");
				}
			}			
		}
	}

	protected void initmaps(conddb.data.Entity entity) {
		PojoMapFactory pm = PojoMapFactory.getInstance();
		try {
			nswentitymap = pm.getEntityMap(entity, "onetomany");
			nswsetmap = pm.getEntityMap(entity, "manytoone");
			entitymap = pm.getEntityMap(entity, "column");
		} catch (ConddbWebException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void build(UriInfo info, conddb.data.Entity entity) throws Exception {
		try {
			//fetchKeysFromEntity(entity);
			log.debug("Keys for "+entity.getClass().getName()+" have been fetched....");
			log.debug("Loop over simple column fields..."+entitymap.size());
			for (String akey : entitymap.keySet()) {
				log.debug("1) Filling map with "+akey+" using method "+entitymap.get(akey).getName());
				put(akey, entitymap.get(akey).invoke(entity));
			}
			log.debug("Loop over OneToMany annotated fields..."+nswentitymap.size());
			for (String akey : nswentitymap.keySet()) {
				log.debug("Loop over nswentitymap with "+akey);
				Method mth = nswentitymap.get(akey);
				Class<?> subentityclass = mth.getReturnType();
				if (this.parent != null && subentityclass.equals(this.parent.getClass())) {
					log.debug("This class is the same as the parent...then you should ignore the parsing: "+subentityclass.getName());
				} else if (this.level <= 0) {
					log.debug("The level of depth for the resource conversion is now <=0 ! Ignoring "+subentityclass.getName());
				} else {
					conddb.data.Entity subentity = (conddb.data.Entity) nswentitymap.get(akey).invoke(entity);
					GenericPojoResource<conddb.data.Entity> gpr = new GenericPojoResource<conddb.data.Entity>(info, subentity, level-1, entity);
					log.debug("2) Filling map with "+akey+" using method "+nswentitymap.get(akey).getName());
					put(akey, gpr);
				}
			}
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void fetchKeysFromEntity(conddb.data.Entity entity) {
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
