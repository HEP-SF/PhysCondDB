package atlas.frontier.fdo;

/**
 * Implementation of BLOB encoder (Base64 encoded binary stream) which provides
 * carrying types information. $Id: BlobTypedEncoder.java,v 1.12 2010/07/28
 * 22:05:30 dwd Exp $
 * 
 * @author Sergey Kosyakov
 * 
 *         Changes log 08/18/2004 if String, byte[] or Date is null, set the
 *         NULL bit in the type info writeEOR() added, makes demarshalling more
 *         flexible
 * 
 *         Copyright (c) 2009, FERMI NATIONAL ACCELERATOR LABORATORY All rights
 *         reserved.
 * 
 *         For details of the Fermitools (BSD) license see Fermilab-2009.txt or
 *         http://fermitools.fnal.gov/about/terms.html
 */

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestOutputStream;
import java.security.MessageDigest;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/* this filter can be inserted to print out a datastream in hex */
class MyDebugOutputStream extends java.io.FilterOutputStream {
	private int cnt = 0;

	public MyDebugOutputStream(final OutputStream out) {
		super(out);
	}

	@Override
	public void write(final int b) throws IOException {
		if (cnt++ == 0) {
			System.out.print("MyDebugOut: [");
		}
		String hs = Integer.toHexString(b & 0xff);
		if (hs.length() == 1) {
			hs = "0" + hs;
		}
		System.out.println(hs);
		super.write(b);
	}

	@Override
	public void flush() throws IOException {
		if (cnt > 0) {
			System.out.println("] " + cnt + " bytes written");
			cnt = 0;
		}
		super.flush();
	}
}

public class FrontierBlobTypedEncoder implements FrontierEncoder {
	public static final byte BIT_NULL = (byte) (1 << 7); // Signals that the
															// value is NULL
	public static final byte TYPE_BYTE = 0;
	public static final byte TYPE_INT4 = 1;
	public static final byte TYPE_INT8 = 2;
	public static final byte TYPE_FLOAT = 3;
	public static final byte TYPE_DOUBLE = 4;
	public static final byte TYPE_TIME = 5;
	public static final byte TYPE_ARRAY_BYTE = 6;
	public static final byte TYPE_EOR = 7; // End Of Record

	private static final int BUFFER_SIZE = 16384;
	// measurements showed 512 to be optimimum size for unzipped BLOBs
	// on 100mbit net but had little effect on zipped BLOBs
	private static final int IN_BUFFER_SIZE = 512;

	private byte[] streambuf = null;

	private DataOutputStream os;
	private final MessageDigest md5;
	// private Base64.OutputStream b64os;
	private FrontierBase64CoderOutputStream b64os;
	private final DigestOutputStream dgos;
	private DeflaterOutputStream zos;
	// private ZOutputStream zos;

	private int ziplevel = 0;
	private long out_size = 0;

	public FrontierBlobTypedEncoder(final OutputStream out, final String param)
			throws Exception {
		// b64os=new Base64.OutputStream(out);
		b64os = new FrontierBase64CoderOutputStream(out);
		md5 = MessageDigest.getInstance("MD5");
		dgos = new DigestOutputStream(b64os, md5);

		if (param.length() >= 3 && param.substring(0, 3).equals("zip")) {
			final String zipparam = param.substring(3);
			if (zipparam.equals("")) {
				ziplevel = 5;
			} else {
				Integer zipint;
				try {
					zipint = new Integer(zipparam);
				} catch (final Exception e) {
					throw new Exception("BLOB zip level " + zipparam + " not an integer");
				}
				ziplevel = zipint.intValue();
				if (ziplevel < 0 || ziplevel > 9) {
					throw new Exception("BLOB zip level less than 0 or greater than 9");
				}
			}
		} else if (param.length() != 0) {
			throw new Exception("Unrecognized BLOB sub-encoding " + param);
			// System.out.println("BLOB zip level ["+ziplevel+"]");
		}

		OutputStream selectos;
		if (ziplevel > 0) {
			zos = new DeflaterOutputStream(dgos, new Deflater(ziplevel));
			// zos=new ZOutputStream(dgos,ziplevel);
			selectos = zos;
		} else {
			zos = null;
			selectos = dgos;
		}
		os = new DataOutputStream(new BufferedOutputStream(selectos, BUFFER_SIZE));
	}

	@Override
	public void writeEOR() throws Exception {
		os.writeByte(TYPE_EOR);
		out_size += 1;
	}

	@Override
	public void writeInt(final int v) throws Exception {
		os.writeByte(TYPE_INT4);
		os.writeInt(v);
		out_size += 5;
	}

	@Override
	public void writeLong(final long v) throws Exception {
		os.writeByte(TYPE_INT8);
		os.writeLong(v);
		out_size += 9;
	}

	@Override
	public void writeDouble(final double v) throws Exception {
		os.writeByte(TYPE_DOUBLE);
		os.writeDouble(v);
		out_size += 9;
	}

	@Override
	public void writeFloat(final float v) throws Exception {
		os.writeByte(TYPE_FLOAT);
		os.writeFloat(v);
		out_size += 5;
	}

	@Override
	public void writeString(final String v) throws Exception {
		if (v == null) {
			os.writeByte(TYPE_ARRAY_BYTE | BIT_NULL);
			out_size += 1;
		} else {
			os.writeByte(TYPE_ARRAY_BYTE);
			os.writeInt(v.length());
			out_size += 5;
			os.writeBytes(v);
			out_size += v.length();
		}
	}

	@Override
	public void writeBytes(final byte[] v) throws Exception {
		if (v == null) {
			os.writeByte(TYPE_ARRAY_BYTE | BIT_NULL);
			out_size += 1;
		} else {
			// System.out.println("writeBytes(): length "+v.length);
			os.writeByte(TYPE_ARRAY_BYTE);
			os.writeInt(v.length);
			out_size += 5;
			os.write(v, 0, v.length);
			out_size += v.length;
		}
	}

	@Override
	public void writeStream(final InputStream is, final int len) throws Exception {
		if (is == null) {
			os.writeByte(TYPE_ARRAY_BYTE | BIT_NULL);
			out_size += 1;
		} else {
			if (streambuf == null) {
				streambuf = new byte[IN_BUFFER_SIZE];
			}
			os.writeByte(TYPE_ARRAY_BYTE);
			os.writeInt(len);
			out_size += 5;
			int in_size = len;
			while (in_size > 0) {
				int read_size;
				if (in_size >= IN_BUFFER_SIZE) {
					read_size = is.read(streambuf, 0, IN_BUFFER_SIZE);
				} else {
					read_size = is.read(streambuf, 0, in_size);
				}
				if (read_size <= 0) {
					throw new Exception("Blob read unexpectedly returned " + read_size);
				}
				os.write(streambuf, 0, read_size);
				in_size -= read_size;
			}
			out_size += len;
		}
	}

	@Override
	public void writeDate(final java.util.Date v) throws Exception {
		if (v == null) {
			os.writeByte(TYPE_TIME | BIT_NULL);
			out_size += 1;
		} else {
			os.writeByte(TYPE_TIME);
			os.writeLong(v.getTime());
			out_size += 9;
		}
	}

	@Override
	public long getOutputSize() {
		return out_size;
	}

	@Override
	public byte[] getMD5Digest() throws Exception {
		return md5.digest();
	}

	@Override
	public void flush() throws Exception {
		os.flush();
		if (zos != null) {
			zos.finish();
		}
		b64os.flushBase64();
	}

	@Override
	public void close() throws Exception {
		flush();
		os = null;
		zos = null;
		b64os = null;
	}
}
