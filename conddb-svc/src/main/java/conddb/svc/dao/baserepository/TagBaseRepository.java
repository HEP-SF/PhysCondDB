/**
 * 
 */
package conddb.svc.dao.baserepository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.Tag;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface TagBaseRepository extends CondDBPageAndSortingRepository<Tag, Long>, JpaSpecificationExecutor<Tag> {

	public List<Tag> findByNameLike(@Param("name") String name);

	public Tag findByName(@Param("name") String name);
	
	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.iovs iovs WHERE p.name = (:name)")
    public Tag findByNameAndFetchIovsEagerly(@Param("name") String name);
			
	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.globalTag WHERE p.name = (:name)")
	public Tag findByNameAndFetchGlobalTagsEagerly(@Param("name") String name);

	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.globalTagMaps maps WHERE p.name = (:name) "+
	" AND maps.globalTag.lockstatus=(:lock)")
	public Tag findByNameAndFetchGlobalTagsWithLock(@Param("name") String name, @Param("lock") String status);
}
