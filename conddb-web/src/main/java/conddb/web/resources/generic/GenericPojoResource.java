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

import conddb.data.AfEntity;
import conddb.data.annotations.Linkit;
import conddb.web.exceptions.ConddbWebException;
import conddb.web.resources.Link;


/**
 * @author aformic
 *
 */
public class GenericPojoResource<T extends AfEntity> extends Link {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6564278102094146306L;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private int level = 0;
	private AfEntity parent = null;

	// Map<String, Method> entitymap = new LinkedHashMap<String, Method>();
	Map<String, Method> nswentitymap = null;
	Map<String, Method> nswsetmap = null;
	Map<String, Method> entitymap = null;

	public GenericPojoResource(UriInfo info, AfEntity entity) {
		super(info, entity);
		initmaps(entity);
		try {
			build(info, entity);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public GenericPojoResource(UriInfo info, AfEntity entity, int level, AfEntity parent) {
		super(info, entity);
		if (parent != null)
			log.debug(
					"Creating new resource using level " + level + " and parent class " + parent.getClass().getName());
		else
			log.debug("Creating new resource using level " + level + " and parent class null");

		this.level = level;
		this.parent = parent;
		initmaps(entity);
		try {
			build(info, entity, level);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	protected void build(UriInfo info, AfEntity entity, int level) throws Exception {
		build(info, entity);
		log.debug("======= Now loop on ManyToOne if level greater than 0 : " + level + " ======= ");
		if (level > 0) {
			log.debug("Loop over ManyToOne annotated fields..." + nswsetmap.size());
			for (String akey : nswsetmap.keySet()) {
				log.debug("Loop over map with " + akey);
				Method mth = nswsetmap.get(akey);
				Class<?> subentitysetclass = mth.getReturnType();
				Set<AfEntity> subentityset;
				try {
					subentityset = (Set<AfEntity>) nswsetmap.get(akey).invoke(entity);
					List<GenericPojoResource> relist = new ArrayList<GenericPojoResource>();
					for (AfEntity nswEntity : subentityset) {
						log.debug("Mapping subset entry: " + nswEntity);
						GenericPojoResource<AfEntity> gpr = new GenericPojoResource<AfEntity>(info, nswEntity,
								level - 1, entity);
						relist.add(gpr);
					}
					log.debug("3) Filling map with " + akey + " using method " + nswsetmap.get(akey).getName());
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

	protected void initmaps(AfEntity entity) {
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

	protected void build(UriInfo info, AfEntity entity) throws Exception {
		try {
			// fetchKeysFromEntity(entity);
			log.debug("Keys for " + entity.getClass().getName() + " have been fetched....");
			log.debug("Loop over simple column fields..." + entitymap.size());
			for (String akey : entitymap.keySet()) {
				Method mth = entitymap.get(akey);
				if (mth.isAnnotationPresent(Linkit.class)) {
					Class<?> subentityclass = mth.getReturnType(); 
					if (AfEntity.class.isAssignableFrom(subentityclass)) {
						AfEntity linkedentity = (AfEntity) subentityclass.newInstance();
						Linkit linkit = mth.getAnnotation(Linkit.class);
						Method getter = entity.getClass().getMethod(linkit.getter(), null);
						String parenthref = (String) getter.invoke(entity, null);
						linkedentity.setHref(parenthref);
						GenericPojoResource<AfEntity> gpr = new GenericPojoResource<AfEntity>(info, linkedentity, 0,
								entity);
						log.debug("1) Link resource not loaded " + akey + " using method " + mth.getName());
						put(akey, gpr);
						continue;
					}
				}
				log.debug("1) Filling map with " + akey + " using method " + mth.getName());
				put(akey, mth.invoke(entity));
			}
			log.debug("Loop over OneToMany annotated fields..." + nswentitymap.size());
			for (String akey : nswentitymap.keySet()) {
				log.debug("Loop over nswentitymap with " + akey);
				Method mth = nswentitymap.get(akey);
				Class<?> subentityclass = mth.getReturnType();
				if (this.parent != null && subentityclass.equals(this.parent.getClass())) {
					log.debug(
							"This class is the same as the parent...you can ignore the parsing if entity are the same for: "
									+ subentityclass.getName());
					AfEntity subentity = (AfEntity) nswentitymap.get(akey).invoke(entity);
					if (subentity.equals(parent)) {
						log.debug("Ignoring: " + subentity.toString());
						continue;
					}
				} else if (this.level <= 0) {
					log.debug("The level of depth for the resource conversion is now <=0 ! Ignoring "
							+ subentityclass.getName());
					continue;
				}
				AfEntity subentity = (AfEntity) nswentitymap.get(akey).invoke(entity);
				GenericPojoResource<AfEntity> gpr = new GenericPojoResource<AfEntity>(info, subentity, level - 1,
						entity);
				log.debug("2) Filling map with " + akey + " using method " + nswentitymap.get(akey).getName());
				put(akey, gpr);
			}

		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
