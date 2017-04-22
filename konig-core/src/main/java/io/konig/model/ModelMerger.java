package io.konig.model;

/*
 * #%L
 * Konig Core
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


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.SH;

/**
 * A utility that merges two semantic models into a single larger model
 * @author Greg McFall
 *
 */
public class ModelMerger {
	
	private static final Logger logger = LoggerFactory.getLogger(ModelMerger.class);
	
	/**
	 * Merge entities from the source graph into the sink graph.
	 * @throws MergeException 
	 */
	public void merge(Graph source, Graph sink) throws MergeException {
		for (Vertex v : source.vertices()) {
			mergeVertex(v, sink);
		}
	}
	
	private void mergeVertices(Vertex v, Vertex w, Graph sink) throws MergeException {
		if (w == null) {
			copy(v, sink);
		} else {
			Set<Entry<URI, Set<Edge>>> out = v.outEdges();
			for (Entry<URI, Set<Edge>> entry : out) {
				URI predicate = entry.getKey();
				Set<Edge> sourceValues = entry.getValue();
				Set<Edge> sinkValues = w.outProperty(predicate);
				
				if (sinkValues.isEmpty()) {
					copyEdges(sourceValues, sink);
				} else {
					boolean distinct = sourceValues.size()==1 && sinkValues.size()==1;
					
					Map<String,Vertex> bnodeMap = bnodeMap(w, predicate, sinkValues);
					
					for (Edge sourceEdge : sourceValues) {
						Value a = sourceEdge.getObject();
						
						if (a instanceof URI) {
							if (distinct) {
								Value b = sourceValues.iterator().next().getObject();
								if (!a.equals(b)) {
									StringBuilder msg = new StringBuilder();
									msg.append("Entity <");
									msg.append(v.getId().stringValue());
									msg.append("> has incompatible values for '");
									msg.append(predicate.getLocalName());
									msg.append("': <");
									msg.append(a.stringValue());
									msg.append("> and <");
									msg.append(b.stringValue());
									msg.append(">");
									throw new MergeException(msg.toString());
								}
							} else if (!containsValue(a, sinkValues)) {
								sink.edge(w.getId(), predicate, a);
							}
						} else if (a instanceof Literal) {
							if (!distinct && !containsValue(a, sinkValues)) {
								sink.edge(w.getId(), predicate, a);
							}
						} else if (a instanceof BNode) {
							Vertex aNode = v.getGraph().getVertex((BNode)a);
							String aKey = bnodeKey(v, predicate, aNode);
							if (aKey != null) {
								Vertex bNode = bnodeMap.get(aKey);
								mergeVertices(aNode, bNode, sink);
							}
						}
					}
				}
				
			}
		}
	}

	private void mergeVertex(Vertex v, Graph sink) throws MergeException {
		
		Vertex w = sink.getVertex(v.getId());
		mergeVertices(v, w, sink);
	}

	

	private Map<String, Vertex> bnodeMap(Vertex w, URI predicate, Set<Edge> outEdges) {
		Map<String, Vertex> map = new HashMap<>();
		for (Edge edge : outEdges) {
			Value object = edge.getObject();
			if (object instanceof BNode) {
				Vertex bnode = w.getGraph().getVertex((BNode)object);
				String key = bnodeKey(w, predicate, bnode);
				if (key != null) {
					map.put(key, bnode);
				} else {
					logger.warn("Failed to create BNode key for predicate: " + predicate.getLocalName());
				}
			}
		}
		return map;
	}

	private String bnodeKey(Vertex w, URI predicate, Vertex bnode) {
		StringBuilder builder = new StringBuilder();
		if (predicate.equals(RDFS.SUBCLASSOF)) {
			
			appendKey(builder, bnode, OWL.ONPROPERTY);
			appendKeyTerm(builder, bnode, OWL.ALLVALUESFROM);
			appendKeyTerm(builder, bnode, OWL.SOMEVALUESFROM);
			appendKeyTerm(builder, bnode, OWL.HASVALUE);
			appendKeyTerm(builder, bnode, OWL.MAXCARDINALITY);
			appendKeyTerm(builder, bnode, OWL.MINCARDINALITY);
			appendKeyTerm(builder, bnode, OWL.CARDINALITY);
			
		} else if (predicate.equals(SH.property)) {
			appendKey(builder, bnode, SH.predicate);
		}
		return builder.length()==0 ? null : builder.toString();
	}

	private void appendKeyTerm(StringBuilder builder, Vertex bnode, URI predicate) {
		if (bnode.getValue(predicate) != null) {
			builder.append('|');
			builder.append(predicate.stringValue());
		}
		
	}

	private void appendKey(StringBuilder builder, Vertex bnode, URI predicate) {
		
		Value value = bnode.getValue(predicate);
		if (value != null) {
			builder.append(predicate.stringValue());
			builder.append('|');
			builder.append(value.stringValue());
		}
		
	}

	private boolean containsValue(Value a, Set<Edge> sinkValues) {
		for (Edge edge : sinkValues) {
			Value b = edge.getObject();
			if (a.equals(b)) {
				return true;
			}
		}
		return false;
	}

	private void copyEdges(Set<Edge> sourceValues, Graph sink) {
		for (Edge e : sourceValues) {
			Edge f = sink.edge(e);
			if (f.getObject() instanceof BNode) {
				Vertex object = sink.vertex((BNode)f.getObject());
				copy(object, sink);
			}
		}
	}

	private void copy(Vertex v, Graph sink) {
		
		Set<Edge> out = v.outEdgeSet();
		copyEdges(out, sink);
		
	}

	

	
}
