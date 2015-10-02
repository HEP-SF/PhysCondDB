/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.SystemNodeBaseRepository;
import conddb.data.SystemDescription;

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
