package io.konig.core.delta;

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


import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.Konig;

public class ChangeSetFactory {
	
	private static final URI KEYLIST = new URIImpl("http://www.konig.io/ns/kcs/keyList");
	
	private boolean preserveNamedIndividuals;

	/**
	 * Compute the difference between two graphs.
	 * @param source The source graph that serves as a baseline.
	 * @param target The target graph that represents a change from the baseline.
	 * @return A new graph containing annotated statements that describe the delta from source to target.  
	 * We call this delta a "change set".
	 */
	public Graph createChangeSet(Graph source, Graph target, BNodeKeyFactory factory) {
		Worker worker = new Worker(source, target, factory);
		return worker.compute();
	}
	
	
	/**
	 * Get the configuration setting for preserving named individuals.
	 * @return True if named individuals should be preserved and false otherwise.
	 */
	public boolean isPreserveNamedIndividuals() {
		return preserveNamedIndividuals;
	}



	/**
	 * Specify whether named individuals should be preserved.
	 * @param preserveNamedIndividuals  True if named individuals should be preserved, and false otherwise.
	 */
	public void setPreserveNamedIndividuals(boolean preserveNamedIndividuals) {
		this.preserveNamedIndividuals = preserveNamedIndividuals;
	}



	private class Worker {
		private Graph source;
		private Graph target;
		private BNodeKeyFactory keyFactory;
		private Graph changes;
		private Map<Vertex, Vertex> bnodeMap = new HashMap<>();
		
		public Worker(Graph source, Graph target, BNodeKeyFactory factory) {
			this.source = source;
			this.target = target;
			this.keyFactory = factory;
			changes = new MemoryGraph();
		}



		private Graph compute() {
			
			scanSource();
			scanTarget();
			
			return changes;
		}



		private void scanTarget() {
			
			for (Vertex w : target.vertices()) {
				if (w.getId() instanceof URI) {
					Vertex v = source.getVertex(w.getId());
					if (v == null) {
						add(w);
					}
				}
			}
			
		}



		private void scanSource() {
			for (Vertex v : source.vertices()) {
				if (v.getId() instanceof URI) {
					handleSourceURI(v);
				}
			}
		}



		private void handleSourceURI(Vertex v) {
			Vertex w = target.getVertex(v.getId());
			if (w == null) {
				if (!preserveNamedIndividuals) {
					remove(v, null);
				}
			} else {
				diff(v, w, null);
			}
			
		}




		private void mapBNodes(Vertex subject1, Vertex subject2, URI predicate, Set<Edge> set1) {
			Set<Edge> set2 = subject2.outProperty(predicate);
			Map<String, Vertex> map2 = keyMap(subject2, predicate, set2, target);
			
			for (Edge e : set1) {
				Value value = e.getObject();
				if (value instanceof BNode) {
					BNode bnode1 = (BNode) value;
					Vertex object1 = source.getVertex(bnode1);
					BNodeKey bnodeKey = keyFactory.createKey(predicate, object1);
					// Stash the keylist in the edge so we can fetch it later.
					e.setAnnotation(KEYLIST, bnodeKey);
					String key = bnodeKey.getHash();
					Vertex object2 = map2.get(key);
					if (object2 != null) {
						bnodeMap.put(object1, object2);
						bnodeMap.put(object2, object1);
					}
				}
			}
			
			
		}
		
		private Map<String, Vertex> keyMap(Vertex subject, URI predicate, Set<Edge> objectSet, Graph graph) {
			Map<String, Vertex> map = new HashMap<>();
			for (Edge e : objectSet) {
				Value value = e.getObject();
				if (value instanceof BNode) {
					BNode bnode = (BNode) value;
					Vertex object = graph.getVertex(bnode);
					BNodeKey bnodeKey = keyFactory.createKey(predicate, object);
					String key = bnodeKey.getHash();
					map.put(key, object);
				}
			}
			return map;
		}




		/**
		 * Compute the delta between two vertices.
		 * @param v A vertex from the source graph.
		 * @param w A vertex from the target graph
		 * @param bnodeKey  A unique key for the source vertex if it is a BNode, and null otherwise.
		 * @return true if there is a difference between the two vertices, and false otherwise.
		 */
		private boolean diff(Vertex v, Vertex w, BNodeKey bnodeKey) {
			int initialSize = changes.size();
			diffSource(v, w, bnodeKey);
			diffTarget(v, w);
			return initialSize != changes.size();
			
		}


		/**
		 * For each statement in the target vertex, check whether the source
		 * vertex contains a corresponding statement.  If no corresponding statement
		 * is found, then record an add function in the ChangeSet.
		 * @param v The source vertex
		 * @param w the target vertex
		 */
		private void diffTarget(Vertex v, Vertex w) {

			Resource subject = v.getId();
			Set<Entry<URI,Set<Edge>>> out = w.outEdges();
			for (Entry<URI, Set<Edge>> entry : out) {
				URI predicate = entry.getKey();
				Set<Edge> set = entry.getValue();
				for (Edge e : set) {
					Value object2 = e.getObject();
					
					if (object2 instanceof BNode) {
						BNode bnode2 = (BNode) object2;
						Vertex vertex2 = target.getVertex(bnode2);
						Vertex vertex1 = bnodeMap.get(vertex2);
						if (vertex1 == null) {
							add(subject, predicate, vertex2.getId());
							add(vertex2);
						}
					} else if (!v.hasProperty(predicate, object2)) {
						add(subject, predicate, object2);
					}
				}
			}
			
		}



