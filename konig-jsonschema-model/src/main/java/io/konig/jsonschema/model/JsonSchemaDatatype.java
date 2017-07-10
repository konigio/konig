package io.konig.jsonschema.model;

/*
 * #%L
 * Konig JSON Schema model
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


public class JsonSchemaDatatype extends JsonSchema {
	
	public static final JsonSchemaDatatype BOOLEAN = new JsonSchemaDatatype("boolean");
	public static final JsonSchemaDatatype INT = new JsonSchemaDatatype("integer");
	public static final JsonSchemaDatatype LONG = new JsonSchemaDatatype("integer");
	public static final JsonSchemaDatatype FLOAT = new JsonSchemaDatatype("number");
	public static final JsonSchemaDatatype DOUBLE = new JsonSchemaDatatype("number");
	public static final JsonSchemaDatatype STRING = new JsonSchemaDatatype("string");
	public static final JsonSchemaDatatype DATE = new JsonSchemaDatatype("string", "date-time");
	public static final JsonSchemaDatatype TIME = new JsonSchemaDatatype("string", "date-time");
	public static final JsonSchemaDatatype TIMESTAMP = new JsonSchemaDatatype("string", "date-time");
	public static final JsonSchemaDatatype URI = new JsonSchemaDatatype("string", "uri");
	public static final JsonSchemaDatatype LANGSTRING = new JsonSchemaDatatype("object");
	
	static {
		PropertyMap properties = new PropertyMap();
		LANGSTRING.setProperties(properties);
		properties.put("@value", STRING);
		properties.put("@language", STRING);
		LANGSTRING.addRequired("@value");
		LANGSTRING.addRequired("@language");
	}
	
	protected JsonSchemaDatatype(String typeName) {
		setType(typeName);
	}
	
	
	protected JsonSchemaDatatype(String typeName, String logicalType) {
		setType(typeName);
		setFormat(logicalType);
	}
	
	public JsonSchemaDatatype(String typeName, String format, Boolean exclusiveMinimum, Boolean exclusiveMaximum,
			Number minimum, Number maximum) {
		setType(typeName);
		setFormat(format);
		setExclusiveMaximum(exclusiveMaximum);
		setExclusiveMinimum(exclusiveMinimum);
		setMinimum(minimum);
		setMaximum(maximum);
	}

}
