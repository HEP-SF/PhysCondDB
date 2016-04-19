/**
 * 
 */
package conddb.svc.dao.specifications;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.domain.Specifications;



/**
 * @author aformic
 *
 */
public class MappingsSpecBuilder<GlobalTagMap> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	
	private final List<SearchCriteria> params;
	 
    public MappingsSpecBuilder() {
        params = new ArrayList<SearchCriteria>();
    }
 
    public MappingsSpecBuilder<GlobalTagMap> with(String key, String operation, Object value) {
    	log.debug("Adding criteria for mappings specification builder "+key+" operation "+operation+" val "+value);
    	if (key.contains("_")) {
    		key = key.replace("_", ".");
    	}
    	params.add(new SearchCriteria(key, operation, value));
        return this;
    }
 
    public Specification<GlobalTagMap> build() {
    	log.debug("build specification for mapping using params "+params.size());

        if (params.size() == 0) {
            return null;
        }
        log.debug("builder is creating a specification object for map with n params: "+params.size());
        List<MappingsSpecifications<GlobalTagMap>> specs = new ArrayList<>();
        for (SearchCriteria param : params) {
            specs.add(new MappingsSpecifications<GlobalTagMap>(param));
        }

        Specification<GlobalTagMap> result = specs.get(0);
        for (int i = 1; i < specs.size(); i++) {
            log.debug("builder is creating a specification "+result.toString());
            result = Specifications.where(result).and(specs.get(i));
        }
        
        return result;
    }    
    
}
