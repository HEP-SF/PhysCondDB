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
package conddb.dao.repositories.impl;


import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;

import conddb.dao.baserepository.PayloadDataBaseCustom;
import conddb.data.PayloadData;
import conddb.data.mappers.PayloadDataMapper;

/**
 * @author formica
 *
 */
public class PayloadDataDBImpl implements PayloadDataBaseCustom {

	@Autowired
	@Qualifier("daoDataSource")
	private DataSource ds;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * conddb.dao.baserepository.PayloadDataBaseCustom#find(java.lang.String)
	 */
	@Override
	public PayloadData find(String id) {
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		return jdbcTemplate.queryForObject(
				"select HASH,DATA from PHCOND_PAYLOAD_DATA where HASH=:hash",
				new Object[] { id }, new PayloadDataMapper());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * conddb.dao.baserepository.PayloadDataBaseCustom#save(conddb.data.PayloadData
	 * )
	 */
	@Override
	public PayloadData save(PayloadData entity) {
		String sql = "INSERT INTO PHCOND_PAYLOAD_DATA "
				+ "(HASH, DATA) VALUES (?, ?)";
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql,
				new Object[] { entity.getHash(), entity.getData() });
		return this.find(entity.getHash());
	}

}
