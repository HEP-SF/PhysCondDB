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
package conddb.cli;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import conddb.data.GlobalTag;
import conddb.data.GlobalTagMap;
import conddb.data.Iov;
import conddb.data.Tag;

/**
 * @author formica
 *
 */
@Component
public class CliJsonHandler {

	@Autowired
	private ObjectMapper objectMapper;
	
	public String toJson(Object obj) {
		try {
			return objectMapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Error in writing JSON";
	}
	public String toJsonPretty(Object obj) {
		try {
			return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "Error in writing JSON";
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getJsonContent(T clazz, File jsonfile) {

		try {
			Object obj = (T) objectMapper.readValue(jsonfile, (Class<T>) clazz);
			return (T) obj;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getJsonContent(T clazz, String jsonfile) {

		try {
			Object obj = (T) objectMapper.readValue(jsonfile, (Class<T>) clazz);
			return (T) obj;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Class<?> getPojoClass(String classname) {

		if (classname.equalsIgnoreCase("globaltag")) {
			return GlobalTag.class;
		} else if (classname.equalsIgnoreCase("tag")) {
			return Tag.class;
		} else if (classname.equalsIgnoreCase("iov")) {
			return Iov.class;
		} else if (classname.equalsIgnoreCase("globaltagmap")) {
			return GlobalTagMap.class;
		}
		return null;
	}

}
