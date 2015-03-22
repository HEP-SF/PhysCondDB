/**
 * 
 */
package conddb.dao.baserepository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.Iov;
import conddb.data.Payload;

/**
 * @author formica
 *
 */
//@RepositoryRestResource(collectionResourceRel = "payloads", path = "payloads")
@RepositoryRestResource
public interface PayloadBaseRepository extends CondDBPageAndSortingRepository<Payload, String> {

	/**
	 * @param objtype
	 * @return payload have a given object type.
	 */
	List<Payload> findByObjectType(
			@Param("name") String otype);

	/**
	 * @param tagname
	 * @return payload have a given object type.
	 */
	List<Payload> findByVersion(
			@Param("version") String version);

	/**
	 * @param tagname
	 * @return payload have a given object type.
	 */
	@EntityGraph(value="graph.light.payload",type = EntityGraph.EntityGraphType.FETCH)
	List<Payload> findByDatasizeGreaterThan(
			@Param("datasize") Integer dsize);

	
}
