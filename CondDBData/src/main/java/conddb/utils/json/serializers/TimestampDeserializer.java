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
package conddb.utils.json.serializers;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author formica
 *
 */
public class TimestampDeserializer extends JsonDeserializer<Timestamp> {

	// DateTimeFormatter locformatter =
	// DateTimeFormatter.ofPattern("yyyyMMddHHmmss:z");
	DateTimeFormatter locformatter = DateTimeFormatter
			.ofPattern("yyyyMMddHHmmss:z");

	@Override
	public Timestamp deserialize(JsonParser jp, DeserializationContext dc)
			throws IOException, JsonProcessingException {
		Timestamp tstamp  =  null;
		try {
		ZonedDateTime zdt = ZonedDateTime.parse(jp.getText(), locformatter);
		tstamp  = new Timestamp(zdt.toInstant().toEpochMilli());
		} catch (Exception ex) {
			// If an exception is catched on parsing, try as if it was milliseconds
			ex.printStackTrace();
			System.out.println("try with milliseconds");
			String millisec = jp.getText();
			tstamp = new Timestamp(new Long(millisec));
		}
		return tstamp;
	}
}
