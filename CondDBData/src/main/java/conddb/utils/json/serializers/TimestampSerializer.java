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
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * @author formica
 *
 */
@Component
public class TimestampSerializer extends JsonSerializer<Timestamp> {

	@Autowired
	TimestampFormat timestampFormat;
	
	private Logger log = LoggerFactory.getLogger(this.getClass()); 

	public TimestampSerializer(){
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);   
    }
	
	@Override
	public void serialize(Timestamp ts, JsonGenerator jg,
			SerializerProvider sp) throws IOException,
			JsonProcessingException {
		try {
			if (timestampFormat == null) {
				log.warn("Get an instance of the format here if no autowiring...but the pattern can be different!!");
				timestampFormat = new TimestampFormat();
			}
			log.debug("Use private version of serializer...."+timestampFormat.getLocformatter().toString());
			jg.writeString(this.format(ts));
		} catch (Exception ex) {
			log.error("Failed to serialize using format "+timestampFormat.getLocformatter().toString());
			ex.printStackTrace();
		}
	}

	public TimestampFormat getTimestampFormat() {
		return timestampFormat;
	}

	public void setTimestampFormat(TimestampFormat timestampFormat) {
		log.info("Set timestampFormat from Spring: "+timestampFormat.getPattern());
		this.timestampFormat = timestampFormat;
	}

	protected String format(Timestamp ts) throws Exception {
		Instant fromEpochMilli = Instant.ofEpochMilli(ts.getTime());
		ZonedDateTime zdt = fromEpochMilli.atZone(ZoneId.of("Europe/Paris"));
		return zdt.format(timestampFormat.getLocformatter());
	}
}
