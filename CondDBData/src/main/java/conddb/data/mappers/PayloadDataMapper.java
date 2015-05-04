/**
 * 
 * This file is part of PhysCondDB.
 *
 *   PhysCondDB is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   PhysCondDB is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with PhysCondDB.  If not, see <http://www.gnu.org/licenses/>.
 **/
package conddb.data.mappers;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import conddb.data.PayloadData;

/**
 * @author formica
 *
 */
public class PayloadDataMapper implements RowMapper<PayloadData> {

	@Override
	public PayloadData mapRow(ResultSet rs, int rownum) throws SQLException {
		PayloadData pyd = new PayloadData();
		pyd.setHash(rs.getString("HASH"));
		pyd.setData(rs.getBytes("DATA"));
		return pyd;
	}

}
