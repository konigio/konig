package io.konig.schemagen;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.vocab.KOL;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.VS;

public class AllJsonldWriter {
	private static final Logger logger = LoggerFactory.getLogger(AllJsonldWriter.class);
	
	private static final Set<URI> ONTOLOGY_SKIP = new HashSet<>();
	static {
		ONTOLOGY_SKIP.add(new URIImpl(OWL.NAMESPACE));
		ONTOLOGY_SKIP.add(new URIImpl(RDF.NAMESPACE));
		ONTOLOGY_SKIP.add(new URIImpl(RDFS.NAMESPACE));
		ONTOLOGY_SKIP.add(new URIImpl(XMLSchema.NAMESPACE));
		ONTOLOGY_SKIP.add(new URIImpl(VS.NAMESPACE));
		ONTOLOGY_SKIP.add(new URIImpl(KOL.NAMESPACE));
		ONTOLOGY_SKIP.add(SH.NAMESPACE_URI);
		
	}
	
	private static class Worker {
		private Set<String> memory = new HashSet<>();
		private List<Vertex> stack = new ArrayList<>();
		private NamespaceManager nsManager;
		private Graph graph;
		private JsonGenerator json;
		
		private void push(Vertex v) {
			
			Resource id = v.getId();
			if (id instanceof URI) {
				URI uri = (URI) id;
				if (OWL.NAMESPACE.equals(uri.getNamespace()) ||
					SH.NAMESPACE_URI.stringValue().equals(uri.getNamespace())) {
					return;
				}
			}
			String key = v.getId().stringValue();
			if (!memory.contains(key)) {
				stack.add(v);
			}
		}

		public void writeJSON(NamespaceManager nsManager, Graph graph, JsonGenerator json) throws IOException {
			this.nsManager = nsManager;
			this.graph = graph;
			this.json = json;
			json.writeStartObject();
			
			addContext();
			json.writeArrayFieldStart("@graph");
			
			writeInstances(OWL.ONTOLOGY, ONTOLOGY_SKIP);
			writeInstances(OWL.CLASS);
			writeInstances(RDF.PROPERTY);
			writeInstances(OWL.OBJECTPROPERTY);
			writeInstances(OWL.DATATYPEPROPERTY);
			writeInstances(OWL.FUNCTIONALPROPERTY);
			writeInstances(SH.Shape);
			
			for (int i=0; i<stack.size(); i++) {
				Vertex v = stack.get(i);
				writeVertex(v);
			}
			
			json.writeEndArray();
			
			json.writeEndObject();
			
			json.flush();
			
		}
		
		private void writeInstances(URI type) throws IOException {
			writeInstances(type, null);
		}

		
		private void writeInstances(URI type, Set<URI> skip) throws IOException {
			List<Vertex> list = graph.v(type).in(RDF.TYPE).toVertexList();
			Collections.sort(list, new LocalnameComparator());
			writeVertices(list, skip);

		}
		
		private void writeVertices(List<Vertex> list, Set<URI> skip) throws IOException {
			for (Vertex v : list) {
				if (skip==null || !skip.contains(v.getId())) {
					writeVertex(v);
				}
			}
		}

		private void writeVertex(Vertex v) throws IOException {

			Set<Entry<URI, Set<Edge>>> set = v.outEdges();
			if (set.isEmpty()) {
				return;
			}
			
			String idValue = v.getId().stringValue();
			if (memory.contains(idValue)) {
				return;
			}
			memory.add(idValue);
			
			json.writeStartObject();
			writeVertexProperties(set, v);
			
			json.writeEndObject();
			
		}
		
		private void writeVertexProperties(Set<Entry<URI, Set<Edge>>> set, Vertex v) throws IOException {

			
			String idValue = idValue(v);
			if (idValue != null) {
				json.writeStringField("@id", idValue(v.getId()));
			}
			
			for (Entry<URI, Set<Edge>> e : set) {
				Set<Edge> edgeSet = e.getValue();
				
				if (!edgeSet.isEmpty()) {
					URI predicate = e.getKey();
					
					boolean isType = predicate.equals(RDF.TYPE);
					
					
					String fieldName = isType ? "@type" : curie(predicate);
					
					json.writeFieldName(fieldName);
					
					if (edgeSet.size() > 1) {
						json.writeStartArray();
						for (Edge edge : edgeSet) {
							writeValue(isType, edge.getObject());
						}
						json.writeEndArray();
					} else {
						Value value = edgeSet.iterator().next().getObject();
						writeValue(isType, value);
					}
					
				} 
			}
			
		}

