package io.konig.pojo.io;

/*
 * #%L
 * Konig Schema Generator
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


import org.openrdf.model.URI;

import io.konig.core.util.StringUtil;

public class BeanUtil {
	
	public static String setterName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		String localName = predicate.getLocalName();
		if (localName.startsWith("is") && localName.length()>2 && Character.isUpperCase(localName.charAt(2))) {
			localName = localName.substring(2);
		}
		builder.append("set");
		builder.append(StringUtil.capitalize(localName));
		
		return builder.toString();
	}
	
	
	
	public static String adderName(URI predicate) {
		StringBuilder builder = new StringBuilder();
		builder.append("add");
		builder.append(StringUtil.capitalize(predicate.getLocalName()));
		
		return builder.toString();
	}

}
