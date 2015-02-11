package conddb.data.handler;

import conddb.data.Payload;
import conddb.data.exceptions.PayloadEncodingException;
import conddb.utils.hash.HashGenerator;

public class PayloadHandler {

	private Payload payload;

	/**
	 * @param payload
	 */
	public PayloadHandler(Payload payload) {
		super();
		this.payload = payload;
	}

	public Payload getPayloadWithHash() {
		try {
			if (this.payload.getHash().equals(this.createSpringHashFromBytes())) {
				throw new PayloadEncodingException("The hash does not correspond to the payload !!!");
			}
			if (this.payload.getHash() == null) {
				this.payload.setHash(this.createSpringHashFromBytes());
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

	public String createSpringHashFromBytes() throws PayloadEncodingException {
		String hash = "ERROR_IN_HASH";
		hash = HashGenerator.md5Spring(this.payload.getData());
		return hash;
	}

}
