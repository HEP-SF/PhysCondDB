package org.cea.physconddb.data;

import conddb.data.PayloadData;
import conddb.data.handler.PayloadHandler;

public class TestHashing {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		String hashex = "ba2b45bdc11e2a4a6e86aab2ac693cbb";
		PayloadData pd = new PayloadData();
		System.out.println("the payload is "+pd.getData());
		PayloadHandler handler = new PayloadHandler(pd);
		PayloadData storable = handler.getPayloadWithHash();
		System.out.println("the payload hash is "+storable.getHash()+" compare to "+hashex);
		if (storable.getHash().equals(hashex)) {
			System.out.println(" hashing is the same ...");
		}
		
	}

}
