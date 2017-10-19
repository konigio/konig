package io.konig.schemagen.jsonschema.impl;

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

import io.konig.schemagen.jsonschema.JsonSchemaNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeMediaTypeNamer;

public class SimpleJsonSchemaNamer implements JsonSchemaNamer {
	
	private String idSuffix;
	private ShapeMediaTypeNamer mediaTypeNamer;
	


	public SimpleJsonSchemaNamer(String idSuffix, ShapeMediaTypeNamer mediaTypeNamer) {
		this.idSuffix = idSuffix;
		this.mediaTypeNamer = mediaTypeNamer;
	}



	@Override
	public String schemaId(Shape shape) {
		String shapeId = shape.getId().stringValue();
		return shapeId + idSuffix;
	}



	@Override
	public String jsonSchemaFileName(Shape shape) {
		String mediaTypeName = mediaTypeNamer.baseMediaTypeName(shape);
		int slash = mediaTypeName.indexOf('/');
		return mediaTypeName.substring(slash+1);
	}

}
