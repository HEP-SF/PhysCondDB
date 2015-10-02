/**
 * 
 */
package conddb.dao.baserepository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.Tag;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface TagBaseRepository extends CondDBPageAndSortingRepository<Tag, Long> {

	public List<Tag> findByNameLike(@Param("name") String name);

	public Tag findByName(@Param("name") String name);
	
	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.iovs iovs WHERE p.name = (:name)")
    public Tag findByNameAndFetchIovsEagerly(@Param("name") String name);
		
	/**
	 * In general we should use the methods inside the IovBaseRepository
	 * @param name
	 * @param from
	 * @param to
	 * @return
	 */
//	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.iovs iovs WHERE p.name = (:name) "
//			+ " and (iovs.since > :from and iovs.since <= :to ")
//    public Tag findByNameAndFetchIovsEagerlyBetweenRange(@Param("name") String name, @Param("from") BigDecimal from, @Param("to") BigDecimal to);
	
	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.globalTagMaps maps WHERE p.name = (:name)")
	public Tag findByNameAndFetchGlobalTagsEagerly(@Param("name") String name);

	@Query("SELECT distinct p FROM Tag p JOIN FETCH p.globalTagMaps maps WHERE p.name = (:name) "+
	" AND maps.globalTag.lockstatus=(:lock)")
	public Tag findByNameAndFetchGlobalTagsWithLock(@Param("name") String name, @Param("name") String status);
}
