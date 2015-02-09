/**
 * 
 */
package conddb.dao.baserepository;

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
	
	@Query("SELECT p FROM Tag p JOIN FETCH p.iovs iovs WHERE p.name = (:name)")
    public Tag findByNameAndFetchIovsEagerly(@Param("name") String name);
}
