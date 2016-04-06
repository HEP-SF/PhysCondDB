/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.data.SystemDescription;
import conddb.svc.dao.baserepository.SystemNodeBaseRepository;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface SystemNodeRepository extends CrudRepository<SystemDescription, Long>, SystemNodeBaseRepository {

    @Override
    @RestResource(exported = false)
    void delete(Long id);

    @Override
    @RestResource(exported = false)
    void delete(SystemDescription entity);

    @SuppressWarnings("unchecked")
	@Override
    @RestResource(exported = false)
    SystemDescription save(SystemDescription entity);

}
