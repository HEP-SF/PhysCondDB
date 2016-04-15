/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import conddb.data.Payload;
import conddb.svc.dao.baserepository.PayloadBaseRepository;

/**
 * @author formica
 *
 */
@Repository
public interface PayloadRepository extends CrudRepository<Payload, String>, PayloadBaseRepository {

    @Override
    void delete(String id);

    @Override
    void delete(Payload entity);

    @SuppressWarnings("unchecked")
	@Override
    Payload save(Payload entity);
}