		private void writeValue(boolean isType, Value object) throws IOException {
			
			if (isType) {
				if (object instanceof Resource) {
					json.writeString(idValue((Resource)object));
				} else {
					throw new SchemaGeneratorException("Illegal type value: " + object.stringValue());
				}
			} else {
				writeValue(object);
			}
			
		}

		private String idValue(Vertex v) {
			Resource id = v.getId();
			
			if (id instanceof BNode) {
				int count = 0;
				Set<Entry<URI, Set<Edge>>> set = v.inEdges();
				for (Entry<URI, Set<Edge>> entry : set) {
					Set<Edge> edgeSet = entry.getValue();
					if (!edgeSet.isEmpty()) {
						count++;
						if (count > 1) {
							break;
						}
					}
				}
				if (count < 2) {
					return null;
				}
			}
			return idValue(id);
		}

		private void writeValue(Value object) throws IOException {
			json.writeStartObject();
			
			if (object instanceof URI) {
				json.writeStringField("@id", curie((URI)object));
				push(graph.vertex((URI)object));
				
			} else if (object instanceof BNode) {
				if (!memory.contains(object.stringValue())) {
					memory.add(object.stringValue());
					Vertex v = graph.vertex((BNode) object);
					writeVertexProperties(v.outEdges(), v);
				} else {
					json.writeStringField("@id", bnodeValue((BNode)object));
				}
				
			} else {
				Literal literal = (Literal) object;
				String value = literal.stringValue();
				String lang = literal.getLanguage();
				URI datatype = literal.getDatatype();
				
				json.writeStringField("@value", value);
				
				if (lang != null) {
					json.writeStringField("@language", lang);
				}
				if (datatype!=null && !XMLSchema.STRING.equals(datatype)) {
					json.writeStringField("@type", curie(datatype));
				}
			}
			
			json.writeEndObject();
			
		}

		private String bnodeValue(BNode object) {
			StringBuilder builder = new StringBuilder();
			builder.append("_:");
			builder.append(object.stringValue());
			return builder.toString();
		}

		private String curie(URI predicate) {
			
			String value = predicate.stringValue();
			String namespace = predicate.getNamespace();
			Namespace ns = nsManager.findByName(namespace);
			if (ns != null) {
				StringBuilder builder = new StringBuilder();
				builder.append(ns.getPrefix());
				builder.append(':');
				builder.append(predicate.getLocalName());
				value = builder.toString();
			} 
			return value;
			
		}

		private String idValue(Resource resource) {
			return (resource instanceof URI) ? curie((URI)resource) : "_:" + resource.stringValue();
		}

		private void addContext() throws IOException {
			
			json.writeObjectFieldStart("@context");
			
			List<Namespace> list = new ArrayList<>(nsManager.listNamespaces());
			Collections.sort(list, new NamespaceComparator());
			for (Namespace namespace : list) {
				json.writeStringField(namespace.getPrefix(), namespace.getName());
			}
			
			json.writeEndObject();
			
		}
	}
	
	public void writeJSON(NamespaceManager nsManager, Graph graph, File outFile) throws IOException {
		JsonFactory factory = new JsonFactory();
		
		FileWriter writer = new FileWriter(outFile);
		try {
			JsonGenerator generator = factory.createGenerator(writer);
			generator.useDefaultPrettyPrinter();
			writeJSON(nsManager, graph, generator);
		} finally {
			close(writer);
		}
	}
	
	private void close(FileWriter writer) {
		try {
			writer.close();
		} catch (Throwable ignore) {
			logger.warn("Failed to close writer", ignore);
		}
		
		
	}

	public void writeJSON(NamespaceManager nsManager, Graph graph, JsonGenerator json) throws IOException {
		
		Worker worker = new Worker();
		worker.writeJSON(nsManager, graph, json);
		
	}


}
