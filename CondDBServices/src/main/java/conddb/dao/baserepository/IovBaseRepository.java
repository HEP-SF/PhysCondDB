/**
 *
 */
package conddb.dao.baserepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
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
public interface IovBaseRepository extends
		CondDBPageAndSortingRepository<Iov, Long> {

	/**
	 * TODO:
	 * May be hide this method to clients.
	 * @param tagname
	 * @return List of IOVs for a given tag name.
	 */
	List<Iov> findByTagName(@Param("tag_name") String tagname);
	/**
	 * @param payloadhash
	 * @return List of IOVs containing the payload_hash as reference.
	 */
	List<Iov> findByPayloadHash(@Param("payload_hash") String payloadhash);

	/**
	 * @param tagname
	 * @param since
	 * @param inserttime
	 * @return A single IOV.
	 */
	@Query("SELECT p FROM Iov p WHERE "
			+ "p.tag.name = (:name) AND p.since = (:since) AND p.insertionTime=(:instime)")
	Iov fetchBySinceAndInsertionTimeAndTagName(
			@Param("name") String tagname,
			@Param("since") BigDecimal since, 
			@Param("instime") Date inserttime);

	/**
	 * TODO:
	 * This is a test only method.
	 * Should be then hidden to clients.
	 * @param tagname
	 * @return All IOVs for given tag including payloads.
	 */
	@Query("SELECT p FROM Iov p JOIN FETCH p.payload pylds WHERE "
			+ "p.tag.name = (:name)")
	List<Iov> findByTagNameAndFetchPayloadEagerly(
			@Param("name") String tagname);

	/**
	 * @param tagname
	 * @param since
	 * @param until
	 * @return list of IOVs.
	 */
	@Query("SELECT p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since >= (:since) AND p.since < (:until))")
	List<Iov> findByRangeAndTag(
			@Param("tag") String tagname,
			@Param("since") BigDecimal since, 
			@Param("until") BigDecimal until);

	/**
	 * @param tagname
	 * @param since
	 * @param instime.
	 * 		Use yyyy-MM-dd hh:mm:ss in local time as format.
	 * @return list of IOVs.
	 */
	@Query("SELECT p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since = (:since) AND p.insertionTime < (:instime)) "
			+ "ORDER BY p.insertionTime desc")
	List<Iov> findBySinceAndTagAndInsertionTimeLessThanOrderByInsertionTimeDesc(
			@Param("tag") String tagname, 
			@Param("since") BigDecimal since, 
			@Param("instime") Timestamp instime);

	/**
	 * @param tagname
	 * @param since
	 * @param until
	 * @return list of IOVs.
	 */
	@Query("SELECT p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since >= (:since) AND p.since < (:until)) "
			+ "AND p.insertionTime >= ALL("
			+ "SELECT p2.insertionTime FROM Iov p2 WHERE "
			+ "p2.tag.name = (:tag) AND (p2.since = p.since))")
	List<Iov> findByRangeAndTagAndInsertionTimeMax(
			@Param("tag") String tagname, 
			@Param("since") BigDecimal since, 
			@Param("until") BigDecimal until);

	/**
	 * @param tagname
	 * @param since
	 * @param until
	 * @param instime.
	 * 		Use yyyy-MM-dd hh:mm:ss in local time as format.
	 * @return list of IOVs.
	 */
	@Query("SELECT p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since >= (:since) AND p.since < (:until)) "
			+ "AND p.insertionTime = ANY("
			+ "SELECT max(p2.insertionTime) FROM Iov p2 WHERE "
			+ "p2.insertionTime < (:instime))")
	List<Iov> findByRangeAndTagAndInsertionTimeLessThan(
			@Param("tag") String tagname, 
			@Param("since") BigDecimal since, 
			@Param("until") BigDecimal until, 
			@Param("instime") Timestamp instime);

	/**
	 * @param tagname
	 * @param since
	 * @param until
	 * @param instime.
	 * 		Use yyyy-MM-dd hh:mm:ss in local time as format.
	 * @return list of IOVs.
	 */
	@Query("SELECT p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since >= (:since) AND p.since < (:until)) "
			+ "AND p.insertionTime = ANY("
			+ "SELECT max(p2.insertionTime) FROM Iov p2 WHERE "
			+ "p2.insertionTime < (:instime))")
	List<Iov> findGroupsByTag(
			@Param("tag") String tagname);

	
}
