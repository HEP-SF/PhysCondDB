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
//@RepositoryRestResource(collectionResourceRel = "payloads", path = "payloads")
@RestResource(exported = false)
public interface PayloadRepository extends CrudRepository<Payload, String>, PayloadBaseRepository {

	
}
