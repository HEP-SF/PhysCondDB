/**
 * 
 */
package conddb.svc.security.dao.repositories;

import java.math.BigDecimal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RestResource;

import conddb.data.security.LogCondRequests;
import conddb.svc.dao.baserepository.CondDBPageAndSortingRepository;

/**
 * @author aformic
 *
 */
public interface LogCondRequestsRepository extends CondDBPageAndSortingRepository<LogCondRequests, BigDecimal>, CrudRepository<LogCondRequests, BigDecimal> {

    @Override
    @RestResource(exported = false)
    void delete(BigDecimal id);

    @Override
    @RestResource(exported = false)
    void delete(LogCondRequests entity);

    @SuppressWarnings("unchecked")
	@Override
    @RestResource(exported = false)
    LogCondRequests save(LogCondRequests entity);

}
