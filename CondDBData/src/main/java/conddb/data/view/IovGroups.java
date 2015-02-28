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
package conddb.data.view;

import java.math.BigDecimal;


/**
 * @author formica
 *
 */
public class IovGroups implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8700232195277325799L;
	private Long id;
	private BigDecimal since;
	private String sinceString;
	/**
	 * 
	 */
	public IovGroups() {
		super();
	}
	/**
	 * @param since
	 * @param sinceString
	 */
	public IovGroups(BigDecimal since, String sinceString) {
		super();
		this.since = since;
		this.sinceString = sinceString;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public BigDecimal getSince() {
		return since;
	}
	public void setSince(BigDecimal since) {
		this.since = since;
	}
	public String getSinceString() {
		return sinceString;
	}
	public void setSinceString(String sinceString) {
		this.sinceString = sinceString;
	}
}
