/**
 *
 */
package conddb.dao.baserepository;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.Iov;

/**
 * @author formica
 *
 */
@RepositoryRestResource
public interface IovBaseRepository extends CondDBPageAndSortingRepository<Iov, Long> {

	public List<Iov> findByTagName(@Param("tag_name") String tagname);

	@Query("SELECT p FROM Iov p WHERE p.tag.name = (:name) AND p.since = (:since) AND p.insertionTime=(:instime)")
	public Iov fetchBySinceAndInsertionTimeAndTagName(
			@Param("name") String tagname, @Param("since") BigDecimal since,
			@Param("instime") Date inserttime);

	@Query("SELECT p FROM Iov p JOIN FETCH p.payload pylds WHERE p.tag.name = (:name)")
	public List<Iov> findByTagNameAndFetchPayloadEagerly(
			@Param("name") String tagname);

	@Query("SELECT p FROM Iov p WHERE p.tag.name = (:tag) AND (p.since >= (:since) AND p.since < (:until))")
	List<Iov> findByRangeAndTag(@Param("tag") String tagname,
			@Param("since") BigDecimal since, @Param("until") BigDecimal until);

	List<Iov> findByPayloadHash(@Param("payload_hash") String payloadhash);

}
