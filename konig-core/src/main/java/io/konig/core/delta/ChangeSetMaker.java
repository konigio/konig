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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.vocab.CS;

public class ChangeSetMaker {
	
	private static final URI KEYLIST = new URIImpl("http://www.konig.io/ns/kcs/keyList");

	/**
	 * Compute the ChangeSet between two graphs.
	 * @param source The source graph that serves as a baseline.
	 * @param target The target graph that represents a change from the baseline.
	 * @return A ChangeSet that describes the delta from source to target.
	 */
	public Graph computeDelta(Graph source, Graph target, KeyExtractor matcher) {
		Worker worker = new Worker(source, target, matcher);
		return worker.compute();
	}
	
	private static class Worker {
		private Graph source;
		private Graph target;
		private KeyExtractor keyExtractor;
		private Graph changes;
		private Map<Vertex, Vertex> bnodeMap = new HashMap<>();
		
		public Worker(Graph source, Graph target, KeyExtractor matcher) {
			this.source = source;
			this.target = target;
			this.keyExtractor = matcher;
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
				remove(v);
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
					BNodeKey bnodeKey = keyExtractor.extractKeys(predicate, object1);
					// Stash the keylist in the edge so we can fetch it later.
					e.setProperty(KEYLIST, bnodeKey);
					String key = bnodeKey.getKey();
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
					BNodeKey bnodeKey = keyExtractor.extractKeys(predicate, object);
					String key = bnodeKey.getKey();
					map.put(key, object);
				}
			}
			return map;
		}




		private boolean diff(Vertex v, Vertex w, BNodeKey keyList) {
			int initialSize = changes.size();
			diffSource(v, w, keyList);
			diffTarget(v, w);
			return initialSize != changes.size();
			
		}



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
					changes.edge(e).setProperty(CS.function, CS.Add);
					Value object = e.getObject();
					if (object instanceof BNode) {
						BNode bnode = (BNode)object;
						add(target.getVertex(bnode));
					}
				}
			}
		}



		private void add(Resource subject, URI predicate, Value object) {
			changes.edge(subject, predicate, object).setProperty(CS.function, CS.Add);
		}



		private void diffSource(Vertex v, Vertex w, BNodeKey bnodeKey) {
			Set<Entry<URI,Set<Edge>>> out = v.outEdges();
			for (Entry<URI, Set<Edge>> entry : out) {
				URI predicate = entry.getKey();
				Set<Edge> set = entry.getValue();
				boolean mapBNodes = true;
				
				for (Edge e : set) {
					Value object1 = e.getObject();
					
					if (object1 instanceof BNode) {
						
						if (mapBNodes && keyExtractor!=null) {
							mapBNodes = false;
							mapBNodes(v, w, predicate, set);
						}
						

						// Get the key list that was stashed in the edge when the BNode was mapped.
						BNodeKey bnodeKey2 = (BNodeKey) e.removeProperty(KEYLIST);
						if (bnodeKey2 != null) {
							
							
							BNode bnode1 = (BNode) object1;
							Vertex vertex1 = source.getVertex(bnode1);
							Vertex vertex2 = bnodeMap.get(vertex1);
							if (vertex2 == null) {
								removeEdge(e);
								remove(vertex1);
							} else {
								Edge keyEdge = key(e);
								if (!diff(vertex1, vertex2, bnodeKey2)) {
									changes.remove(keyEdge);
								}
							}
						} else {
							throw new KonigException("TODO: handle the case where keys are not defined");
						}
						
						
						
						
					} else {
						if (bnodeKey!=null && bnodeKey.getPredicates().contains(e.getPredicate())) {
							key(e);
						} else if (!w.hasProperty(predicate, object1)) {
							removeEdge(e);
						}
					}
				}
			}
			
		}



		private Edge key(Edge e) {
			Edge edge = changes.edge(e);
			edge.setProperty(CS.function, CS.Key);
			return edge;
		}



		private void removeEdge(Edge e) {
			changes.edge(e).setProperty(CS.function, CS.Remove);
		}



		private void remove(Vertex v) {
			
			Set<Entry<URI,Set<Edge>>> out = v.outEdges();
			
			for (Entry<URI, Set<Edge>> entry : out) {
				Set<Edge> set = entry.getValue();
				for (Edge e : set) {
					changes.edge(e).setProperty(CS.function, CS.Remove);
				}
			}
		}
	}
}
