/**
 * 
 */
package conddb.dao.baserepository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.SystemDescription;

/**
 * @author formica
 *
 */
@RepositoryRestResource
@Transactional(readOnly = true)
public interface SystemNodeBaseRepository extends CondDBPageAndSortingRepository<SystemDescription, Long> {

	public List<SystemDescription> findByTagNameRootLike(@Param("tagNameRoot") String tagNameRoot);

	public SystemDescription findByNodeFullpath(@Param("node") String node);
}
