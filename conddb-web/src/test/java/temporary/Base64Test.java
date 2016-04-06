package temporary;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

public class Base64Test {

	public static void main(String[] args) {
		
		String home = System.getProperty("user.home");
		File file = new File(home + File.separatorChar + ".align" + File.separatorChar + ".login");
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			int size = is.available();
			System.out.println("Input Stream is " + size);
			byte[] credentials = new byte[size];
			int ret = is.read(credentials);
			if (ret != credentials.length) {
				System.out.println("input stream has size " + size + " but we read " + ret + " bytes !");
			}
			Base64 decoder = new Base64();
			String c = new String(decoder.decode(credentials));
			// String c = new String(new
			// sun.misc.BASE64Decoder().decodeBuffer(is));
			int i = c.indexOf(":");
			if (i == -1) {
				return;
			}
			String userName = c.substring(0, i);
			String	password = c.substring(i + 1);
			System.out.println("Using username : " + userName);
			System.out.println("Using password : " + password);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
