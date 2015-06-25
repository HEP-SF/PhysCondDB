package conddb.utils.bytes;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PayloadBytesHandler {

	public static byte[] getBytesFromInputStream(InputStream is) {
		try {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();

			int nRead;
			byte[] data = new byte[16384];

			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
