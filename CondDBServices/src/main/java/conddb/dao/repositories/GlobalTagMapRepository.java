/**
 * 
 */
package conddb.dao.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.GlobalTagMapBaseRepository;
import conddb.data.GlobalTagMap;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface GlobalTagMapRepository extends CrudRepository<GlobalTagMap, Long>, GlobalTagMapBaseRepository {

    @Override
    @RestResource(exported = false)
    void delete(Long id);

    @Override
    @RestResource(exported = false)
    void delete(GlobalTagMap entity);

    @SuppressWarnings("unchecked")
	@Override
    @RestResource(exported = false)
    GlobalTagMap save(GlobalTagMap entity);

}
