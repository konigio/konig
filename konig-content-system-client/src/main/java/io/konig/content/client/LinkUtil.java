package io.konig.content.client;

import org.apache.http.Header;

public class LinkUtil {

	public static String getLink(Header[] headerList, String relation) {
		if (headerList != null) {
			for (Header header : headerList) {
				String value = header.getValue();
				
				int leftAngle = value.indexOf('<');
				int rightAngle = value.indexOf('>', leftAngle+1);
				int semicolon = value.indexOf(';', rightAngle+1);
				if (leftAngle>=0 && rightAngle>leftAngle && semicolon>rightAngle) {
					int mark = skipSpace(value, semicolon+1);
					if (matchWord(value, "rel", mark)) {
						mark += 3;
						mark = skipSpace(value, mark);
						char equals = value.charAt(mark);
						if (equals == '=') {
							mark = skipSpace(value, mark+1);
							if (matchWord(value, relation, mark)) {
								return value.substring(leftAngle+1, rightAngle);
							}
						}
					}
				}
			}
		}
		return null;
	}

	private static boolean matchWord(String text, String expected, int mark) {
		if (mark+expected.length() <= text.length()) {
			for (int i=0; i<expected.length(); i++) {
				char a = text.charAt(mark+i);
				char b = expected.charAt(i);
				if (a != b) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	private static int skipSpace(String value, int i) {
		for (; i<value.length(); i++) {
			char c = value.charAt(i);
			if (!Character.isWhitespace(c)) {
				return i;
			}
		}
		return -1;
	}

}