		private void add(Vertex v) {
			
			Set<Entry<URI,Set<Edge>>> out = v.outEdges();
			
			for (Entry<URI, Set<Edge>> entry : out) {
				Set<Edge> set = entry.getValue();
				for (Edge e : set) {
					changes.edge(e).addAnnotation(RDF.TYPE, Konig.Dictum);
					Value object = e.getObject();
					if (object instanceof BNode) {
						BNode bnode = (BNode)object;
						add(target.getVertex(bnode));
					}
				}
			}
		}



		private void add(Resource subject, URI predicate, Value object) {
			changes.edge(subject, predicate, object).addAnnotation(RDF.TYPE, Konig.Dictum);
		}



		/**
		 * For each statement in the source vertex, check to see if a corresponding
		 * statement exists in the target vertex.  If no corresponding statement in 
		 * the target is found, then record a remove function for that statement in the
		 * ChangeSet.
		 * @param v
		 * @param w
		 * @param bnodeKey
		 */
		private void diffSource(Vertex v, Vertex w, BNodeKey bnodeKey) {
			
			
			Set<Entry<URI,Set<Edge>>> out = v.outEdges();
			for (Entry<URI, Set<Edge>> entry : out) {
				URI predicate = entry.getKey();
				Set<Edge> set = entry.getValue();
				boolean mapBNodes = true;
				URI keyPart = bnodeKey==null ? null : bnodeKey.keyPart(predicate);
			
				
				for (Edge e : set) {
					Value object1 = e.getObject();
					
					if (object1 instanceof BNode) {
						
						if (mapBNodes && keyFactory!=null) {
							mapBNodes = false;
							mapBNodes(v, w, predicate, set);
						}
						// Get the BNodeKey that was stashed in the edge when the BNode was mapped.
						BNodeKey childKey = (BNodeKey) e.removeAnnotation(KEYLIST);		
						
						if (childKey != null) {
							BNode bnode1 = (BNode) object1;
							Vertex sourceBNode = source.getVertex(bnode1); 
							Vertex targetBNode = bnodeMap.get(sourceBNode);
							
							if (targetBNode == null) {
								// No matching bnode in the target
								changes.edge(e).addAnnotation(RDF.TYPE, Konig.Falsity);
								removeBNode(sourceBNode, childKey);
								
							} else {
								// found a matching bnode in the target
								
								Edge edge = key(e);
								if (!diff(sourceBNode, targetBNode, childKey)) {
									Resource doomed = (Resource) edge.getObject();
									changes.remove(doomed);
								}
								
								changes.edge(e).addAnnotation(RDF.TYPE, Konig.KeyValue);
								
								
								
							}
							
						} else {
							throw new KonigException("TODO: handle the case where keys are not defined");
						}
						
					} else if (!w.hasProperty(predicate, object1)){
						
						Edge edge = changes.edge(e).addAnnotation(RDF.TYPE, Konig.Falsity);
						if (keyPart!=null) {
							edge.addAnnotation(RDF.TYPE, keyPart);
						}
						
						
					} else if (keyPart!=null) {
						changes.edge(e).addAnnotation(RDF.TYPE, keyPart);
					}
				}
			}
			
		}



		private void removeBNode(Vertex bnode, BNodeKey key) {
			Set<Edge> out = bnode.outEdgeSet();
			for (Edge e : out) {
				URI predicate = e.getPredicate();
				URI part = key.keyPart(predicate);
				if (part != null) {
					changes.edge(e).addAnnotation(RDF.TYPE, part);
				}
			}
			
		}




		private Edge key(Edge e) {
			Edge edge = changes.edge(e);
			edge.addAnnotation(RDF.TYPE, Konig.KeyValue);
			return edge;
		}



		private void removeEdge(Edge e) {
			changes.edge(e).addAnnotation(RDF.TYPE, Konig.Falsity);
		}

		private void remove(Vertex v, BNodeKey bnodeKey) {
			
			Set<Entry<URI,Set<Edge>>> out = v.outEdges();
			Graph g = v.getGraph();
			
			
			for (Entry<URI, Set<Edge>> entry : out) {
				Set<Edge> set = entry.getValue();
				URI predicate = entry.getKey();
				URI part = bnodeKey==null ? null : bnodeKey.keyPart(predicate);
				
				for (Edge e : set) {
					if (part!=null) {
						changes.edge(e).addAnnotation(RDF.TYPE, part);
					} else {
						removeEdge(e);
					}
					Value object = e.getObject();
					if (object instanceof BNode) {
						Vertex w = g.getVertex((BNode)object);
						BNodeKey childKey = bnodeKey(predicate, w);
						remove(w, childKey);
					}
				}
			}
		}



		private BNodeKey bnodeKey(URI predicate, Vertex object) {
			return keyFactory==null ? null : keyFactory.createKey(predicate, object);
		}
	}
}
