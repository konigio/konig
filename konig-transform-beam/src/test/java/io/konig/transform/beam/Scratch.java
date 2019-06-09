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


import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.join.CoGbkResult;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.TupleTag;

import com.google.api.client.util.DateTime;
import com.google.api.services.bigquery.model.TableRow;

public class Scratch {

	public class ReadPersonFn extends DoFn<TableRow, KV<String, TableRow>> {
		@ProcessElement
		public void processElement(ProcessContext c) {
			try {
				TableRow inputRow = c.element();

				String key = getKey(inputRow);
				c.output(KV.of(key, inputRow));

			} catch (Throwable oops) {
				oops.printStackTrace();
			}

		}


		private String getKey(TableRow inputRow) {
			return (String) inputRow.get("id");
		}
	}
	
	
}

class MainClass {
	static final TupleTag<TableRow> personTargetTag = new TupleTag<>();
	static final TupleTag<TableRow> personSourceTag = new TupleTag<>();
}

class MergeFn extends DoFn<KV<String, CoGbkResult>, TableRow> {
	
	
	@ProcessElement 
	public void processElement(ProcessContext c) {
		try {
			KV<String, CoGbkResult> e = c.element();
			
			TableRow outputRow = baselineRow(e, MainClass.personTargetTag);
			overlay(outputRow, e, MainClass.personSourceTag);
			
			
			if (!outputRow.isEmpty()) {
				c.output(outputRow);
			}
		} catch (Throwable oops) {
			
		}
	}

	private void overlay(TableRow outputRow, KV<String, CoGbkResult> e, TupleTag<TableRow> tupleTag) {
		

		Iterator<TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
		
		while (sequence.hasNext()) {
			TableRow sourceRow = sequence.next();
			TableRow targetRow = transform(sourceRow);
			if (targetRow != null) {
				copy(targetRow, outputRow);
			}
		}
		
		
	}

	private void copy(TableRow targetRow, TableRow outputRow) {
		
		for (Entry<String, Object> entry : targetRow.entrySet()) {
			String fieldName = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof TableRow) {
				Object outputValue = outputRow.get(fieldName);
				if (outputValue instanceof TableRow) {
					copy((TableRow)value, (TableRow)outputValue);
				} else {
					outputRow.put(fieldName, value);
				}
				
			} else {
				outputRow.put(fieldName, value);
			}
		}
		
	}

	private TableRow transform(TableRow sourceRow) {
		// TODO Auto-generated method stub
		return null;
	}

	private TableRow baselineRow(KV<String, CoGbkResult> e, TupleTag<TableRow> tupleTag) {
		
		Iterator<TableRow> sequence = e.getValue().getAll(tupleTag).iterator();
		TableRow result = null;
		Long latest = null;
		
		
		while (sequence.hasNext()) {
			TableRow row = sequence.next();
			Long modified = dateTime(row, "modified");
			if (modified!=null && (latest==null || modified > latest)) {
				latest = modified;
				result = row;
			}
		}
		
		if (result == null) {
			result = new TableRow();
		}
		
		return result;
	}

	private Long dateTime(TableRow row, String fieldName) {
		Object value = row.get(fieldName);
		if (value instanceof String) {
			return new DateTime((String) value).getValue();
		}
		return null;
	}

}
