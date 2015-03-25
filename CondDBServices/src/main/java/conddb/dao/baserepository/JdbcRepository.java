package conddb.dao.baserepository;

import java.sql.Timestamp;
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
	@Qualifier("localDataSource")
	private DataSource localDs;

	public DataSource getLocalDs() {
		return localDs;
	}

	public void setLocalDs(DataSource dataSource) {
		this.localDs = dataSource;
	}

	//FIXME: method not yet implemented
	public List<IovGroups> selectGroups(String tagname, Timestamp insertionTime) throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(localDs);
		try {
			String sqlquery = "select   "
					+ " max(tag.tag_name) as tagname, "
					+ " FLOOR(since/node.iovgroup_size) * node.iovgroup_size as since, "
					+ " count(*) as niovs, "
					+ " iov.insertionTime as insertionTime "
					+ "from IOV iov, TAG tag, SYSTEM_NODE node where "
					+ "tag.tag_name=(:tagname) group by FLOOR(since/node.iovgroup_size) * node.iovgroup_size ";
			return jdbcTemplate.query(sqlquery, 
					new Object[] { tagname, insertionTime }, 
					new IovGroupsMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw emptyResultDataAccessException;
		} catch (Exception e) {
			throw e;
		}
	}

}
