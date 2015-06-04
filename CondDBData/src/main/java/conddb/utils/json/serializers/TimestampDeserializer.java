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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

/**
 * @author formica
 *
 */
@Component
public class TimestampDeserializer extends JsonDeserializer<Timestamp> {

	@Autowired
	TimestampFormat tsformat;

	private Logger log = LoggerFactory.getLogger(this.getClass());

	public TimestampDeserializer() {
		SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
	}

	@Override
	public Timestamp deserialize(JsonParser jp, DeserializationContext dc)
			throws IOException, JsonProcessingException {
		Timestamp tstamp = null;
		try {
			if (tsformat == null) {
				log.warn("Get an instance of the format here if no autowiring...but the pattern can be different!!");
				tsformat = new TimestampFormat();
			}
			log.debug("Use private version of deserializer...."
					+ tsformat.getLocformatter().toString());
			ZonedDateTime zdt = ZonedDateTime.parse(jp.getText(),
					tsformat.getLocformatter());
			tstamp = new Timestamp(zdt.toInstant().toEpochMilli());
		} catch (Exception ex) {
			// If an exception is catch on parsing, try as if it was
			// milliseconds
			log.error(ex.getMessage());
			log.warn("Failed parsing date, try with milliseconds: "
					+ jp.getText());
			log.error("Failed to deserialize using format "+tsformat.getLocformatter().toString());
			try {
				String millisec = jp.getText();
				tstamp = new Timestamp(new Long(millisec));
			} catch (Exception e) {
				log.error("Failed to deserialize using format "+tsformat.getLocformatter().toString());
				throw new JsonParseException(e.getMessage(),
						jp.getCurrentLocation());
			}
		}
		return tstamp;
	}

	public TimestampFormat getTsformat() {
		return tsformat;
	}

	public void setTsformat(TimestampFormat tsformat) {
		this.tsformat = tsformat;
	}

}
