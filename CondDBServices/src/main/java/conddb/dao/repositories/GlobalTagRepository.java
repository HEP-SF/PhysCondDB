/**
 *
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.GlobalTagBaseRepository;
import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
@RestResource(exported = false)
public interface GlobalTagRepository extends CrudRepository<GlobalTag, String>, GlobalTagBaseRepository {

    @Override
    @RestResource(exported = false)
    void delete(String id);

    @Override
    @RestResource(exported = false)
    void delete(GlobalTag entity);

    @SuppressWarnings("unchecked")
	@Override
    @RestResource(exported = false)
    GlobalTag save(GlobalTag entity);

}
