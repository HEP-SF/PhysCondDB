/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import conddb.data.Iov;
import conddb.svc.dao.baserepository.IovBaseRepository;

/**
 * @author formica
 *
 */
@Repository
public interface IovRepository extends CrudRepository<Iov, Long>, IovBaseRepository {

}
