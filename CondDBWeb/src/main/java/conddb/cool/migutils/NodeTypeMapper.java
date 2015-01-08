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
public class NodeTypeMapper implements RowMapper<NodeType> {

	@Override
	public NodeType mapRow(ResultSet rs, int rownum) throws SQLException {
		NodeType nt = new NodeType();
		nt.setDbName(rs.getString("DBNAME"));
		nt.setSchemaName(rs.getString("SCHEMA_NAME"));
		nt.setNodeFullpath(rs.getString("NODE_FULLPATH"));
		nt.setNodeDescription(rs.getString("NODE_DESCRIPTION"));
		nt.setNodeIovBase(rs.getString("iov_base"));
		nt.setNodeIovType(rs.getString("iov_type"));
		nt.setFolderVersioning(rs.getInt("folder_versioning"));

		return nt;
	}

}
