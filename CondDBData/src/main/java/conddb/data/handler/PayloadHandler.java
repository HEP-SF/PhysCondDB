package conddb.data.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conddb.data.PayloadData;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.utils.hash.HashGenerator;

public class PayloadHandler {

	private Logger log = LoggerFactory.getLogger(this.getClass());

	private PayloadData payload;

	/**
	 * @param payload
	 */
	public PayloadHandler(PayloadData payload) {
		super();
		this.payload = payload;
	}

	public PayloadData getPayloadWithHash() {
		try {
			if (this.payload.getHash() == null) {
				this.payload.setHash(this.createJavaShaHashFromBytes());
			} else if (!this.payload.getHash().equals(this.createJavaShaHashFromBytes())) {
				throw new PayloadEncodingException("The hash does not correspond to the payload !!!");
			}
		
			return this.payload;
		} catch (PayloadEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public String createJavaHashFromBytes() throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.md5Java(this.payload.getData());
		return hash;
	}

	public String createJavaShaHashFromBytes() throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.shaJava(this.payload.getData());
		return hash;
	}

	public String createSpringHashFromBytes() throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.md5Spring(this.payload.getData());
		log.info("Payload has hash "+hash);
		return hash;
	}

}
