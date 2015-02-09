/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.TagBaseRepository;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
//@RepositoryRestResource
@RestResource(exported = false)
public interface TagRepository extends CrudRepository<Tag, Long>, TagBaseRepository {

//	public List<Tag> findByNameLike(@Param("name") String name);
//
//	public Tag findByName(@Param("name") String name);
//	
//	@Query("SELECT p FROM Tag p JOIN FETCH p.iovs iovs WHERE p.name = (:name)")
//    public Tag findByNameAndFetchIovsEagerly(@Param("name") String name);

}
