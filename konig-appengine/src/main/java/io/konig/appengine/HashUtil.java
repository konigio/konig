package io.konig.appengine;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class HashUtil {

	public static String sha1Base64(String text) {
		byte[] bytes = DigestUtils.sha1(text);
		return Base64.encodeBase64URLSafeString(bytes);
	}
}
