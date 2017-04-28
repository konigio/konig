package io.konig.core.util;

/*
 * #%L
 * Konig Core
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


import io.konig.core.util.ValueFormat.Element;

public class ValueExpander {
	
	public static String expand(String text, ValueMap map) {
		
		if (text.indexOf('{')>=0) {
			SimpleValueFormat format = new SimpleValueFormat(text);
			StringBuilder result = new StringBuilder();
			
			for (Element e : format.toList()) {
				String token = e.getText();
				switch(e.getType()) {
				case TEXT :
					result.append(token);
					break;
					
				case VARIABLE :
					String value = map.get(token);
					if (value == null) {
						result.append('{');
						result.append(token);
						result.append('}');
					} else if (value.indexOf('{') >= 0) {
						value = expand(value, map);
						result.append(value);
					} else {
						result.append(value);
					}
					break;
				}
			}
			
			text = result.toString();
		}
		
		
		
		return text;
	}


}
