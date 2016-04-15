/**
 * 
 */
package conddb.svc.dao.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import conddb.data.GlobalTagMap;
import conddb.svc.dao.baserepository.GlobalTagMapBaseRepository;

/**
 * @author formica
 *
 */
@Repository
public interface GlobalTagMapRepository extends CrudRepository<GlobalTagMap, Long>, GlobalTagMapBaseRepository {

    @Override
    void delete(Long id);

    @Override
    void delete(GlobalTagMap entity);

    @SuppressWarnings("unchecked")
	@Override
    GlobalTagMap save(GlobalTagMap entity);

}
