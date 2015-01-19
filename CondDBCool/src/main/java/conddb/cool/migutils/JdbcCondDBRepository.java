package conddb.cool.migutils;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import conddb.cool.data.CoolIovType;
import conddb.cool.data.GtagTagType;
import conddb.cool.data.NodeType;

public class JdbcCondDBRepository {

	@Autowired
	@Qualifier("coolDataSource")
	private DataSource coolDs;

	public DataSource getCoolDs() {
		return coolDs;
	}

	public void setCoolDs(DataSource dataSource) {
		this.coolDs = dataSource;
	}


	public List<GtagTagType> getTagsAssociationsFromCool(String schemaname, String instanceId,
			String gtagname) throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(coolDs);
		try {
			String sqlquery = "select   "
					+ " gtag_name, "
					+ " gtag_description, "
					+ " gtag_lock_status, "
					+ " node_fullpath ,"
					+ " node_description ,"
					+ " schema_name, "
					+ " tag_name, "
					+ " tag_description, "
					+ " tag_lock_status, "
					+ " sys_instime "
					+ "from table(cool_select_pkg.f_getall_tagsforgtag(:schema,:dbname,:gtag))";

			return jdbcTemplate.query(sqlquery, new Object[] { schemaname,
					instanceId, gtagname }, new GtagTagMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw emptyResultDataAccessException;
		} catch (Exception e) {
			throw e;
		}
	}

	public List<CoolIovType> getIovRangeFromCool(String schemaname, String instanceId,
			String node, String tag, String channame, BigDecimal since, BigDecimal until) throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(coolDs);
		try {
			String sqlquery = "select   object_id,"
					+ " channel_name , "
					+ " channel_id ,"
					+ " iov_since ,"
					+ " iov_until ,"
					+ " user_tag_id ,"
					+ " tag_name ,"
					+ " sys_instime ,"
					+ " lastmod_date ,"
					+ " new_head_id, "
					+ " iov_base "
					+ " from table(cool_select_pkg.f_Get_IovsRangeForChannelName("
					+ " :schema,:db,:node,:tag,:channame,:since,:until)) "
					+ " order by channel_id, iov_since asc";

			return jdbcTemplate.query(sqlquery, new Object[] { schemaname,
					instanceId, node, tag, channame, since, until }, new CoolIovTypeMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw emptyResultDataAccessException;
		} catch (Exception e) {
			throw e;
		}
	}

	public List<NodeType> getNodesFromCool(String schemaname, String instanceId,
			String node) throws Exception {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(coolDs);
		try {
			String sqlquery= "select "
					+ " node_fullpath ," 
					+ " node_description ,"
					+ " folder_versioning ," 
					+ " schema_name, " 
					+ " dbname, "
					+ " iov_base, " 
					+ " iov_type " 
					+ "from table(cool_select_pkg.f_getall_nodes(:schema,:dbname,:node))";

			return jdbcTemplate.query(sqlquery, new Object[] { schemaname,
					instanceId, node}, new NodeTypeMapper());
		} catch (EmptyResultDataAccessException emptyResultDataAccessException) {
			throw emptyResultDataAccessException;
		} catch (Exception e) {
			throw e;
		}
	}

}
