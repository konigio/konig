package io.konig.core.binary;

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


public class TermPart {
	
	private String value;
	private int start;
	private int end;
	
	public TermPart(String value) {
		this.value = value;
		start = 0;
		next();
		while (end < value.length()) {
			char c = value.charAt(end);
			if (c != '/') {
				break;
			}
			end++;
		}
	}
	
	public String toString() {
		return value.substring(start, end);
	}
	
	public boolean more() {
		return end < value.length();
	}
	
	public boolean matches(String text) {
		if (text.length() != (end-start)) {
			return false;
		}
		int j = start;
		for (int i=0; i<text.length(); i++) {
			char a = value.charAt(j++);
			char b = text.charAt(i);
			if (a!=b) {
				return false;
			}
		}
		return true;
	}
	
	public boolean next() {
		start = end;
		
		while (end < value.length()) {
			int c = value.charAt(end);
			end++;
			
			if (c=='/' || c==':' || c=='#' || c=='?' || c=='&') {
				break;
			}
		}
		
		return start < value.length();
	}
	
	

}
