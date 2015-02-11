/**
 * 
 */
package conddb.dao.baserepository;

import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import conddb.data.Payload;

/**
 * @author formica
 *
 */
//@RepositoryRestResource(collectionResourceRel = "payloads", path = "payloads")
@RepositoryRestResource
public interface PayloadBaseRepository extends CondDBPageAndSortingRepository<Payload, String> {

	
}
