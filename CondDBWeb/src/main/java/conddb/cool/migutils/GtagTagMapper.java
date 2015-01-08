/**
 * 
 */
package conddb.cool.migutils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * @author formica
 *
 */
public class GtagTagMapper implements RowMapper<GtagTagType> {

	@Override
	public GtagTagType mapRow(ResultSet rs, int rownum) throws SQLException {
		GtagTagType nt = new GtagTagType();
		nt.setNodeFullpath(rs.getString("NODE_FULLPATH"));
		nt.setGtagName(rs.getString("GTAG_NAME"));
		nt.setGtagDescription(rs.getString("GTAG_DESCRIPTION"));
		nt.setGtagLockStatus(rs.getInt("gtag_lock_status"));
		nt.setTagName(rs.getString("TAG_NAME"));
		nt.setTagDescription(rs.getString("TAG_DESCRIPTION"));
		nt.setTagLockStatus(rs.getInt("tag_lock_status"));
		nt.setSysInstime(rs.getString("SYS_INSTIME"));
		nt.setNodeDescription(rs.getString("NODE_DESCRIPTION"));
		nt.setSchemaName(rs.getString("SCHEMA_NAME"));
		return nt;
	}

}
