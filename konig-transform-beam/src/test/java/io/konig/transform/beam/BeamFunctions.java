package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


public class BeamFunctions {


	/**
	 * Get the local name from a fully-qualified IRI
	 * @param iriString
	 * @return
	 */
	public static String localName(String iriString) {
		if (iriString != null) {
			int start = iriString.lastIndexOf('/');
			if (start<0) {
				start = iriString.lastIndexOf('#');
				if (start < 0) {
					start = iriString.lastIndexOf(':');
				}
			}
			
			if (start >= 0) {
				return iriString.substring(start+1);
			}
			
		}
		return null;
	}
	
	/**
	 * Strip the spaces from some text.
	 * @param text
	 * @return
	 */
	public static String stripSpaces(String text) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<text.length();) {
			int c = text.codePointAt(i);
			
			if (!Character.isSpaceChar(c)) {
				builder.appendCodePoint(c);
			}
			
			i += Character.charCount(c);
			
		}
		return builder.toString();
	}
}
