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
package conddb.svc.dao.baserepository;

import java.io.IOException;

import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.svc.dao.exceptions.ConddbServiceException;

/**
 * @author formica
 *
 */
public interface PayloadDataBaseCustom {
	PayloadData find(String id);
	PayloadData save(PayloadData entity) throws ConddbServiceException;
	PayloadData saveNull() throws IOException, PayloadEncodingException;
	void delete(String id);
}
