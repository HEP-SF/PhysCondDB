/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.PayloadBaseRepository;
import conddb.data.Payload;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface PayloadRepository extends CrudRepository<Payload, String>, PayloadBaseRepository {

    @Override
    @RestResource(exported = false)
    void delete(String id);

    @Override
    @RestResource(exported = false)
    void delete(Payload entity);

    @SuppressWarnings("unchecked")
	@Override
    @RestResource(exported = false)
    Payload save(Payload entity);
	
}
