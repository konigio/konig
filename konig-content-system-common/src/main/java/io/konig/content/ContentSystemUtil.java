package io.konig.content;

public class ContentSystemUtil {

	public static String trimSlashes(String value) {
		int start = value.charAt(0)=='/' ? 1 : 0;
		int end = value.length();
		if (value.charAt(end-1)=='/') {
			end--;
		}
		
		return value.substring(start, end);
	}
}
