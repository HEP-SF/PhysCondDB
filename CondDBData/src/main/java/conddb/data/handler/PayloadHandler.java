package conddb.data.handler;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.utils.data.IStreamHash;
import conddb.utils.hash.HashGenerator;

public class PayloadHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private PayloadData payload;
	private InputStream payloadstream;

	/**
	 * @param payload
	 * @throws IOException 
	 */
	public PayloadHandler(PayloadData payload) throws IOException {
		super();
		this.payload = payload;
		if (this.payload.getUri() == null) {
			throw new IOException("Cannot use this entity to generate hash...no URI is defined");
		}
	}
	
	/**
	 * @param payload
	 * @throws IOException 
	 */
	public PayloadHandler(InputStream payloadstream) throws IOException {
		super();
		this.payloadstream = payloadstream;
	}

	public PayloadHandler(File data) throws IOException {
		super();
		this.payload = new PayloadData();
		this.payload.setUri(data.getCanonicalPath());
	}
	

	public PayloadData getPayloadWithHash() throws PayloadEncodingException {
		try {
			if (this.payload.getHash() == null) {
				// Add logic to generate hash from file
				File f = new File(this.payload.getUri());
				FileInputStream fstream = new FileInputStream(f);
				this.payload.setHash(this.createJavaHashFromStream(fstream));
			} 
			return this.payload;
		} catch (FileNotFoundException e) {
			throw new PayloadEncodingException("Cannot generate hash : "+e.getMessage());
		}
	}
	
	public InputStream getPayloadStream() {
		return payloadstream;
	}

	public IStreamHash createJavaIStreamHashFromStream(InputStream fstream) throws PayloadEncodingException {
		BufferedInputStream bis = new BufferedInputStream(fstream);
		try {
			IStreamHash ishash = HashGenerator.hashstream(bis);
			return ishash;
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new PayloadEncodingException(e.getMessage());
		}
	}

	public String createJavaHashFromStream(InputStream fstream) throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		BufferedInputStream bis = new BufferedInputStream(fstream);
		try {
			hash = HashGenerator.hash(bis);
			return hash;
		} catch (NoSuchAlgorithmException | IOException e) {
			throw new PayloadEncodingException(e.getMessage());
		}
	}
	

	public String createJavaHashFromBytes(byte[] bytes) throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.md5Java(bytes);
		return hash;
	}

	public String createJavaShaHashFromBytes(byte[] bytes) throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.shaJava(bytes);
		return hash;
	}

	public String createSpringHashFromBytes(byte[] bytes) throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.md5Spring(bytes);
		log.info("Payload has hash "+hash);
		return hash;
	}

}
