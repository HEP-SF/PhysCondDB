/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.data.Tag;
import conddb.svc.dao.baserepository.TagBaseRepository;

/**
 * @author formica
 *
 */
//@RepositoryRestResource
@RestResource(exported = false)
public interface TagRepository extends CrudRepository<Tag, Long>, TagBaseRepository {

    @Override
    @RestResource(exported = false)
    void delete(Long id);

    @Override
    @RestResource(exported = false)
    void delete(Tag entity);

    @SuppressWarnings("unchecked")
	@Override
    @RestResource(exported = false)
    Tag save(Tag entity);

}
