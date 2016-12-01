package io.konig.shacl.io.json;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JsonWriter {
	
	private JsonGenerator json;
	
	
	

	public JsonWriter(JsonGenerator json) {
		this.json = json;
	}

	public void write(Shape shape, Vertex subject) throws IOException {
		json.writeStartObject();
		
		writeId(shape, subject);
		List<PropertyConstraint> list = shape.getProperty();
		for (PropertyConstraint p : list) {
			writeProperty(subject, p);
		}
		
		json.writeEndObject();
	}

	private void writeProperty(Vertex subject, PropertyConstraint p) throws IOException {
		
		Integer maxCount = p.getMaxCount();
		
		Set<Value> set = subject.getValueSet(p.getPredicate());


		boolean isMultiValue = maxCount==null || maxCount>1;
		URI predicate = p.getPredicate();
		
		json.writeFieldName(predicate.getLocalName());
		
		if (isMultiValue) {
			json.writeStartArray();
			for (Value v : set) {
				writeValue(subject, p, v);
			}
			json.writeEndArray();
		} else if (set.isEmpty()) {
			json.writeNull();
		} else {
			writeValue(subject, p, set.iterator().next());
		}
		
	}

	private void writeValue(Vertex subject, PropertyConstraint p, Value value) throws IOException {
		
		if (value instanceof Literal) {
			Literal literal = (Literal) value;
			// TODO: Provide special handling for different data types.
			json.writeString(literal.stringValue());
		} else {
			throw new KonigException("Values other than Literal are not yet supported");
		}
		
	}

	private void writeId(Shape shape, Vertex subject) throws IOException {
		
		if (shape.getNodeKind() == NodeKind.IRI) {
			Resource id = subject.getId();
			if (!(id instanceof URI)) {
				throw new KonigException("Invalid shape: expecting IRI id");
			} else {
				URI uri = (URI) id;
				boolean isEnum = subject.hasProperty(RDF.TYPE, Schema.Enumeration);
				String idValue = isEnum ? uri.getLocalName() : uri.stringValue();
				
				json.writeStringField("id", idValue);
			}
		}
		
	}

}
