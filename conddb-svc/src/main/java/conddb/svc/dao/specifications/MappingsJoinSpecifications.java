/**
 * 
 */
package conddb.svc.dao.specifications;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.JoinType;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;


/**
 * @author aformic
 *
 */
public class MappingsJoinSpecifications<GlobalTagMap> implements Specification<GlobalTagMap> {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());


	public MappingsJoinSpecifications() {
		super();
	}

	@Override
	public Predicate toPredicate(Root<GlobalTagMap> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        log.debug("Fake specification for fetching only...");
		root.fetch("globalTag", JoinType.LEFT);
		root.fetch("systemTag", JoinType.LEFT);
		query.distinct(true);
		//////query.select(GlobalTagMap.class).distinct(true);
		return null;
//        //building the desired query
//        root.fetch("globalTag", JoinType.LEFT);
//        root.fetch("systemTag", JoinType.LEFT);
//        final Join<GlobalTagMap, GlobalTag> globaltag = root.join("globalTag");
//        final Join<GlobalTagMap, Tag> systemtag = root.join("systemTag");
//        for (PluralAttribute<? super GlobalTagMap, ?, ?> fetch : root.getModel().getPluralAttributes()) {
//        	log.debug("get plural attribute "+fetch);
//            root.fetch(fetch, JoinType.LEFT);
//        }
//        query.distinct(true);        
        
	}

}
