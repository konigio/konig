package io.konig.core.io.impl;

/*
 * #%L
 * konig-core
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
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Term;
import io.konig.core.Vertex;
import io.konig.core.io.ContextWriter;
import io.konig.core.io.JsonldGraphWriter;
import io.konig.core.io.KonigWriteException;

public class JsonldGraphWriterImpl implements JsonldGraphWriter {

	@Override
	public void write(Graph graph, Context context, JsonGenerator json) throws KonigWriteException, IOException {
		Worker worker = new Worker(graph, context, json);
		worker.run();

	}
	
	private static class Worker {
		private Graph graph;
		private Context context;
		private Context inverse;
		private JsonGenerator json;
		private String idKey;
		private String valueKey;
		private String typeKey;
		private String languageKey;
		
		public Worker(Graph graph, Context context, JsonGenerator json) {
			this.graph = graph;
			this.context = context;
			this.json = json;
			this.inverse = context.inverse();
			
			idKey = context.alias("@id");
			valueKey = context.alias("@value");
			typeKey = context.alias("@type");
			languageKey = context.alias("@language");
			
		}
		
		void run() throws IOException {
			
			json.writeStartObject();
			
			String contextId = context.getContextIRI();
			if (contextId == null) {
				List<Term> list = context.asList();
				if (list.size()>0) {
					ContextWriter contextWriter = new ContextWriter();
					json.writeFieldName("@context");
					contextWriter.writeContext(context, json);
				}
			}
			
			String graphKey = context.alias("@graph");
			json.writeFieldName(graphKey);
			json.writeStartArray();
			for (Vertex vertex : graph.vertices()) {
				writeVertex(vertex);
			}
			json.writeEndArray();
			
			json.writeEndObject();
		}

		private void writeVertex(Vertex vertex) throws IOException {
			
			Resource subject = vertex.getId();
			
			String idValue = idValue(subject);
			
			json.writeStartObject();

			json.writeStringField(idKey, idValue);
			Set<Edge> typeSet = vertex.outProperty(RDF.TYPE);
			writeType(typeSet);
			
			Set<Entry<URI,Set<Edge>>> set = vertex.outEdges();
			
			for (Entry<URI,Set<Edge>> property : set) {
				URI predicate = property.getKey();
				if (predicate.equals(RDF.TYPE)) {
					continue;
				}
				Set<Edge> edgeSet = property.getValue();
				
				writeProperty(predicate, edgeSet);
			}
			
			json.writeEndObject();
			
			
			
		}
		
		private void writeType(Set<Edge> typeSet) throws IOException {
			
			if (!typeSet.isEmpty()) {
				json.writeFieldName(typeKey);
				
				boolean isArray = typeSet.size()>1;
				
				if (isArray) {
					json.writeStartArray();
				}
				for (Edge edge : typeSet) {
					String typeValue = typeValue(edge.getObject());
					json.writeString(typeValue);
				}
				if (isArray) {
					json.writeEndArray();
				}
			}
			
		}
		
		private String typeValue(Value object) {
			
			if (object instanceof URI) {
				return typeValue((URI)object);
			}
			if (object instanceof BNode) {
				return "_:" + object.stringValue();
			}
			throw new KonigWriteException("Invalid type: " + object.stringValue());
		}

		private String typeValue(URI uri) {
			Term term = inverse.getTerm(uri.stringValue());
			if (term != null) {
				return term.getKey();
			}
			term = inverse.getTerm(uri.getNamespace());
			if (term != null) {
				return term.getKey() + ":" + uri.getLocalName();
			}
			return uri.stringValue();
		}

		private String idValue(Resource subject) {

			return (subject instanceof URI) ? 
				compactIRI((URI)subject) :	"_:" + subject.stringValue();
		}

		private void writeProperty(URI predicate, Set<Edge> edgeSet) throws IOException {
			
			if (edgeSet.isEmpty()) {
				return;
			}
			
			Term term = inverse.getTerm(predicate.stringValue());
			
			String fieldName = termName(predicate, term);
			json.writeFieldName(fieldName);
			
			boolean isArray = edgeSet.size()>1 || (term!=null && term.getContainer()!=null);
			

			if (isArray) {
				json.writeStartArray();
			}
			
			for (Edge edge : edgeSet) {
				Value object = edge.getObject();
				if (object instanceof Resource) {
					String idValue = idValue((Resource)object);
					if (term!=null && "@id".equals(term.getType())) {
						json.writeString(idValue);
					} else {
						json.writeStartObject();
						json.writeStringField(idKey, idValue);
						json.writeEndObject();
					}
				} else {
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
						if (!XMLSchema.STRING.equals(datatype)) {
							json.writeStringField(typeKey, compactIRI(datatype));
						}
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

		String compactIRI(URI iri) {
			Term term = inverse.getTerm(iri.getNamespace());
			return term==null ? iri.stringValue() : term.getKey() + ":" + iri.getLocalName();
		}
		
		
		
		private String termName(URI predicate, Term term) {
			
			if (term != null) {
				return term.getKey();
			}
			term = inverse.getTerm(predicate.getNamespace());
			if (term != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(term.getKey());
				builder.append(':');
				builder.append(predicate.getLocalName());
				return builder.toString();
			}
			
			return predicate.stringValue();
		}
		
		
		
		
	}

}
