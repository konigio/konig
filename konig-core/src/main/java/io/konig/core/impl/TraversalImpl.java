package io.konig.core.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Traversal;
import io.konig.core.Vertex;

public class TraversalImpl implements Traversal {
	
	private Graph graph;
	private List<Object> list = new ArrayList<Object>();
	
	public TraversalImpl(Graph g) {
		this.graph = g;
	}
	
	public TraversalImpl(Vertex v) {
		graph = v.getGraph();
		list.add(v);
	}
	
	@SuppressWarnings("unchecked")
	public TraversalImpl(Graph g, List<?> list) {
		this.graph = g;
		this.list = (List<Object>) list;
	}

	public Traversal has(URI property) {
		TraversalImpl result = new TraversalImpl(graph);
		
		List<Object> sink = result.list;
		
		for (Object e : list) {
			
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(property);
				if (!set.isEmpty()) {
					sink.add(v);
				}
			}
		}
		
		return result;
	}

	public int size() {
		return list.size();
	}

	public Traversal hasValue(URI property, Value value) {

		TraversalImpl result = new TraversalImpl(graph);
		
		List<Object> sink = result.list;
		
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex)e;

				Set<Edge> set = v.outProperty(property);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (value.equals(object)) {
						sink.add(v);
					}
				}
			}
		}
		return result;
	}
	public Traversal addProperty(URI property, Value value) {
		
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;
				v.getGraph().edge(v.getId(), property, value);
			}
		}
		return this;
	}
	public Traversal addObject(String property, String iri) {		
		return addProperty(uri(property), uri(iri));
	}
	public Traversal addLiteral(String property, String value) {
		return addProperty(uri(property), literal(value));
	}
	
	public Literal literal(String value) {
		return new KonigLiteral(value);
	}
	
	public URI uri(String value) {
		return new URIImpl(value);
	}
	public Traversal addV(Resource... iri) {
		TraversalImpl result = new TraversalImpl(graph);
		for (int i=0; i<iri.length; i++) {
			Vertex v = graph.vertex(iri[i]);
			result.list.add(v);
		}
		return result;
	}
	public Traversal addLiteral(URI property, String value) {
		return addProperty(property, literal(value));
	}

	@Override
	public URI firstIRI(URI predicate) {
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex)e;
				Resource r = v.getId();
				if (r instanceof URI) {
					return (URI) r;
				}
			}
			if (e instanceof URI) {
				return (URI) e;
			}
		}
		return null;
	}
	
	public Value firstValue(URI predicate) {
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(predicate);
				for (Edge edge : set) {
					return edge.getObject();
				}
			}
		}
		return null;
	}
	public Traversal hasValue(URI property, String value) {

		TraversalImpl result = new TraversalImpl(graph);
		
		List<Object> sink = result.list;
		
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(property);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (value.equals(object.stringValue())) {
						sink.add(v);
					}
				}
			}
		}
		return result;
	}
	public Traversal out(URI predicate) {
		TraversalImpl result = new TraversalImpl(graph);
		List<Object> sink = result.list;
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.outProperty(predicate);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (object instanceof Resource) {
						Vertex outV = graph.vertex((Resource)object);
						sink.add(outV);
					} else {
						// TODO: Consider implementing a Vertex wrapper around literals
						sink.add(object);
					}
				}
			}
		}
		
		return result;
	}
	@Override
	public Vertex firstVertex(URI predicate) {

		for (Object e : list) {
			
			if (e instanceof Vertex) {
				Vertex v = (Vertex)e;
				
				Set<Edge> set = v.outProperty(predicate);
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (object instanceof Resource) {
						return graph.vertex((Resource)object);
					}
				}
				
			}
		}
		return null;
	}
	@Override
	public Traversal in(URI predicate) {
		TraversalImpl result = new TraversalImpl(graph);
		List<Object> sink = result.list;
		for (Object e : list) {
			if (e instanceof Vertex) {
				Vertex v = (Vertex) e;

				Set<Edge> set = v.inProperty(predicate);
				for (Edge edge : set) {
					Resource subject = edge.getSubject();
					sink.add(graph.vertex(subject));
				}
			}
		}
		
		return result;
	}
	@Override
	public Vertex firstVertex() {
		
		for (Object obj : list) {
			if (obj instanceof Vertex) {
				return (Vertex) obj;
			}
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Vertex> toVertexList() {
		if (list.isEmpty() || list.get(0) instanceof Vertex) {
			return (List<Vertex>) ((Object)list);
		}
		return null;
	}
	@Override
	public void addValues(Set<Value> set) {
		
		for (Object obj : list) {
			if (obj instanceof Value) {
				set.add((Value)obj);
			} else if (obj instanceof Vertex) {
				Vertex v = (Vertex)obj;
				set.add(v.getId());
			}
		}
		
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<Value> toValueList() {
		if (list.isEmpty() || list.get(0) instanceof Value) {
			return (List<Value>) ((Object)list);
		}
		List<Value> result = new ArrayList<>();
		for (Object obj : list) {
			if (obj instanceof Vertex) {
				Vertex v = (Vertex) obj;
				result.add(v.getId());
			} else if (obj instanceof Value) {
				result.add((Value)obj);
			}
		}
		return result;
	}

	@Override
	public Traversal distinct() {
		Map<String,Object> map = new HashMap<>();
		for (Object value : list) {
			if (value instanceof Vertex) {
				Vertex v = (Vertex) value;
				map.put(v.getId().stringValue(), v);
			} else if (value instanceof Value) {
				Value v = (Value) value;
				map.put(v.stringValue(), value);
			}
		}

		list = new ArrayList<>(map.values());
		return this;
	}

	@Override
	public Traversal outTransitive(URI predicate) {
		
		Map<String,Object> closure = new HashMap<>();
		
		for (int i=0; i<list.size(); i++) {
			Object element = list.get(i);
			Vertex v = (element instanceof Vertex) ? (Vertex) element :
				(element instanceof Resource) ? graph.vertex((Resource) element) :
				null;
				
			if (v != null) {
				Set<Edge> out = v.outProperty(predicate);
				for (Edge e : out) {
					Value object = e.getObject();
					if (!closure.containsKey(object.stringValue()) && object instanceof Resource) {
						Vertex next = graph.vertex((Resource)object);
						list.add(next);
						closure.put(object.stringValue(), next);
					}
				}
			}
		}
		
		list = new ArrayList<>(closure.values());
		return this;
	}

	@Override
	public Traversal inTransitive(URI predicate) {

		Map<String,Object> closure = new HashMap<>();
		
		for (int i=0; i<list.size(); i++) {
			Object element = list.get(i);
			Vertex v = (element instanceof Vertex) ? (Vertex) element :
				(element instanceof Resource) ? graph.vertex((Resource) element) :
				null;
				
			if (v != null) {
				Set<Edge> in = v.inProperty(predicate);
				for (Edge e : in) {
					Value subject = e.getSubject();
					if (!closure.containsKey(subject.stringValue()) && subject instanceof Resource) {
						Vertex next = graph.vertex((Resource)subject);
						list.add(next);
						closure.put(subject.stringValue(), next);
					}
				}
			}
		}
		
		list = new ArrayList<>(closure.values());
		return this;
	}

	@Override
	public Traversal union(Vertex v) {
		for (Object obj : list) {
			if (obj instanceof Vertex) {
				Vertex w = (Vertex) obj;
				if (w.getId().equals(v.getId())) {
					return this;
				}
			}
			if (obj instanceof Value) {
				Value w = (Value) obj;
				if (v.getId().stringValue().equals(w.stringValue())) {
					return this;
				}
			}
		}
		list.add(v);
		return this;
	}

	@Override
	public Traversal union(Value v) {
		if (v instanceof Resource) {
			return union(graph.vertex((Resource)v));
		}
		for (Object obj : list) {
			if (obj instanceof Value) {
				Value w = (Value) obj;
				if (w.equals(v)) {
					return this;
				}
			}
		}
		list.add(v);
		return this;
	}

	@Override
	public Traversal isIRI() {
		TraversalImpl result = new TraversalImpl(graph);

		List<Object> sink = result.list;
		
		for (Object obj : list) {
			if (obj instanceof Vertex) {
				Vertex v = (Vertex) obj;
				Resource id = v.getId();
				if (id instanceof URI) {
					sink.add(v);
				}

			}
		}
		return result;
	}
	
	
	
	

}
