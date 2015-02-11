/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.SystemNodeBaseRepository;
import conddb.data.SystemDescription;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface SystemNodeRepository extends CrudRepository<SystemDescription, Long>,SystemNodeBaseRepository {

//	public List<SystemDescription> findByTagNameRootLike(@Param("tagNameRoot") String tagNameRoot);
//
//	public SystemDescription findByNodeFullpath(@Param("node") String node);
}
