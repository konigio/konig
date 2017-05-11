package io.konig.core.util;

public class PathUtil {

	public static String toFilePath(String iri) {
		StringBuilder builder = new StringBuilder();
		
		int last = 0;
		for (int i=0; i<iri.length(); ) {
			int c = iri.codePointAt(i);
			i += Character.charCount(c);
			if (c == ':') {
				c = '/';
			}
			if (c=='/' && last=='/') {
				continue;
			}
			last = c;
			if (c < 128) {
				builder.appendCodePoint(c);
			} else {
				String hex = Integer.toHexString(c).toUpperCase();
				builder.append('%');
				builder.append(hex);
			}
		}
		
		
		return builder.toString();
	}
}
