package io.konig.core.util;

/*
 * #%L
 * konig-core
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


public class StringUtil {
    

	private static final int OTHER = 0;
	private static final int DELIM = 0;
	private static final int LOWER = 1;
	private static final int UPPER = 2;
	
	
	
	private static final String DELIMITER = "_-./:";
	
	public static final String LABEL_TO_SNAKE_CASE(String label) {
		StringBuilder builder = new StringBuilder();
		
		
		label = label.trim();
		
		int priorCase = OTHER;
		int prior = 0;
		
		for (int i=0; i<label.length();) {
			int c = label.codePointAt(i);
			
			if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
				
				if (prior == '_') {
					i += Character.charCount(c);
					continue;
				}
				c = '_';
			}
			if (c=='_' && i==(label.length()-1)) {
				break;
			}
			int caseValue = caseValue(c);
			
			if (i>1 && caseValue!=OTHER && prior!='_' && caseValue!=priorCase  && builder.codePointAt(builder.length()-2)!='_') {
				builder.append('_');
			}
			prior = c;
			priorCase = caseValue;
			
			builder.appendCodePoint(Character.toUpperCase(c));
			i += Character.charCount(c);
		}
		
		return builder.toString();
	}
	
	public static final String SNAKE_CASE(String text) {
		StringBuilder builder = new StringBuilder();
		
		if (text.indexOf('_') > 0) {
			return text.toUpperCase();
		}
		
		
		int priorCase = OTHER;
		int prior = 0;
		
		for (int i=0; i<text.length();) {
			int c = text.codePointAt(i);
			int caseValue = caseValue(c);
			
			if (i>1 && caseValue!=OTHER && prior!='_' && caseValue!=priorCase  && builder.codePointAt(builder.length()-2)!='_') {
				builder.append('_');
			}
			prior = c;
			priorCase = caseValue;
			builder.appendCodePoint(Character.toUpperCase(c));
			i += Character.charCount(c);
		}
		
		return builder.toString();
	}

	
	private static int caseValue(int c) {
		if (Character.isAlphabetic(c)) {
			if (Character.isUpperCase(c)) {
				return UPPER;
			}
			if (Character.isLowerCase(c)) {
				return LOWER;
			}
		}
		return OTHER;
	}
	
	public static final String capitalize(String text) {
		StringBuilder builder = new StringBuilder(text.length());
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			
			if (i == 0) {
				c = Character.toUpperCase(c);
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
	
	
	
	public static String PascalCase(String text) {
	
		
		StringBuilder builder = new StringBuilder();
		

		int charType = DELIM;
		
		for (int i=0; i<text.length(); ) {
			int c = text.codePointAt(i);
			i += Character.charCount(c);
			
			if (DELIMITER.indexOf(c)>=0) {
				// skip delimiters
				charType = DELIM;
				continue;
			}
			
			
			int newCharType = Character.isUpperCase(c) ? UPPER : LOWER;
			
			if (charType == DELIM) {
				c = Character.toUpperCase(c);
				newCharType = UPPER;
				
			} else if (charType==LOWER && newCharType==UPPER) {
				// Do nothing
				
			} else {
				c = Character.toLowerCase(c);
			}
			charType = newCharType;
			
			builder.appendCodePoint(c);
			
		}
		
		return builder.toString();
	}
	
	
	public static String camelCase(String text) {
		
		StringBuilder builder = new StringBuilder();
		

		boolean firstWord = true;
		int charType = DELIM;
		
		for (int i=0; i<text.length(); ) {
			int c = text.codePointAt(i);
			i += Character.charCount(c);
			
			if (DELIMITER.indexOf(c)>=0) {
				// skip delimiters
				charType = DELIM;
				continue;
			}
			
			
			int newCharType = Character.isUpperCase(c) ? UPPER : LOWER;
			
			if (charType == DELIM) {
				if (firstWord) {
					c = Character.toLowerCase(c);
					firstWord = false;
				} else {
					c = Character.toUpperCase(c);
				}
				newCharType = UPPER;
				
			} else if (charType==LOWER && newCharType==UPPER) {
				// Do nothing
				
			} else {
				c = Character.toLowerCase(c);
			}
			charType = newCharType;
			
			builder.appendCodePoint(c);
			
		}
		
		return builder.toString();
	}

	public static final String firstLetterLowerCase(String text) {
		StringBuilder builder = new StringBuilder(text.length());
		for (int i=0; i<text.length(); i++) {
			char c = text.charAt(i);
			if (i == 0) {
				c = Character.toLowerCase(c);
			}
			builder.append(c);
		}
		return builder.toString();
	}
	
	public static final String javaSimpleName(String fullyQualifiedClassName) {
		int dot = fullyQualifiedClassName.lastIndexOf('.');
		if (dot > 0) {
			return fullyQualifiedClassName.substring(dot+1);
		}
		return fullyQualifiedClassName;
	}
	
	
	public static final String rdfLocalName(String fullyQualifiedIRI) {
		int hash = fullyQualifiedIRI.lastIndexOf('#');
		if (hash >= 0) {
			return fullyQualifiedIRI.substring(hash+1);
		}
		int slash = fullyQualifiedIRI.lastIndexOf('/');
		if (slash >=0) {
			return fullyQualifiedIRI.substring(slash+1);
		}
		
		return null;
	}
}
