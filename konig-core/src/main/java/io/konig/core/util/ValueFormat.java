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


import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class ValueFormat {

	private String source;
	private MessageFormat format;
	private List<String> argList;
	
	public ValueFormat(String source) {
		this.source = source;
		compile();
	}
	
	public String format(ValueMap map) {
		Object[] args = argList.toArray();
		
		for (int i=0; i<argList.size(); i++) {
			String name = argList.get(i);
			args[i] = map.get(name);
		}
		
		return format.format(args);
	}

	private void compile() {
		
		argList = new ArrayList<>();
		
		boolean insideVar = false;
		StringBuilder builder = new StringBuilder();
		
		StringBuilder varName = null;
		
		for (int i=0; i<source.length(); ) {
			int c = source.codePointAt(i);
			i += Character.charCount(c);
			
			
			if (insideVar) {
				if (c == '}') {
					String name = varName.toString();
					int index = argList.indexOf(name);
					if (index == -1) {
						builder.append(argList.size());
						argList.add(name);
						varName = null;
					} else {
						builder.append(index);
					}
					builder.append('}');
					insideVar = false;
				} else {
					varName.appendCodePoint(c);
				}
			} else {
				builder.appendCodePoint(c);
				if (c == '{') {
					varName = new StringBuilder();
					insideVar = true;
				}
			}
		}
		format = new MessageFormat(builder.toString());
	}
	
	public String toString() {
		return source;
	}
	
}
