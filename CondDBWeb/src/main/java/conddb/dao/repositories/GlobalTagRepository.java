/**
 * 
 */
package conddb.dao.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface GlobalTagRepository extends CrudRepository<GlobalTag, String> {

	List<GlobalTag> findByNameLike(@Param("name")String name);
	
	List<GlobalTag> findByDescriptionLike(@Param("description")String description);
	
	@Query("SELECT p FROM GlobalTag p JOIN FETCH p.globalTagMaps maps WHERE maps.globalTag.name = (:name)")
    public GlobalTag findByNameAndFetchTagsEagerly(@Param("name") String name);
//	List<GlobalTag> findByInsertionTimeBetween(Date since, Date until);
}
