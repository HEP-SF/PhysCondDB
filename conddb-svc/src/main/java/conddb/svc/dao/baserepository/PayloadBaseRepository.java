/**
 * 
 */
package conddb.svc.dao.baserepository;

import java.util.List;

import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.Payload;

/**
 * @author formica
 *
 */
@Transactional(readOnly = true)
public interface PayloadBaseRepository extends CondDBPageAndSortingRepository<Payload, String> {

	/**
	 * @param otype
	 * 	The object type (used to determine the payload data.
	 * 
	 * @return payload have a given object type.
	 */
	List<Payload> findByObjectType(
			@Param("name") String otype);

	/**
	 * @param version
	 * 	The name of the tag.
	 * @return payload list.
	 */
	List<Payload> findByVersion(
			@Param("version") String version);

	
	/* (non-Javadoc)
	 * @see conddb.svc.dao.baserepository.CondDBPageAndSortingRepository#findOne(java.io.Serializable)
	 */
//	@EntityGraph(value="graph.detailed.payload",type = EntityGraph.EntityGraphType.FETCH)
	Payload findByHash(@Param("hash") String hash);
	
	/**
	 * @param  dsize
	 * 	The data size.
	 * 
	 * @return payload have a given object type.
	 */
	List<Payload> findByDatasizeGreaterThan(
			@Param("datasize") Integer dsize);

	
}
