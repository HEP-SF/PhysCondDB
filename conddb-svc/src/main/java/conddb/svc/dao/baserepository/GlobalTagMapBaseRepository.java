/**
 * 
 */
package conddb.svc.dao.baserepository;


import java.util.List;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.GlobalTagMap;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface GlobalTagMapBaseRepository extends CondDBPageAndSortingRepository<GlobalTagMap, Long>, JpaSpecificationExecutor<GlobalTagMap> {

	@Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g JOIN FETCH p.systemTag t WHERE p.globalTag.name = (:globaltag) and p.systemTag.name = (:tag)")
	GlobalTagMap findByGlobalTagAndTagName(@Param("globaltag")String gtag, @Param("tag") String tag);

	@Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g JOIN FETCH p.systemTag t WHERE p.globalTag.name = (:globaltag)")
	List<GlobalTagMap> findByGlobalTagName(@Param("globaltag")String gtag);

	@Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g JOIN FETCH p.systemTag t WHERE p.systemTag.name = (:tag)")
	List<GlobalTagMap> findByTagName(@Param("tag")String tag);

	@Query("SELECT distinct p FROM GlobalTagMap p JOIN FETCH p.globalTag g JOIN FETCH p.systemTag t WHERE p.id = (:id)")
	GlobalTagMap findByIdFetchTagAndGlobalTag(@Param("id")Long id);

}
