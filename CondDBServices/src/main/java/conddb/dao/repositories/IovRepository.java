/**
 * 
 */
package conddb.dao.repositories;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.dao.baserepository.IovBaseRepository;
import conddb.data.Iov;

/**
 * @author formica
 *
 */
//@RepositoryRestResource
@RestResource(exported = false)
public interface IovRepository extends CrudRepository<Iov, Long>, IovBaseRepository {

//	public List<Iov> findByTagName(@Param("tag_name") String tagname);
//
//	@Query("SELECT p FROM Iov p WHERE p.tag.name = (:name) AND p.since = (:since) AND p.insertionTime=(:instime)")
//	public Iov fetchBySinceAndInsertionTimeAndTagName(@Param("name") String tagname, 
//			@Param("since") BigDecimal since, @Param("instime") Date inserttime);
//	
//	@Query("SELECT p FROM Iov p JOIN FETCH p.payload pylds WHERE p.tag.name = (:name)")
//	public List<Iov> findByTagNameAndFetchPayloadEagerly(@Param("name") String tagname);
}
