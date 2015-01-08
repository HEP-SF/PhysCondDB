package atlas.frontier.fdo;

/**
 * OutputStream for (slightly modified) public domain Base64Coder. $Id:
 * Base64CoderOutputStream.java,v 1.2 2007/05/24 15:30:27 dwd Exp $
 * 
 * @author Dave Dykstra dwd@fnal.gov
 * 
 */

import java.io.IOException;
import java.io.OutputStream;

class FrontierBase64CoderOutputStream extends java.io.FilterOutputStream {
	private final byte[] savebytes = new byte[3];
	private int nsavedbytes = 0;
	private final byte[] outbuf = new byte[76];
	private int noutbytes = 0;
	private final byte[] onebyte = new byte[1];

	public FrontierBase64CoderOutputStream(final OutputStream out) {
		super(out);
	}

	private void endline(final boolean isFinal) throws IOException {
		out.write(outbuf, 0, noutbytes);
		if (!isFinal) {
			out.write('\n');
		}
		noutbytes = 0;
	}

	@Override
	public void write(final int b) throws IOException {
		onebyte[0] = (byte) b;
		write(onebyte, 0, 1);
	}

	@Override
	public void write(final byte[] b, int off, int len) throws IOException {
		if (len == 0) {
			return;
		}
		if (nsavedbytes > 0) {
			/*
			 * add 1 or 2 bytes to leftover to make a new 3-byte group, encode
			 * it
			 */
			savebytes[nsavedbytes++] = b[off++];
			--len;
			if (len > 0 && nsavedbytes < 3) {
				savebytes[nsavedbytes++] = b[off++];
				--len;
			}
			if (nsavedbytes < 3) {
				return; /* needed 2 bytes, only got one */
			}
			noutbytes = FrontierBase64Coder.encode(savebytes, 0, nsavedbytes, outbuf,
					noutbytes);
			nsavedbytes = 0;
			if (noutbytes >= 76) {
				endline(false);
			}
			if (len == 0) {
				return;
			}
		}
		/* write out as many full 76-byte lines as available */
		/* note that noutbytes will always be a multiple of 4 */
		int maxwrite;
		while ((maxwrite = (76 - noutbytes) / 4 * 3) <= len) {
			noutbytes = FrontierBase64Coder.encode(b, off, maxwrite, outbuf, noutbytes);
			len -= maxwrite;
			off += maxwrite;
			endline(false);
		}
		/* write out amount left that's full 3-byte groups */
		maxwrite = len / 3 * 3;
		noutbytes = FrontierBase64Coder.encode(b, off, maxwrite, outbuf, noutbytes);
		len -= maxwrite;
		off += maxwrite;
		/* save leftover 1 or 2 bytes if there are any */
		if (len > 0) {
			savebytes[nsavedbytes++] = b[off++];
			--len;
			if (len > 0) {
				savebytes[nsavedbytes++] = b[off++];
				--len;
			}
		}
	}

	public void flushBase64() throws IOException {
		if (nsavedbytes > 0) {
			noutbytes = FrontierBase64Coder.encode(savebytes, 0, nsavedbytes, outbuf,
					noutbytes);
			nsavedbytes = 0;
		}
		endline(true);
		flush();
	}

	@Override
	public void flush() throws IOException {
		// don't flush "out"; save that for explicit flushing by caller
	}
}
