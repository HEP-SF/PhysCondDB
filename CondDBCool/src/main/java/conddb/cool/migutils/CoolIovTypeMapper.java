/**
 * 
 */
package conddb.cool.migutils;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import conddb.cool.data.CoolIovType;

/**
 * @author formica
 *
 */
public class CoolIovTypeMapper implements RowMapper<CoolIovType> {

	@Override
	public CoolIovType mapRow(ResultSet rs, int rownum) throws SQLException {
		CoolIovType nt = new CoolIovType();
		nt.setChannelName(rs.getString("CHANNEL_NAME"));
		nt.setChannelId(rs.getLong("CHANNEL_ID"));
		nt.setIovSince(rs.getBigDecimal("IOV_SINCE"));
		nt.setIovUntil(rs.getBigDecimal("IOV_UNTIL"));
		nt.setTagId(rs.getLong("USER_TAG_ID"));
		nt.setTagName(rs.getString("TAG_NAME"));
		nt.setLastmodDate(rs.getTimestamp("LASTMOD_DATE"));
		nt.setIovBase(rs.getString("IOV_BASE"));
		nt.setSysInstime(rs.getTimestamp("SYS_INSTIME"));
		return nt;
	}
}
