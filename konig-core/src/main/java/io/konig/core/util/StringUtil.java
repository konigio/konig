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
}
