/**
 *
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import conddb.data.GlobalTag;
import conddb.svc.dao.baserepository.GlobalTagBaseRepository;

/**
 * @author formica
 *
 */
@Repository
public interface GlobalTagRepository extends CrudRepository<GlobalTag, Long>, GlobalTagBaseRepository {

    @Override
    void delete(Long id);

    @Override
    void delete(GlobalTag entity);

    @SuppressWarnings("unchecked")
	@Override
    GlobalTag save(GlobalTag entity);

}
