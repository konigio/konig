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


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class RecursiveValueFormat extends SimpleValueFormat {
	
	private Map<String, ValueFormat> formatMap;
	
	public RecursiveValueFormat() {
		this(new HashMap<String,ValueFormat>());
	}
	public RecursiveValueFormat(Map<String,ValueFormat> map) {
		super(null);
		this.formatMap = map;
	}
	
	public void compile(String text) {
		this.text = text.trim();
		compile();
	}
	
	public void put(String name, ValueFormat format) {
		formatMap.put(name, format);
	}
	
	public void put(String name, String formatText) {
		put(name, new SimpleValueFormat(formatText));
	}
	
	public void put(Properties properties) {
		Set<Entry<Object,Object>> entries = properties.entrySet();
		for (Entry<Object,Object> e : entries) {
			String key = e.getKey().toString();
			String value = e.getValue().toString();
			put(key, value);
		}
	}

	@Override
	protected Element createVariable(String text) {
		
		ValueFormat format = formatMap.get(text);
		
		return format==null ? new Variable(text) : new RecursiveVariable(text, format);
	}
	
	static class RecursiveVariable extends Variable {
		
		private ValueFormat format;

		public RecursiveVariable(String text, ValueFormat format) {
			super(text);
			this.format = format;
		}

		String get(ValueMap map) {
			return format.format(map);
		}
		
	}
}
