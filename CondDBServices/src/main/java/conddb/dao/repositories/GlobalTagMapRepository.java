/**
 * 
 */
package conddb.dao.repositories;


import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.GlobalTagMapBaseRepository;
import conddb.data.GlobalTagMap;

/**
 * @author formica
 *
 */
//@RepositoryRestResource
@RestResource(exported = false)
public interface GlobalTagMapRepository extends CrudRepository<GlobalTagMap, Long>, GlobalTagMapBaseRepository {

//	@Query("SELECT p FROM GlobalTagMap p WHERE p.globalTag.name = (:globaltag) and p.systemTag.name = (:tag)")
//	GlobalTagMap findByGlobalTagAndTagName(@Param("globaltag")String gtag, @Param("tag") String tag);
}
