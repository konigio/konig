package io.konig.ldp.impl;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import io.konig.ldp.ResourceType;

public class LDPUtil {
	
	/**
	 * Check if a given media type is the type of an RDF source.
	 * @param mediaType The media type being tested.
	 * @return True if the given mediaType is an RDF format, and false otherwise.
	 */
	public static boolean isRdfSourceMediaType(String mediaType) {
		// TODO: support other kinds of media types.
		return
				"text/turtle".equals(mediaType) ||
				"application/ld+json".equals(mediaType);
	}
	
	/**
	 * Given the value of a 'Link' header, return the most specific
	 * ResourceType given by a 'type' link.
	 * @param linkValue
	 * @return
	 */
	public static ResourceType getResourceType(String linkValue) {
		
		ResourceType bestType = null;
		if (linkValue != null) {
			
			final int START = 1;
			final int BEGIN_URI = 2;
			final int END_URI = 3;
			final int BEGIN_REL = 4;
			final int T = 6;
			final int Y = 7;
			final int P = 8;
			final int E = 9;
			String uri = null;
			int state = START;
			int mark = 0;
			for (int i=0; i<linkValue.length(); i++) {
				int c = linkValue.charAt(i);
				switch (state) {
				
				case START :
					if (c=='<') {
						mark = i+1;
						state = BEGIN_URI;
					}
					break;
					
				case BEGIN_URI :
					if (c == '>') {
						uri = linkValue.substring(mark, i).trim();
						state = END_URI;
					}
					break;
					
				case END_URI :
					if (c == ';') {
						int j = linkValue.indexOf("rel", i+1);
						if ( j>0 &&		
							(j = linkValue.indexOf('=', j+3)) > 0 &&
							(j=linkValue.indexOf('"', j+1)) > 0
						) {
							i = j;
							state = BEGIN_REL;
						} else {
							state = START;
						}
					}
					break;
					
				case BEGIN_REL :
					state = 't' == Character.toLowerCase(c) ? T : START;
					break;
					
				case T :
					state = 'y' == Character.toLowerCase(c) ? Y : START;
					break;
					
				case Y :
					state = 'p' == Character.toLowerCase(c) ? P : START;
					break;
					
				case P :
					state = 'e' == Character.toLowerCase(c) ? E : START;
					break;
					
				case E :
					if (c == '"') {
						ResourceType type = ResourceType.fromURI(uri);
						if (bestType == null || (type!=null && type.isSubClassOf(bestType))) {
							bestType = type;
						}
					}
					state = START;
					break;
					
				}
				
			}
		}
		
		
		return bestType;
	}

}
