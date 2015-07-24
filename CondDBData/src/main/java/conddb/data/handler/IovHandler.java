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
package conddb.data.handler;

import java.util.HashSet;
import java.util.Set;

import conddb.data.Iov;

/**
 * @author formica
 *
 */
public class IovHandler implements CondDBObjectHandler<Iov, Long> {

	@Override
	public Iov cloneObject(Iov source, Long newid) {
		Iov newiov = new Iov();
		newiov.setSince(source.getSince());
		newiov.setPayload(source.getPayload());
		newiov.setSinceString(source.getSinceString());
		
		return newiov;
	}

	@Override
	public Iterable<Iov> cloneObjectList(Iterable<Iov> source, Long newid) {
		Set<Iov> newlist = new HashSet<Iov>();
		for (Iov iov : source) {
			Iov newiov = cloneObject(iov,null);
			newlist.add(newiov);
		}
		return newlist;
	}

}
