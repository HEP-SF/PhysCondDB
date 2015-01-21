/**
 * 
 */
package conddb.dao.repositories;

import java.util.List;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.SystemDescription;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface SystemNodeRepository extends CrudRepository<SystemDescription, Long> {

	public List<SystemDescription> findByTagNameRootLike(@Param("tagNameRoot") String tagNameRoot);

	public SystemDescription findByNodeFullpath(@Param("node") String node);
}
