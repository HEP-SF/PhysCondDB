/**
 *
 */
package conddb.dao.baserepository;

import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
@RepositoryRestResource
@Transactional(readOnly = true)
public interface GlobalTagBaseRepository extends CondDBPageAndSortingRepository<GlobalTag, String> {

	List<GlobalTag> findByNameLike(@Param("name") String name);

	List<GlobalTag> findByDescriptionLike(
			@Param("description") String description);

	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps WHERE maps.globalTag.name = (:name)")
	public GlobalTag findByNameAndFetchTagsEagerly(@Param("name") String name);

	@Query("SELECT distinct p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps WHERE maps.globalTag.name like (:name)")
	public List<GlobalTag> findByNameLikeAndFetchTagsEagerly(
			@Param("name") String name);

	List<GlobalTag> findByInsertionTimeBetween(@Param("since") Timestamp since,
			@Param("until") Timestamp until);
}
