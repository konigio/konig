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
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.Schema;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class JsonWriter {
	
	private JsonGenerator json;
	private ValueSelector valueSelector;
	private Graph graph;
	private OwlReasoner owlReasoner;
	private NamespaceManager nsManager;
	
	

	public JsonWriter(OwlReasoner reasoner, JsonGenerator json) {
		this.owlReasoner = reasoner;
		this.graph = owlReasoner.getGraph();
		this.nsManager = this.graph.getNamespaceManager();
		this.json = json;
	}

	public JsonWriter(JsonGenerator json, ValueSelector valueSelector) {
		this.json = json;
		this.valueSelector = valueSelector;
	}




	public ValueSelector getValueSelector() {
		return valueSelector;
	}

	public void setValueSelector(ValueSelector valueSelector) {
		this.valueSelector = valueSelector;
	}

	public void write(Shape shape, Vertex subject) throws IOException {
		
		Graph g = subject.getGraph();
		
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
			
			Value value  = null;
			if (set.size()==1) {
				value = set.iterator().next();
			} else if (valueSelector != null){
				value = valueSelector.select(subject, predicate, set);
				
			} else {
				StringBuilder msg = new StringBuilder();
				msg.append("Expected single value for ");
				msg.append(predicate.getLocalName());
				msg.append(" property of ");
				msg.append(subject.getId().stringValue());
				msg.append(" but found ");
				for (Value v : set) {
					msg.append(v.stringValue());
					msg.append(' ');
				}
				throw new KonigException(msg.toString());
			}
			
			writeValue(subject, p, value);
		}
		
	}

	private void writeValue(Vertex subject, PropertyConstraint p, Value value) throws IOException {
		
		if (value == null) {
			json.writeNull();
		} else if (value instanceof Literal) {
			Literal literal = (Literal) value;
			String text = literal.stringValue();
			URI datatype = literal.getDatatype();
			if (datatype != null) {
				
				if (owlReasoner.isBooleanType(datatype)) {
					json.writeBoolean("true".equalsIgnoreCase(text));
				} else if (owlReasoner.isIntegerDatatype(datatype)) {
					json.writeNumber(Long.parseLong(text));
				} else if (owlReasoner.isRealNumber(datatype)) {
					json.writeNumber(Double.parseDouble(text));
				} else {
					json.writeString(text);
				}
				
			} else {
				json.writeString(text);
			}
		} else if (value instanceof Resource) {
			Resource id = (Resource) value;
			Vertex object = subject.getGraph().getVertex(id);
			if (object == null) {
				throw new KonigException("Resource not found: " + id);
			}
			
			if (p.getShapeId() != null) {
				Shape shape = p.getShape();
				if (shape == null) {
					throw new KonigException("Shape not found: " + p.getShapeId());
				}
				
				write(shape, object);
			} else if (nsManager!=null && id instanceof URI && owlReasoner.instanceOf(id, Schema.Enumeration)) {
				URI uri = (URI) id;
				String namespace = uri.getNamespace();
				Namespace ns = nsManager.findByName(namespace);
				if (ns == null) {
					json.writeString(id.stringValue());
				} else {
					json.writeString(uri.getLocalName());
				}
			} else {
				json.writeString(id.stringValue());
			}
		}
		
	}

	private void writeId(Shape shape, Vertex subject) throws IOException {
		
		NodeKind nodeKind = shape.getNodeKind();
		if (nodeKind == NodeKind.IRI || nodeKind==NodeKind.BlankNodeOrIRI) {
			Resource id = subject.getId();
			
			
			
			if (id instanceof URI) {

				URI uri = (URI) id;
				String idValue = null;
				
				URI idFormat = shape.getIdFormat();
				if (Konig.Curie.equals(idFormat)) {
					idValue = RdfUtil.curie(uri, nsManager);
				} else if (Konig.LocalName.equals(idFormat)) {
					idValue = uri.getLocalName();
				} else {
					idValue = uri.stringValue();
				}
				
				json.writeStringField("id", idValue);
			} else if (nodeKind==NodeKind.IRI) {
				throw new KonigException("Invalid shape: expecting IRI id");
			}
		}
		
	}

}
