package io.konig.shacl.jsonld.impl;

/*
 * #%L
 * konig-shacl
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

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Term;
import io.konig.core.Vertex;
import io.konig.core.io.impl.JsonUtil;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.jsonld.JsonldShapeWriter;

public class JsonldShapeWriterImpl implements JsonldShapeWriter {

	@Override
	public void toJson(Vertex subject, Shape shape, Context context, ContextOption contextOption, JsonGenerator json) throws IOException {
		
		if (context == null) {
			context = shape.getJsonldContext();
		}
		
		Graph graph = subject.getGraph();
		Context inverse = (context==null) ? null : context.inverse();
		
		json.writeStartObject();
		
		if (contextOption == ContextOption.URI && context!=null) {
			String contextId = context.getContextIRI();
			if (contextId != null) {
				json.writeStringField("@context", contextId);
			}
		}
		String valueKey = "@value";
		String typeKey = "@type";
		String languageKey = "@language";
		if (context != null) {
			valueKey = context.alias(valueKey);
			typeKey = context.alias(typeKey);
			languageKey = context.alias(languageKey);
		}
		
		Resource id = subject.getId();
		String idValue = null;
		if (id instanceof URI) {
			URI uri = (URI) id;
			idValue = uri.stringValue();
			if (inverse != null) {
				Term term = inverse.getTerm(uri.getNamespace());
				if (term != null) {
					idValue = term.getId() + ":" + uri.getLocalName();
				}
			}
		}
		if (idValue != null) {
			String idKey = "@id";
			if (context != null) {
				idKey = context.alias(idKey);
			}
			json.writeStringField(idKey, idValue);
		}
		
		List<PropertyConstraint> list = shape.getProperty();
		for (PropertyConstraint p : list) {
			Term term = p.getTerm();
			URI predicate = p.getPredicate();
			Shape valueShape = p.getValueShape();
			
			if (term == null && context!=null) {
				term = context.inverse().getTerm(predicate.stringValue());
			}
			
			
			
			Set<Edge> set = subject.outProperty(predicate);
			if (set.isEmpty()) {
				continue;
			}

			String key = (term==null) ? predicate.stringValue() : term.getKey();
			
			json.writeFieldName(key);

			boolean isArray = set.size()>1 || (term!=null && term.getContainer()!=null);
			
			if (isArray) {
				json.writeStartArray();
			}
			
			for (Edge edge : set) {
				Value object = edge.getObject();
				if (valueShape != null && (object instanceof Resource)) {
					Resource resource = (Resource) object;
					Vertex vertex = graph.vertex(resource);
					
					toJson(vertex, valueShape, context, ContextOption.IGNORE, json);
				} else if (object instanceof URI) {
					URI uri = (URI) object;
					idValue = uri.stringValue();
					if (inverse != null) {
						String namespace = uri.getNamespace();
						Term nsTerm = inverse.getTerm(namespace);
						if (nsTerm != null) {
							idValue = nsTerm.getKey() + ":" + uri.getLocalName();
						}
					}
					json.writeString(idValue);
					
				} else if (object instanceof BNode) {
					throw new RuntimeException("BNode without a valueShape is not supported");
				} else if (object instanceof Literal) {
					Literal literal = (Literal) object;
					URI datatype = literal.getDatatype();
					String language = literal.getLanguage();
					if (datatype != null) {
						
						if (term!=null && term.getType()!=null) {
							
							Object value = JsonUtil.toObject(literal);
							
							if (value instanceof Long) {
								json.writeNumber((Long) value);
							} else if (value instanceof Double) {
								json.writeNumber((Double) value);
							} else if (value instanceof Boolean) {
								json.writeBoolean((Boolean)value);
							} else {
								json.writeString(literal.stringValue());
							}
							continue;
							
						}
						
						json.writeStartObject();
						json.writeStringField(valueKey, literal.stringValue());
						json.writeStringField(typeKey, compactIRI(datatype, context));
						json.writeEndObject();
						
					}  else if (language != null) {
						json.writeStartObject();
						json.writeStringField(valueKey, literal.stringValue());
						json.writeStringField(languageKey, language);
						json.writeEndObject();
					} else {
						if (term == null || term.getType()==null) {

							json.writeStartObject();
							json.writeStringField(valueKey, literal.stringValue());
							json.writeEndObject();
							
						} else {
							json.writeString(literal.stringValue());
						}
					}
				}
			}
			
			if (isArray) {
				json.writeEndArray();
			}
		}
		
		json.writeEndObject();
		
	}
	
	String compactIRI(URI iri, Context context) {
		Context inverse = context.inverse();
		Term term = inverse.getTerm(iri.getNamespace());
		return term==null ? iri.stringValue() : term.getKey() + ":" + iri.getLocalName();
	}

	@Override
	public void toJson(Vertex subject, Shape shape, JsonGenerator json) throws IOException {
		
		toJson(subject, shape, shape.getJsonldContext(), ContextOption.URI, json);
		
	}



}
