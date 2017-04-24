package io.konig.content;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class EtagFactory {

	public static String createEtag(byte[] body) {
		
		byte[] sha1 = DigestUtils.sha1(body);
		return new String( Base64.encodeBase64URLSafe(sha1) );
	}
}
