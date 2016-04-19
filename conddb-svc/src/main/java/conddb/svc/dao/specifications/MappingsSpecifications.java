/**
 * 
 */
package conddb.svc.dao.specifications;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;

import conddb.data.GlobalTag;
import conddb.data.Tag;


/**
 * @author aformic
 *
 */
public class MappingsSpecifications<GlobalTagMap> implements Specification<GlobalTagMap> {
	
	private SearchCriteria criteria;
	
	private Logger log = LoggerFactory.getLogger(this.getClass());


	public MappingsSpecifications(SearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}

	@Override
	public Predicate toPredicate(Root<GlobalTagMap> root, CriteriaQuery<?> query, CriteriaBuilder builder) {

        log.debug("Dump query object "+query.toString()+" with criteria "+criteria.getKey()+" val "+criteria.getValue());
        if (criteria.getKey().contains("globalTag")) {
        	Path<GlobalTag> path = root.<GlobalTag>get("globalTag");
        	String field = criteria.getKey().split("\\.")[1];
            log.debug("Use global tag for expression like using field "+field);
        	return builder.like(path.<String>get(field), "%" + criteria.getValue() + "%");
        } else if (criteria.getKey().contains("systemTag")) {
        	Path<Tag> path = root.<Tag>get("systemTag");
        	String field = criteria.getKey().split("\\.")[1];
            log.debug("Use tag for expression like using field "+field);
        	return builder.like(path.<String>get(field), "%" + criteria.getValue() + "%");
        }
        return null;
        
	}

}
