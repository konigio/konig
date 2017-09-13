package io.konig.dao.core;

/*
 * #%L
 * Konig DAO Core
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


import java.util.ArrayList;
import java.util.StringTokenizer;

import io.konig.sql.runtime.EntityStructure;
import io.konig.sql.runtime.FieldInfo;
import io.konig.sql.runtime.Stereotype;

public class FieldPath extends ArrayList<FieldInfo> {
	private static final long serialVersionUID = 1L;
	
	private String stringValue;

	public FieldPath(String stringValue) {
		this.stringValue = stringValue;
	}

	public String stringValue() {
		if (stringValue == null) {
			StringBuilder builder = new StringBuilder();
			boolean first = true;
			for (FieldInfo field : this) {
				if (first) {
					first = false;
				} else {
					builder.append('.');
				}
				builder.append(field.getName());
			}
			stringValue = builder.toString();
		}
		return stringValue;
	}
	
	public String toString() {
		return stringValue();
	}
	
	public FieldInfo pop() {
		return isEmpty() ? null : remove(size()-1);
	}
	
	public FieldInfo lastField() {
		return isEmpty() ? null : get(size()-1);
	}
	
	public static FieldPath createFieldPath(String path, EntityStructure struct) throws DaoException {

		FieldPath result = new FieldPath(path);
		StringTokenizer tokenizer = new StringTokenizer(path, ".");
		while (tokenizer.hasMoreTokens()) {
			String fieldName = tokenizer.nextToken();
			FieldInfo field = struct==null ? null : struct.findFieldByName(fieldName);
			if (field == null) {
				throw new DaoException("Field not found: " + path);
			}
			result.add(field);
			struct = field.getStruct();
		}
		return result;
	}
	
	/**
	 * Build a path to the first field in the given structure that has the MEASURE stereotype.
	 */
	public static FieldPath measurePath(EntityStructure struct ) {
		FieldPath result = new FieldPath(null);
		return measurePath(result, struct) ? result : null;
	}
		
	
	private static boolean measurePath(FieldPath result, EntityStructure struct) {
		
		for (FieldInfo field : struct.getFields()) {
			if (Stereotype.MEASURE.equals(field.getStereotype())) {
				result.add(field);
				return true;
			}
		}
		for (FieldInfo field : struct.getFields()) {
			EntityStructure child = field.getStruct();
			if (child != null) {
				result.add(field);
				if (measurePath(result, child)) {
					return true;
				}
				result.pop();
			}
		}
		
		return false;
	}
}
