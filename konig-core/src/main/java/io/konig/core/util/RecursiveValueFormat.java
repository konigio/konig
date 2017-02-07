package io.konig.core.util;

import java.util.ArrayList;

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
import java.util.List;
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
		
		Resolver resolver = new Resolver(formatMap);
		while (resolver.resolve()!=0);
		
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
			String result = format.format(map);
			return result;
		}
		
	}
	
	static class Resolver implements ValueMap {
		private Map<String, ValueFormat> formatMap;

		public Resolver(Map<String, ValueFormat> formatMap) {
			this.formatMap = formatMap;
		}
		
		
		
		int resolve() {
			int count = 0;
			List<Entry<String,ValueFormat>> list = new ArrayList<>(formatMap.entrySet());
			for (Entry<String,ValueFormat> e : list) {
				ValueFormat format = e.getValue();
				String expandedPattern = format.format(this);
				if (!expandedPattern.equals(format.getPattern())) {
					formatMap.put(e.getKey(), new SimpleValueFormat(expandedPattern));
					count++;
				}
			}
			return count;
		}

		@Override
		public String get(String name) {
			ValueFormat format = formatMap.get(name);
			if (format != null) {
				String pattern = format.getPattern();
				if (pattern.indexOf('{')==-1) {
					return pattern;
				}
			}
			return null;
		}

	}
}
