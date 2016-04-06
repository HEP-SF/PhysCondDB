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
package conddb.cli.action;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;

import conddb.utils.json.serializers.TimestampFormat;

/**
 * @author formica
 *
 */
@Configurable
public class AtestClass {

	@Autowired
	private TimestampFormat tsformat;
	
	public AtestClass() {
		
	}
	
	public String getFormat() {
		return tsformat.getPattern();
	}

	public TimestampFormat getTsformat() {
		return tsformat;
	}

	public void setTsformat(TimestampFormat tsformat) {
		System.out.println("Setting tsformat in AtestClass");
		this.tsformat = tsformat;
	}
	
	
}
