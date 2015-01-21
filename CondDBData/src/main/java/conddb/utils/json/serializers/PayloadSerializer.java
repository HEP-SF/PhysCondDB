/**
 * 
 */
package conddb.utils.json.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import conddb.data.Payload;

/**
 * @author formica
 *
 */
public class PayloadSerializer extends JsonSerializer<Payload> {

	@Override
	public void serialize(Payload pyld, JsonGenerator jgen,
			SerializerProvider arg2) throws IOException,
			JsonProcessingException {
		
		String hashpayload = "NO_PAYLOAD";
		if (pyld != null) {
			hashpayload = pyld.getHash();
		} 
		jgen.writeStartObject();
		jgen.writeStringField("hash", hashpayload);
		jgen.writeEndObject();
	}

}
