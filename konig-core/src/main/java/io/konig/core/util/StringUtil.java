package io.konig.core.util;

import java.util.HashMap;
import java.util.Map;

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
	
	private static final int WORD_BOUNDARY = 3;
	
	
	private static final String DELIMITER = "_-./:";
	
	/**
	 * Converts a technicalName in pascalCase or snake_case to a human-friendly name with spaces between
	 * the words, with each word capitalized.
	 * @return
	 */
	public static final String label(String technicalName) {
		technicalName = technicalName.trim();
		if (technicalName.indexOf(' ') > 0) {
			return technicalName;
		}
		int prior = 0;
		
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<technicalName.length();) {
			int c = technicalName.codePointAt(i);
			i += Character.charCount(c);
			
			if (!Character.isAlphabetic(c)) {
				if (c=='_' || c=='.') {
					c = ' ';
				}
				
				builder.appendCodePoint(c);
				prior = c;
				continue;
				
			} else {
				
				if (prior == ' ' || builder.length()==0) {
					c = Character.toUpperCase(c);
					builder.appendCodePoint(c);
					prior = c;
					continue;
				}

				int priorCase = caseValue(prior);
				int thisCase = caseValue(c);
				
				
				if (priorCase != thisCase) {
					
					if (Character.isAlphabetic(prior) && priorCase==LOWER) {
						builder.append(' ');
					}
					

					prior = c;
					builder.appendCodePoint(c);
					continue;
				}
				
				builder.appendCodePoint(c);
				prior = c;
			}
			
			
		}
		return builder.toString();
	}
	
	public static final String normalizedLocalName(String label) {
		StringBuilder builder = new StringBuilder();
		
		
		label = label.trim();
		
		int prior = 0;
		for (int i=0; i<label.length();) {
			int c = label.codePointAt(i);

			i += Character.charCount(c);
			if (Character.isWhitespace(c)) {
				c = '_';
			}
			if (!Character.isAlphabetic(c) && !Character.isDigit(c) && c!='-' && c!='_') {
				if (prior != '_') {
					builder.append('_');
				}
				builder.append('x');
				builder.append(Integer.toHexString(c));
				builder.append('_');
				prior = '_';
			} else {	
				builder.appendCodePoint(c);
				prior = c;
			}
		}
		if (builder.charAt(builder.length()-1) == '_') {
			builder.setLength(builder.length()-1);
		}
		
		return builder.toString();
	}
	
	public static final String LABEL_TO_SNAKE_CASE(String label) {
		StringBuilder builder = new StringBuilder();
		
		
		label = label.trim();
		
		int priorCase = OTHER;
		int prior = '_';
		
		int k = label.codePointAt(0);
		if (!Character.isAlphabetic(k) && !Character.isDigit(k)) {
			builder.append('_');
		}
		
		
		for (int i=0; i<label.length();) {
			int c = label.codePointAt(i);
			char ch = (char) c;
			i += Character.charCount(c);
			
			if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
				
				if (c=='-' || Character.isWhitespace(c) || c=='_') {
					builder.append('_');
					prior = '_';
					priorCase = OTHER;
					continue;
				} 
				
				if (prior != '_') {
					builder.append('_');
				}
				builder.append('x');
				builder.append(Integer.toHexString(c));
				builder.append('_');
				prior = '_';
				priorCase = OTHER;
				
				continue;
			}
			int caseValue = caseValue(c);
			
			if (i>=2 && caseValue!=OTHER && prior!='_' && caseValue!=priorCase && c!='_' 
					&& builder.length()>=2 && builder.charAt(builder.length()-2) != '_') {
				builder.append('_');
			}
			prior = c;
			priorCase = caseValue;
			
			builder.appendCodePoint(Character.toUpperCase(c));
		}
		
		while (builder.length()>0 && builder.charAt(builder.length()-1) == '_') {
			builder.setLength(builder.length()-1);
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
	
	public static String mediaTypePart(String source) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<source.length();) {
			int c = source.codePointAt(i);
			if (!Character.isAlphabetic(c) && !Character.isDigit(c)) {
				c = '-';
			}
			builder.appendCodePoint(c);
			i += Character.charCount(c);
		}
		return builder.toString();
	}
}
