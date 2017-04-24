package io.konig.content.gae;

import io.konig.content.EtagFactory;

public class EtagGenerator {

	public static void main(String[] args) {
		
		String text = "To thine own self be true";
		String etag = EtagFactory.createEtag(text.getBytes());
		
		System.out.println(etag);

	}

}
