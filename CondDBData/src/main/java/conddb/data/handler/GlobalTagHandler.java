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

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import conddb.data.GlobalTag;

/**
 * @author formica
 *
 */
public class GlobalTagHandler implements CondDBObjectHandler<GlobalTag, String> {

	@Override
	public GlobalTag cloneObject(GlobalTag source, String newname) {
		GlobalTag newgtag = new GlobalTag(newname);
		newgtag.setDescription(source.getDescription() + " - Cloned from "
				+ source.getName());
		newgtag.setValidity(source.getValidity());
		newgtag.setRelease(source.getRelease());
		newgtag.setSnapshotTime(source.getSnapshotTime());
//		newgtag.setInsertionTime(new Timestamp(new Date().getTime()));
		newgtag.setLockstatus(source.getLockstatus());
		return newgtag;
	}

	@Override
	public Iterable<GlobalTag> cloneObjectList(Iterable<GlobalTag> source, String newname) {
		Set<GlobalTag> newlist = new HashSet<GlobalTag>();
		for (GlobalTag globalTag : source) {
			GlobalTag newgtag = cloneObject(globalTag,newname);
			newlist.add(newgtag);
		}
		return newlist;
	}

}
