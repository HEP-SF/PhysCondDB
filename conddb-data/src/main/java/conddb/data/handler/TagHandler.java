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

import conddb.data.Tag;

/**
 * @author formica
 *
 */
public class TagHandler implements CondDBObjectHandler<Tag, String> {

	@Override
	public Tag cloneObject(Tag source, String newname) {
		Tag newtag = new Tag(newname);
		newtag.setDescription(source.getDescription() + " - Cloned from "
				+ source.getName());
		newtag.setEndOfValidity(source.getEndOfValidity());
		newtag.setLastValidatedTime(source.getLastValidatedTime());
		newtag.setObjectType(source.getObjectType());
		newtag.setTimeType(source.getTimeType());
		newtag.setSynchronization(source.getSynchronization());
		return newtag;
	}

	@Override
	public Iterable<Tag> cloneObjectList(Iterable<Tag> source, String newname) {
		Set<Tag> newlist = new HashSet<Tag>();
		for (Tag tag : source) {
			Tag newtag = cloneObject(tag,newname);
			newlist.add(newtag);
		}
		return newlist;
	}

}
