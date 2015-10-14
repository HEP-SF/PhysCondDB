/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.IovBaseRepository;
import conddb.data.Iov;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface IovRepository extends CrudRepository<Iov, Long>, IovBaseRepository {

}
