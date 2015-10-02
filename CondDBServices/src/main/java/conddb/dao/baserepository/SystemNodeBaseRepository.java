/**
 * 
 */
package conddb.dao.baserepository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

	/**
	 * @param tagNameRoot
	 * @return
	 */
	public Page<SystemDescription> findByTagNameRootLike(@Param("tagNameRoot") String tagNameRoot, Pageable pageable);

	/**
	 * @param tagNameRoot
	 * @return
	 */
	public SystemDescription findByTagNameRoot(@Param("tagNameRoot") String tagNameRoot);

	/**
	 * @param schema
	 * @return
	 */
	public List<SystemDescription> findBySchemaName(@Param("schemaName") String schemaName);

	/**
	 * @param schema
	 * @return
	 */
	public Page<SystemDescription> findBySchemaNameLike(@Param("schemaName") String schemaName, Pageable pageable);

	/**
	 * @param node
	 * @return
	 */
	public SystemDescription findByNodeFullpath(@Param("nodeFullpath") String node);
	
	/**
	 * @param node
	 * @return
	 */
	public Page<SystemDescription> findByNodeFullpathLike(@Param("nodeFullpath") String node, Pageable pageable);
	
	/**
	 * @param tagNameRoot
	 * @return
	 */
	public List<SystemDescription> findByTagNameRootLike(@Param("tagNameRoot") String tagNameRoot);

	/**
	 * @param nodeFullpath
	 * @return
	 */
	public List<SystemDescription> findByNodeFullpathLike(@Param("nodeFullpath") String nodeFullpath);

}
