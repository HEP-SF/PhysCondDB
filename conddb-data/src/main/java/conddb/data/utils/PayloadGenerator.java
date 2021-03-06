/**
 * 
 */
package conddb.data.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import conddb.data.Payload;
import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.data.handler.PayloadHandler;

/**
 * @author aformic
 *
 */
public class PayloadGenerator {

	private static String defaultPayload = "DEFAULT_NULL_PAYLOAD";
	private static String defaultpath = "/tmp/.defaultpayloadfile.txt";
	
//	public static Map<String,Object> createDefaultPayload() {
//		Payload pyld = new Payload();
//		PayloadData pylddata = new PayloadData();
//		pyld.setBackendInfo("none");
//		pyld.setObjectType("NULL");
//		pyld.setStreamerInfo("String");
//		pyld.setVersion("1.0");
//		
//		byte[] bytes = defaultPayload.getBytes();
//		pylddata.setData(bytes);
//
//		PayloadHandler phandler = new PayloadHandler(pylddata);
//		PayloadData storable = phandler.getPayloadWithHash();
//		pyld.setHash(storable.getHash());
//		pyld.setDatasize(bytes.length);
//		Map<String, Object> container = new HashMap<String,Object>();
//		container.put("payload", pyld);
//		container.put("payloaddata", storable);
//		return container;
//	}
	
	public static Map<String,Object> createDefaultPayload() throws IOException, PayloadEncodingException {
		Payload pyld = new Payload();
		PayloadData pylddata = new PayloadData();
		pyld.setBackendInfo("none");
		pyld.setObjectType("NULL");
		pyld.setStreamerInfo("String");
		pyld.setVersion("1.0");
		
		byte[] bytes = defaultPayload.getBytes();
		File defaultpayloadFile = new File(defaultpath);
		FileOutputStream ofs = new FileOutputStream(defaultpayloadFile);
		ofs.write(bytes);
		ofs.close();
		pylddata.setUri(defaultpath);
		
		PayloadHandler phandler = new PayloadHandler(pylddata);
		PayloadData storable = phandler.getPayloadWithHash();
		pyld.setHash(storable.getHash());
		pyld.setDatasize(bytes.length);
		Map<String, Object> container = new HashMap<String,Object>();
		container.put("payload", pyld);
		container.put("payloaddata", storable);
		container.put("blob", bytes);
		return container;
	}

}
