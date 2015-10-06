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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.hibernate.engine.jdbc.StreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;

import conddb.data.PayloadData;

/**
 * @author formica
 *
 */
public class PayloadDataMapper implements RowMapper<PayloadData> {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	@Override
	public PayloadData mapRow(ResultSet rs, int rownum) throws SQLException {
		PayloadData pyd = new PayloadData();
		pyd.setHash(rs.getString("HASH"));
		try {
			LobHandler lobhandler = new DefaultLobHandler();
			String uri = "/tmp/"+pyd.getHash()+".blob";
//			Blob blob = rs.getBlob("DATA");
//			InputStream istream = blob.getBinaryStream();
			InputStream istream = lobhandler.getBlobAsBinaryStream(rs, "DATA");
			log.debug("retrieved blob stream from handler : "+istream.available()+" !");
			log.debug("copy stream into uri  : "+uri+" !");
			OutputStream out = new FileOutputStream(new File(uri));
			StreamUtils.copy(istream, out);
			out.close();
			pyd.setData(rs.getBlob("DATA"));
			pyd.setUri(uri);
		} catch (IOException e) {
			e.printStackTrace();
		}		
		return pyd;
	}

}
