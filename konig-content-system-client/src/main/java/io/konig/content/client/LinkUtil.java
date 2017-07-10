package io.konig.content.client;

/*
 * #%L
 * Konig Content System, Client Library
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


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
