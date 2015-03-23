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
		String streamer = "NO_STREAMER";
		String objtype = "NO_TYPE";
		String version = "NO_VERSION";
		Integer size = new Integer(0);
		if (pyld != null) {
			hashpayload = pyld.getHash();
			streamer = pyld.getStreamerInfo();
			objtype = pyld.getObjectType();
			size = pyld.getDatasize();
			version = pyld.getVersion();
		} 
		jgen.writeStartObject();
		jgen.writeStringField("hash", hashpayload);
		jgen.writeStringField("streamerInfo", streamer);
		jgen.writeStringField("objectType", objtype);
		jgen.writeStringField("version", version);
		jgen.writeNumberField("datasize", size);
		jgen.writeEndObject();
	}

}
