/**
 *
 */
package conddb.svc.dao.baserepository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface GlobalTagBaseRepository extends CondDBPageAndSortingRepository<GlobalTag, Long>, JpaSpecificationExecutor<GlobalTag> {

	GlobalTag findByName(@Param("name") String name);

	List<GlobalTag> findByNameLike(@Param("name") String name);

	List<GlobalTag> findByInsertionTimeBetween(@Param("since") Timestamp since,
			@Param("until") Timestamp until);

	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.systemTag WHERE maps.globalTag.name = (:name)")
	public GlobalTag findByNameAndFetchTagsEagerly(@Param("name") String name);

	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps JOIN FETCH maps.systemTag WHERE maps.globalTag.name like (:name)")
	public List<GlobalTag> findByNameLikeAndFetchTagsEagerly(
			@Param("name") String name);

}
