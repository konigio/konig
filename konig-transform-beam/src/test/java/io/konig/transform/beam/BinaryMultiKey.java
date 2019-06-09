package io.konig.transform.beam;

/*
 * #%L
 * Konig Transform Beam
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import com.google.api.services.bigquery.model.TableRow;

@SuppressWarnings("serial")
public class BinaryMultiKey implements Serializable {

	private byte[][] keyList;
	private int hashCode;
	
	public BinaryMultiKey(List<Object> list) throws IOException {
		
		keyList = new byte[list.size()][];
		
		for (int i=0; i<list.size(); i++) {
			Object key = list.get(i);
			keyList[i] = toByteArray(key);
		}
		
		computeHashCode();
		
	}
	
	private void computeHashCode() {
		
		int[] array = new int[keyList.length];
		for (int i=0; i<keyList.length; i++) {
			byte[] key = keyList[i];
			array[i] = Arrays.hashCode(key);
		}
		
		hashCode = Arrays.hashCode(array);
		
	}

	public int hashCode() {
		return hashCode;
	}
	

	private byte[] toByteArray(Object value) throws IOException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try (
			DataOutputStream out = new DataOutputStream(buffer)
		) {
			writeValue(out, value);
		}
		return buffer.toByteArray();
	}

	private void writeTableRow(DataOutputStream out, TableRow row) throws IOException {

		if (!row.isEmpty()) {
			out.writeChar('{');
			// Sort the fields alphabetically
			List<Entry<String,Object>> entryList = new ArrayList<>(row.entrySet());
			Collections.sort(entryList, new Comparator<Entry<String,Object>>() {
	
				@Override
				public int compare(Entry<String, Object> a, Entry<String, Object> b) {
					return a.getKey().compareTo(b.getKey());
				}
				
			});
			
			for (Entry<String,Object> entry : entryList) {
				String fieldName = entry.getKey();
				Object value = entry.getValue();
				if (value != null) {
					out.writeUTF(fieldName);
					out.writeChar(':');
					writeValue(out, value);
				}
			}
			
			out.writeChar('}');
		}
		
	}

	private void writeValue(DataOutputStream out, Object value) throws IOException {
		
		if (value == null) {
			throw new IllegalArgumentException("'value' must not be null");
		}
		
		if (value instanceof TableRow) {
			writeTableRow(out, (TableRow) value);
		} else if (value instanceof String) {
			out.writeUTF((String) value);
		} else if (value instanceof Long) {
			out.writeLong((Long) value);
		} else if (value instanceof Boolean) {
			out.writeBoolean((Boolean) value);
		}  else {
			throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getSimpleName());
		}
		
	}
	

}
