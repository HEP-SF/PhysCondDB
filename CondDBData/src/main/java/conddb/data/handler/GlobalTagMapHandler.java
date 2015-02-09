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

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;

/**
 * @author formica
 *
 */
public class GlobalTagMapHandler implements CondDBObjectHandler<GlobalTagMap, GlobalTag> {

	@Override
	public GlobalTagMap cloneObject(GlobalTagMap source, GlobalTag id) {
		GlobalTagMap newmap = new GlobalTagMap(id,source.getSystemTag(),source.getRecord(),source.getLabel());
		return newmap;
	}

	@Override
	public Iterable<GlobalTagMap> cloneObjectList(
			Iterable<GlobalTagMap> source, GlobalTag id) {
		Set<GlobalTagMap> maplist = new HashSet<GlobalTagMap>();
		for (GlobalTagMap amap : source) {
			GlobalTagMap newmap = cloneObject(amap, id);
			maplist.add(newmap);
		}
		return maplist;
	}

}
