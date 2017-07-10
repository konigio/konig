package io.konig.schemagen.avro;

/*
 * #%L
 * Konig Schema Generator
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


public class AvroDatatype {
	
	public static final AvroDatatype BOOLEAN = new AvroDatatype("boolean");
	public static final AvroDatatype INT = new AvroDatatype("int");
	public static final AvroDatatype LONG = new AvroDatatype("long");
	public static final AvroDatatype FLOAT = new AvroDatatype("float");
	public static final AvroDatatype DOUBLE = new AvroDatatype("double");
	public static final AvroDatatype STRING = new AvroDatatype("string");
	public static final AvroDatatype DATE = new AvroDatatype("int", "date");
	public static final AvroDatatype TIME = new AvroDatatype("int", "time-millis");
	public static final AvroDatatype TIMESTAMP = new AvroDatatype("long", "timestamp-millis");
	public static final AvroDatatype DURATION = new FixedAvroDatatype("duration", 12);
	public static final AvroDatatype BYTE = new FixedAvroDatatype(1);
	
	private String typeName;
	private String logicalType;
	
	protected AvroDatatype(String typeName, String logicalType) {
		this.typeName = typeName;
		this.logicalType = logicalType;
	}

	protected AvroDatatype(String typeName) {
		this.typeName = typeName;
	}

	public String getTypeName() {
		return typeName;
	}

	public String getLogicalType() {
		return logicalType;
	}
	
	

}
