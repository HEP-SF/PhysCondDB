/**
 * 
 */
package conddb.svc.dao.specifications;

import java.sql.Timestamp;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.springframework.data.jpa.domain.Specification;


/**
 * @author aformic
 *
 */
public class GenericSpecifications<T> implements Specification<T> {
	
	private SearchCriteria criteria;
	
	

	public GenericSpecifications(SearchCriteria criteria) {
		super();
		this.criteria = criteria;
	}

	@Override
	public Predicate toPredicate(Root<T> root, CriteriaQuery<?> query, CriteriaBuilder builder) {
        if (criteria.getOperation().equalsIgnoreCase(">")) {
            if (root.get(criteria.getKey()).getJavaType() == Timestamp.class) {
            	Long inpval = new Long(criteria.getValue().toString());
            	Timestamp ts = new Timestamp(inpval);
                return builder.greaterThanOrEqualTo(root.<Timestamp>get(criteria.getKey()), ts);
            } else {
            	return builder.greaterThanOrEqualTo(root.<String> get(criteria.getKey()), criteria.getValue().toString());
            }
        } 
        else if (criteria.getOperation().equalsIgnoreCase("<")) {
        	if (root.get(criteria.getKey()).getJavaType() == Timestamp.class) {
            	Long inpval = new Long(criteria.getValue().toString());
            	Timestamp ts = new Timestamp(inpval);
                return builder.lessThanOrEqualTo(root.<Timestamp>get(criteria.getKey()), ts);
            } else {
            	return builder.lessThanOrEqualTo(root.<String> get(criteria.getKey()), criteria.getValue().toString());
            }
              
        } 
        else if (criteria.getOperation().equalsIgnoreCase(":")) {
        	if (root.get(criteria.getKey()).getJavaType() == Timestamp.class) {
            	Long inpval = new Long(criteria.getValue().toString());
            	Timestamp ts = new Timestamp(inpval);
                return builder.equal(root.<Timestamp>get(criteria.getKey()), ts);
            } else if (root.get(criteria.getKey()).getJavaType() == String.class) {
                return builder.like(
                  root.<String>get(criteria.getKey()), "%" + criteria.getValue() + "%");
            } else {
                return builder.equal(root.get(criteria.getKey()), criteria.getValue());
            }
        }
        return null;
	}

}
