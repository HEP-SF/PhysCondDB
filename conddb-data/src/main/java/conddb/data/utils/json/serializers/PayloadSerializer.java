/**
 * 
 */
package conddb.data.utils.json.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import conddb.data.Payload;
import conddb.data.PayloadData;

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
		String data = "DATA_NOT_LOADED";
		if (pyld != null) {
			hashpayload = pyld.getHash();
			streamer = pyld.getStreamerInfo();
			objtype = pyld.getObjectType();
			size = pyld.getDatasize();
			version = pyld.getVersion();
			PayloadData pd = pyld.getData();
//			data = new String(pd.getData());
			data = new String("payload blob stored in "+pd.getUri());
		} 
		jgen.writeStartObject();
		jgen.writeStringField("hash", hashpayload);
		jgen.writeStringField("streamerInfo", streamer);
		jgen.writeStringField("objectType", objtype);
		jgen.writeStringField("version", version);
		jgen.writeNumberField("datasize", size);
		jgen.writeStringField("data", data);		
		jgen.writeEndObject();
	}

}
