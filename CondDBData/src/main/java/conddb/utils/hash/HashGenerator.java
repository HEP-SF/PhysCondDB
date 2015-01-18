package conddb.utils.hash;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.DigestUtils;

public class HashGenerator {
	
	private static Logger log = LoggerFactory.getLogger("HashGenerator");


	public static String md5Java(String message) {
		String digest = null;
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] hash = md.digest(message.getBytes("UTF-8"));
			// converting byte array to Hexadecimal String
			StringBuilder sb = new StringBuilder(2 * hash.length);
			for (byte b : hash) {
				sb.append(String.format("%02x", b & 0xff));
			}
			digest = sb.toString();
		} catch (UnsupportedEncodingException ex) {
			log.error(ex.getMessage());
		} catch (NoSuchAlgorithmException ex) {
			log.error(ex.getMessage());
		}
		return digest;
	}

	/**
	 * Spring framework also provides overloaded md5 methods. You can pass input
	 * as String or byte array and Spring can return hash or digest either as
	 * byte array or Hex String. Here we are passing String as input and getting
	 * MD5 hash as hex String. @
	 **/
	public static String md5Spring(String text) {
		try {
			return DigestUtils.md5DigestAsHex(text.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	/*
	 * public static String md5ApacheCommonsCodec(String content){ return
	 * DigestUtils.md5Hex(content); }
	 */
}

/*
 * import java.io.UnsupportedEncodingException; import
 * java.security.MessageDigest; import java.security.NoSuchAlgorithmException;
 * import java.util.logging.Level; import java.util.logging.Logger; import
 * org.apache.commons.codec.digest.DigestUtils;
 *//**
 * * Java program to generate MD5 hash or digest for String. In this example *
 * we will see 3 ways to create MD5 hash or digest using standard Java API, *
 * Spring framework and open source library, Apache commons codec utilities. *
 * Generally MD5 has are represented as Hex String so each of this function *
 * will return MD5 hash in hex format. * * @author Javin Paul
 */
/*
 * public class MD5Hash { public static void main(String args[]) { String
 * password = "password"; System.out.println("MD5 hash generated using Java : "
 * + md5Java(password)); System.out.println("MD5 digest generated using : " +
 * md5Spring(password));
 * System.out.println("MD5 message created by Apache commons codec : " +
 * md5ApacheCommonsCodec(password)); } }
 * 
 * public static String md5Java(String message){ String digest = null; try {
 * MessageDigest md = MessageDigest.getInstance("MD5"); byte[] hash =
 * md.digest(message.getBytes("UTF-8")); //converting byte array to Hexadecimal
 * String StringBuilder sb = new StringBuilder(2*hash.length); for(byte b :
 * hash){ sb.append(String.format("%02x", b&0xff)); } digest = sb.toString(); }
 * catch (UnsupportedEncodingException ex) {
 * Logger.getLogger(StringReplace.class.getName()).log(Level.SEVERE, null, ex);
 * } catch (NoSuchAlgorithmException ex) {
 * Logger.getLogger(StringReplace.class.getName()).log(Level.SEVERE, null, ex);
 * } return digest; } Spring framework also provides overloaded md5 methods. You
 * can pass input * as String or byte array and Spring can return hash or digest
 * either as byte * array or Hex String. Here we are passing String as input and
 * getting * MD5 hash as hex String. public static String md5Spring(String
 * text){ return DigestUtils.md5Hex(text); } Apache commons code provides many
 * overloaded methods to generate md5 hash. It contains * md5 method which can
 * accept String, byte[] or InputStream and can return hash as 16 element byte *
 * array or 32 character hex String. public static String
 * md5ApacheCommonsCodec(String content){ return DigestUtils.md5Hex(content); }
 * } } }
 */