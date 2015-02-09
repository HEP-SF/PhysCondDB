/**
 * 
 */
package conddb.dao.baserepository;


import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.GlobalTagMap;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface GlobalTagMapBaseRepository extends CondDBPageAndSortingRepository<GlobalTagMap, Long> {

	@Query("SELECT p FROM GlobalTagMap p WHERE p.globalTag.name = (:globaltag) and p.systemTag.name = (:tag)")
	GlobalTagMap findByGlobalTagAndTagName(@Param("globaltag")String gtag, @Param("tag") String tag);

}
