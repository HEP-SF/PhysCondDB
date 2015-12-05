/**
 * 
 */
package conddb.security.svc.dao.repositories;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import conddb.security.data.LogCondRequests;
import conddb.svc.dao.baserepository.CondDBPageAndSortingRepository;

/**
 * @author aformic
 *
 */
public interface LogCondRequestsBaseRepository extends CondDBPageAndSortingRepository<LogCondRequests, BigDecimal> {
	
	/**
	 * @param username
	 * @return
	 */
	@Query("SELECT distinct p FROM LogCondRequests p WHERE p.userName = (:username)")
	List<LogCondRequests> findByUserName(@Param("username")String username);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM LogCondRequests p WHERE " 
			+ "p.start > (:starttime) ")
	List<LogCondRequests> findByStartTimeGt(@Param("starttime") Timestamp st);

	/**
	 * @param st
	 * @param httpmeth
	 * @return
	 */
	@Query("SELECT distinct p FROM LogCondRequests p WHERE " 
			+ "p.start > (:starttime) and p.httpMethod=(:httpmeth) order by p.start asc")
	List<LogCondRequests> findByStartTimeGtAndMethod(@Param("starttime") Timestamp st, @Param("httpmeth") String httpmeth);

}
