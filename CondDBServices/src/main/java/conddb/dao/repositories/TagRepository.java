/**
 * 
 */
package conddb.dao.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.Tag;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface TagRepository extends CrudRepository<Tag, Long> {

	public List<Tag> findByNameLike(@Param("name") String name);

	public Tag findByName(@Param("name") String name);
	
	@Query("SELECT p FROM Tag p JOIN FETCH p.iovs iovs WHERE p.name = (:name)")
    public Tag findByNameAndFetchIovsEagerly(@Param("name") String name);

}
