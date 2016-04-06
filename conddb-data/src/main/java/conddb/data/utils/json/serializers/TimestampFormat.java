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
package conddb.data.utils.json.serializers;

import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author formica
 *
 */
@Component
public class TimestampFormat {
	
	private Logger log = LoggerFactory.getLogger(this.getClass());

	private DateTimeFormatter locFormatter;
	private String pattern="yyyyMMddHHmmss:z";
	/**
	 * 
	 */
	public TimestampFormat() {
		super();
		log.info("Created object using pattern "+pattern);
	}

	/**
	 * @param locFormatter
	 */
	public TimestampFormat(String pattern) {
		super();
		this.pattern = pattern;
		log.info("Created object using pattern in constructor "+pattern);
	}

	public DateTimeFormatter getLocformatter() throws IllegalArgumentException {
		if(pattern.equals("ISO_OFFSET_DATE_TIME")){
			locFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		} else if(pattern.equals("ISO_LOCAL_DATE_TIME")) {
			locFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
		}else{
			locFormatter = DateTimeFormatter.ofPattern(pattern);
		}
		return locFormatter;
	}

	public String getPattern() {
		return pattern;
	}

	public void setPattern(String pattern) {
		this.pattern = pattern;
		log.info("Setting pattern "+pattern);
	}

}
