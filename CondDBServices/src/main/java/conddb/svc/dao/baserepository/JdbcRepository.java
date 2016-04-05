package conddb.svc.dao.baserepository;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import conddb.data.mappers.IovGroupsMapper;
import conddb.data.view.IovGroups;


@Repository
public class JdbcRepository {

	@Autowired
	@Qualifier("daoDataSource")
	private DataSource localDs;

	public DataSource getLocalDs() {
		return localDs;
	}

	public void setLocalDs(DataSource dataSource) {
		this.localDs = dataSource;
	}

	//FIXME: method not yet implemented
	public List<IovGroups> selectGroups(String tagname) throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(localDs);
		try {
			String tagrootname=tagname.split("-")[0];
			String sqlquery = "select   "
					+ " (FLOOR(iov.since/node.iovgroup_size) * node.iovgroup_size) as since, "
					+ " count(iov.since) as niovs, "
					+ " min(iov.since_string) as sinceString "
					+ " from PHCOND_IOV iov, PHCOND_TAG tag, PHCOND_SYSTEM_NODE node where "
					+ " tag.name=(:tagname) and "
					+ " iov.tag_id=tag.tag_id and "
					+ " node.tag_name_root=:tagrootname and "
					+ " iov.insertion_time >= ALL(select p.insertion_time from PHCOND_IOV p where p.since=iov.since and p.tag_id=tag.tag_id) "
					+ " group by (FLOOR(since/node.iovgroup_size) * node.iovgroup_size) ";
			return jdbcTemplate.query(sqlquery, 
					new Object[] { tagname, tagrootname }, 
					new IovGroupsMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw emptyResultDataAccessException;
		} catch (Exception e) {
			throw e;
		}
	}
	
	public List<IovGroups> selectGroups2(String tagname) throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(localDs);
		try {
			String tagrootname=tagname.split("-")[0];
			String sqlquery = "select   "
					+ " (FLOOR(iov.since/node.iovgroup_size) * node.iovgroup_size) as since, "
					+ " count(iov.since) as niovs, "
					+ " min(iov.since_string) as sinceString "
					+ " from PHCOND_IOV iov, PHCOND_TAG tag, PHCOND_SYSTEM_NODE node where "
					+ " tag.name=(:tagname) and "
					+ " iov.tag_id=tag.tag_id and "
					+ " node.tag_name_root=:tagrootname "
					+ " group by (FLOOR(since/node.iovgroup_size) * node.iovgroup_size) ";
			return jdbcTemplate.query(sqlquery, 
					new Object[] { tagname, tagrootname }, 
					new IovGroupsMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw emptyResultDataAccessException;
		} catch (Exception e) {
			throw e;
		}
	}


}
