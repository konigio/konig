package io.konig.schemagen.jsonschema;

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


import org.openrdf.model.URI;

import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.ShapeIdGenerator;
import io.konig.core.util.ValueFormat;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class TemplateJsonSchemaNamer extends ShapeIdGenerator implements JsonSchemaNamer {
	
	public TemplateJsonSchemaNamer(NamespaceManager nsManager, ShapeManager shapeManager, ValueFormat template) {
		super(nsManager, shapeManager, template);
	}

	@Override
	public String schemaId(Shape shape) {
		if (shape.getId() instanceof URI) {
			return forShape((URI)shape.getId()).stringValue();
		}
		throw new KonigException("Shape must be identified by a URI");
	}

	@Override
	public String jsonSchemaFileName(Shape shape) {
		URI shapeId = (URI) shape.getId();
		StringBuilder builder = new StringBuilder();
		builder.append(namespace(shapeId).getPrefix());
		builder.append('.');
		
		return builder.toString();
	}

}
