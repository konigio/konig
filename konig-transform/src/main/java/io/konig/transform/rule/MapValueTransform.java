package io.konig.transform.rule;

/*
 * #%L
 * Konig Transform
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

import org.openrdf.model.Value;

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class MapValueTransform extends AbstractPrettyPrintable implements ValueTransform {
	
	private Map<Value,Value> valueMap = new HashMap<>();

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.beginArray("valueMap");
		for (Entry<Value,Value> e : valueMap.entrySet()) {
			out.beginObject(e);
			out.field("key", e.getKey());
			out.field("value", e.getValue());
			out.endObject();
		}
		
		out.endArray("valueMap");
		out.endObject();

	}
	
	public void put(Value key, Value value) {
		valueMap.put(key, value);
	}

	public Map<Value, Value> getValueMap() {
		return valueMap;
	}

}
