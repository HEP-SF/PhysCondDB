/**
 * 
 */
package conddb.svc.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import conddb.data.Tag;
import conddb.svc.dao.baserepository.TagBaseRepository;

/**
 * @author formica
 *
 */
@Repository
public interface TagRepository extends CrudRepository<Tag, Long>, TagBaseRepository {

    @Override
    void delete(Long id);

    @Override
    void delete(Tag entity);

    @SuppressWarnings("unchecked")
	@Override
    Tag save(Tag entity);
}
