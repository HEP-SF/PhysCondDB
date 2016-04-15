/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import conddb.data.SystemDescription;
import conddb.svc.dao.baserepository.SystemNodeBaseRepository;

/**
 * @author formica
 *
 */
@Repository
public interface SystemNodeRepository extends CrudRepository<SystemDescription, Long>, SystemNodeBaseRepository {

    @Override
    void delete(Long id);

    @Override
    void delete(SystemDescription entity);

    @SuppressWarnings("unchecked")
	@Override
    SystemDescription save(SystemDescription entity);

}
