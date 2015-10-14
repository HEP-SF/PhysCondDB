/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.data.Iov;
import conddb.svc.dao.baserepository.IovBaseRepository;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface IovRepository extends CrudRepository<Iov, Long>, IovBaseRepository {

}
