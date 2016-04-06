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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.support.AbstractLobCreatingPreparedStatementCallback;
import org.springframework.jdbc.core.support.SqlLobValue;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobCreator;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.transaction.annotation.Transactional;

import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.data.mappers.PayloadDataMapper;
import conddb.svc.dao.baserepository.PayloadDataBaseCustom;
import conddb.svc.dao.repositories.PayloadRepository;
import conddb.utils.bytes.PayloadBytesHandler;
import conddb.utils.data.PayloadGenerator;

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
	private PayloadBytesHandler payloadBytesHandler;
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

//	@Override
//	public PayloadData save(PayloadData entity, String fileblob) {
//		File f = new File(fileblob);
//		Blob blob;
//		try {
//			BufferedInputStream fstream = new BufferedInputStream(new FileInputStream(f));
//			blob = ds.getConnection().createBlob();
//			BufferedOutputStream bstream = new BufferedOutputStream(blob.setBinaryStream(1));
//			// stream copy runs a high-speed upload across the network
//			StreamUtils.copy(fstream, bstream);
//			entity.setData(blob);
//			entity = this.saveBlob(entity);
//			return entity;
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		return null;
//	}

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

	protected Blob getBlobFromFile(String filename) {
//		return payloadBytesHandler.createBlobFromFile(filename);
		return this.createBlobFromFile(filename);
	}
	
	@Transactional
	protected PayloadData saveBlob(PayloadData entity) {
		// FIXME: This does not WORK !!!! Attention !!!!
		String sql = "INSERT INTO PHCOND_PAYLOAD_DATA " + "(HASH, DATA) VALUES (?, ?)";
		log.info("Insert payload " + entity.getHash() + " using JDBCTEMPLATE");
		JdbcTemplate jdbcTemplate = new JdbcTemplate(ds);
		LobHandler lobHandler = new DefaultLobHandler();
//		Blob blob = (entity.getData() != null ? entity.getData() : getBlobFromFile(entity.getUri()));
		Blob blob = null;
		if (entity.getData() != null) {
			log.debug("Payload data is not null....check why");
		} else {
			blob = getBlobFromFile(entity.getUri());
		}
		try {
			jdbcTemplate.update(sql, new Object[] { entity.getHash(),
					new SqlLobValue(blob.getBinaryStream(), (int) blob.length(), lobHandler) },
			new int[] {Types.VARCHAR, Types.BLOB});
			log.info("Insertion done...");
			entity.setUri(sql + " ; hash=" + entity.getHash());
		} catch (DataAccessException | SQLException e) {
			DataIntegrityViolationException ex = new DataIntegrityViolationException(e.getMessage());
			ex.initCause(e);
			throw ex;
		}
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
//		Blob nullblob = payloadBytesHandler.createBlobFromFile(pyldata.getUri());
//		pyldata.setData(nullblob);
		payloadRepository.save(pyld);
		this.saveBlobWithLobCreator(pyldata);
		return pyldata;
	}

	//FIXME: This does not work .... but for the moment it is not needed....
	protected Blob createBlobFromFile(String filelocation) {
		Blob blob=null;
		try {
			log.debug("Fill blob using file in "+filelocation);
			File f = new File(filelocation);
			BufferedInputStream fstream = new BufferedInputStream(new FileInputStream(f));
			blob = ds.getConnection().createBlob();
			return blob;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return blob;
	}

}
