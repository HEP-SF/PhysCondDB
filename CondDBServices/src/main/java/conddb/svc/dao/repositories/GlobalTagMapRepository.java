/**
 * 
 */
package conddb.svc.dao.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.data.GlobalTagMap;
import conddb.svc.dao.baserepository.GlobalTagMapBaseRepository;

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
