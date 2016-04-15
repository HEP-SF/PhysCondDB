/**
 *
 */
package conddb.svc.dao.baserepository;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.Iov;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface IovBaseRepository extends CondDBPageAndSortingRepository<Iov, Long>, JpaSpecificationExecutor<Iov>  {

	/**
	 * 
	 * @param iovid
	 *            The ID of the payload.
	 * 
	 * @return The corresponding IOV with the associated payload and tag objects.
	 */
	@Query("select iv from Iov iv join fetch iv.payload join fetch iv.tag where iv.id = :id")
	Iov findByIdFetchPayloadAndTag(@Param("id") Long iovid);

	/**
	 * TODO: May be hide this method to clients.
	 * 
	 * @param tagname
	 *            The name of the tag.
	 * 
	 * @return List of IOVs for a given tag name.
	 */
	Page<Iov> findByTagName(@Param("tag_name") String tagname, Pageable pageable);

	/**
	 * TODO: May be hide this method to clients.
	 * 
	 * @param tagname
	 *            The name of the tag.
	 * 
	 * @return List of IOVs for a given tag name.
	 */
	Page<Iov> findByTag(@Param("tag") Tag tag, Pageable pageable);

	/**
	 * TODO: Test paging for large resultset.
	 * 
	 * @param tagname
	 *            The name of the tag.
	 * 
	 * @return List of IOVs for a given tag name.
	 */
	@Query("select iv from Iov iv where iv.tag.name = :tagname")
	Page<Iov> findAllByTagName(@Param("tagname") String tagname, Pageable pageable);

	/**
	 * @param payloadhash
	 *            The payload Hash.
	 * @return List of IOVs containing the payload_hash as reference.
	 */
	List<Iov> findByPayloadHashOrderByIdAsc(@Param("payload_hash") String payloadhash);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param inserttime
	 *            The insertion time.
	 * 
	 * @return A single IOV.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE "
			+ "p.tag.name = (:name) AND p.since = (:since) AND p.insertionTime=(:instime)")
	Iov fetchBySinceAndInsertionTimeAndTagName(@Param("name") String tagname, @Param("since") BigDecimal since,
			@Param("instime") Date inserttime);

	/**
	 * TODO: This is a test only method. Should be then hidden to clients.
	 * 
	 * @param tagname
	 *            The name of the tag.
	 * @return All IOVs for given tag including payloads.
	 */
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.payload pylds WHERE " + "p.tag.name = (:name)")
	List<Iov> findByTagNameAndFetchPayloadEagerly(@Param("name") String tagname);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE " + "p.tag.id = (:tag) " + "AND p.insertionTime >= ALL("
			+ "SELECT p2.insertionTime FROM Iov p2 WHERE " + "p2.tag.id = p.tag.id AND (p2.since = p.since))")
	List<Iov> findByTagAndInsertionTimeMax(@Param("tag") Long id);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.payload pylds WHERE " + "p.tag.id = (:tag) " + "AND p.insertionTime >= ALL("
			+ "SELECT p2.insertionTime FROM Iov p2 WHERE " + "p2.tag.id = p.tag.id AND (p2.since = p.since))")
	List<Iov> findByTagAndInsertionTimeMaxFetchPayload(@Param("tag") Long id);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE " + "p.tag.id = (:tag) " + "AND p.insertionTime = ANY("
			+ "SELECT max(p2.insertionTime) FROM Iov p2 WHERE "
			+ "p2.tag.id = p.tag.id AND (p2.since = p.since) AND p2.insertionTime <= (:snapshot) "
			+ " group by p2.since)")
	List<Iov> findByTagAndInsertionTimeSnapshot(@Param("tag") Long id, @Param("snapshot") Timestamp snapshot);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p JOIN FETCH p.payload pylds WHERE " + "p.tag.id = (:tag) " + "AND p.insertionTime = ANY("
			+ "SELECT max(p2.insertionTime) FROM Iov p2 WHERE "
			+ "p2.tag.id = p.tag.id AND (p2.since = p.since) AND p2.insertionTime <= (:snapshot) "
			+ " group by p2.since)")
	List<Iov> findByTagAndInsertionTimeSnapshotFetchPayload(@Param("tag") Long id, @Param("snapshot") Timestamp snapshot);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE " + "p.tag.id = (:tag) AND (p.since >= (:since) AND p.since < (:until))")
	List<Iov> findByRangeAndTag(@Param("tag") Long id, @Param("since") BigDecimal since,
			@Param("until") BigDecimal until);

	/**
	 * Same query as before using pagination
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE " + "p.tag.id = (:tag) AND (p.since >= (:since) AND p.since < (:until))")
	Page<Iov> findByRangeAndTag(@Param("tag") Long id, @Param("since") BigDecimal since,
			@Param("until") BigDecimal until, Pageable pageable);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE "
			+ "(p.tag.id = (:tag) AND (p.since >= (:since) AND p.since < (:until))) " 
			+ "AND p.insertionTime >= ALL("
			+ "SELECT p2.insertionTime FROM Iov p2 WHERE " 
			+ "p2.tag.id = p.tag.id AND (p2.since = p.since)) ")
	List<Iov> findByRangeAndTagAndInsertionTimeMax(@Param("tag") Long id, @Param("since") BigDecimal since,
			@Param("until") BigDecimal until);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE "
			+ "p.tag.id = (:tag) " 
			+ "AND p.insertionTime >= ALL("
			+ "SELECT p2.insertionTime FROM Iov p2 WHERE " 
			+ "p2.tag.id = p.tag.id AND (p2.since = p.since)) order by p.since desc ")
	Page<Iov> findByTagAndInsertionTimeMax(@Param("tag") Long id, Pageable pageable);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param instime
	 *            The insertion time. Use yyyy-MM-dd hh:mm:ss in local time as
	 *            format.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since = (:since) AND p.insertionTime < (:instime)) "
			+ "ORDER BY p.insertionTime desc")
	List<Iov> findBySinceAndTagAndInsertionTimeLessThanOrderByInsertionTimeDesc(@Param("tag") String tagname,
			@Param("since") BigDecimal since, @Param("instime") Timestamp instime);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interva.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE "
			+ "p.tag.name = (:tag) AND (p.since >= (:since) AND p.since < (:until)) " + "AND p.insertionTime >= ALL("
			+ "SELECT p2.insertionTime FROM Iov p2 WHERE " + "p2.tag.name = (:tag) AND (p2.since = p.since))")

	List<Iov> findByRangeAndTagAndInsertionTimeMax(@Param("tag") String tagname, @Param("since") BigDecimal since,
			@Param("until") BigDecimal until);

	/**
	 * @param tagname
	 *            The name of the tag.
	 * @param since
	 *            The since time of the interval.
	 * @param until
	 *            The until time of the selected interval.
	 * @param instime.
	 *            The insertion time. Use yyyy-MM-dd hh:mm:ss in local time as
	 *            format.
	 * @return list of IOVs.
	 */
	@Query("SELECT distinct p FROM Iov p WHERE "
			+ "(p.tag.id = (:tag) AND (p.since >= (:since) AND p.since < (:until))) " 
			+ "AND p.insertionTime = ANY("
			+ "SELECT max(p2.insertionTime) FROM Iov p2 WHERE " 
			+ "(p2.tag.id = p.tag.id AND (p2.since = p.since)) "
			+ "AND p2.insertionTime < (:instime)"
			+ " group by p2.since)")
	List<Iov> findByRangeAndTagAndInsertionTimeLessThan(@Param("tag") Long tagid, @Param("since") BigDecimal since,
			@Param("until") BigDecimal until, @Param("instime") Timestamp instime);

}
