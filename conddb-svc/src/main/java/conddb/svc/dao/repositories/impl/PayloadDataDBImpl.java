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
package conddb.svc.dao.repositories.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.data.mappers.PayloadDataMapper;
import conddb.data.utils.PayloadGenerator;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.repositories.PayloadRepository;

/**
 * @author formica
 *
 */
public class PayloadDataDBImpl implements PayloadDataBaseCustom {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	@Qualifier("daoDataSource")
	private DataSource ds;

	@Autowired
	private PayloadRepository payloadRepository;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * conddb.svc.dao.baserepository.PayloadDataBaseCustom#find(java.lang.String)
	 */
	@Override
	@Transactional
	public PayloadData find(String id) {
		log.info("Find payload " + id + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		return jdbcTemplate.queryForObject("select HASH,DATA from PHCOND_PAYLOAD_DATA where PHCOND_PAYLOAD_DATA.HASH=?",
				new Object[] { id }, new PayloadDataMapper());
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see conddb.svc.dao.baserepository.PayloadDataBaseCustom#save(conddb.data.
	 * PayloadData )
	 */
	@Override
	@Transactional
	public PayloadData save(PayloadData entity) {
//		PayloadData savedentity = this.saveBlob(entity);
		PayloadData savedentity = null;
		try {
			savedentity = this.saveBlobWithLobCreator(entity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return savedentity;
	}

	@Transactional
	protected PayloadData saveBlobAsBytes(PayloadData entity) {
		String sql = "INSERT INTO PHCOND_PAYLOAD_DATA " + "(HASH, DATA) VALUES (?, ?)";
		log.info("Insert payload " + entity.getHash() + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { entity.getHash(), entity.getData() });
		log.info("Insertion done...");
		entity.setUri(sql + " ; hash=" + entity.getHash());
		return entity;
	}

	protected PayloadData saveBlobWithLobCreator(PayloadData entity) throws IOException {
		final File blobIn = new File(entity.getUri());
		final InputStream blobIs = new FileInputStream(blobIn);
		LobHandler lobHandler = new DefaultLobHandler();
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		String sql = "INSERT INTO PHCOND_PAYLOAD_DATA " + "(HASH, DATA) VALUES (?, ?)";
		jdbcTemplate.execute(
		  sql,
		  new AbstractLobCreatingPreparedStatementCallback(lobHandler) {                         
		      protected void setValues(PreparedStatement ps, LobCreator lobCreator) 
		          throws SQLException {
		        ps.setString(1, entity.getHash());
		        lobCreator.setBlobAsBinaryStream(ps, 2, blobIs, (int)blobIn.length());           
		      }
		  }
		);
		blobIs.close();
		return entity;
	}


	@Override
	@Transactional
	public void delete(String id) {
		String sql = "DELETE FROM PHCOND_PAYLOAD_DATA " + " WHERE HASH=(?)";
		log.info("Remove payload with hash " + id + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		jdbcTemplate.update(sql, new Object[] { id });
		log.info("Entity removal done...");
	}

	@Override
	@Transactional
	public PayloadData saveNull() throws IOException, PayloadEncodingException {
		List<Payload> nullpyld = payloadRepository.findByObjectType("NULL");
		if (nullpyld != null & nullpyld.size()>0) {
			return this.find(nullpyld.get(0).getHash());
		}
		Map<String,Object> genmap = PayloadGenerator.createDefaultPayload();
		Payload pyld = (Payload) genmap.get("payload");
		PayloadData pyldata = (PayloadData) genmap.get("payloaddata");
		payloadRepository.save(pyld);
		this.saveBlobWithLobCreator(pyldata);
		return pyldata;
	}

}
