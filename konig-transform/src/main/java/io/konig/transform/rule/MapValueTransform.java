package io.konig.transform.rule;

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
