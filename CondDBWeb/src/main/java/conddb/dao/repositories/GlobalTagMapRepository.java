/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.GlobalTagMap;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface GlobalTagMapRepository extends CrudRepository<GlobalTagMap, Long> {

}
