package io.konig.core.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.SPARQLBuilder;
import io.konig.core.TraversalException;
import io.konig.core.Traverser;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;

public class HasStep implements Step {

	private List<PredicateValuePair> list = new ArrayList<>();
	
	public HasStep(URI predicate, Value value) {
		list.add(new PredicateValuePair(predicate, value));
		
	}
	
	public HasStep() {
		
	}
	
	public void add(URI predicate, Value value) {
		list.add(new PredicateValuePair(predicate, value));
	}
	
	public List<PredicateValuePair> getPairList() {
		return list;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean result = false;
		
		if (other instanceof HasStep) {
			HasStep b = (HasStep) other;
			
			if (b.list.size() == list.size()) {
				if (list.size()==1) {
					result = list.get(0).compareTo(b.list.get(0))==0;
				} else {
					result = true;
					List<PredicateValuePair> mine = new ArrayList<>(list);
					List<PredicateValuePair> yours = new ArrayList<>(b.list);
					Collections.sort(mine);
					Collections.sort(yours);
					for (int i=0; i<mine.size(); i++) {
						if (mine.get(i).compareTo(yours.get(i)) != 0) {
							result = false;
							break;
						}
					}
				}
			}
		}
		
		return result;
	}

	@Override
	public void traverse(Traverser traverser) throws TraversalException {

		Graph graph = traverser.getGraph();
		Set<Value> source = traverser.getSource();
		
		for (Value s : source) {
			if (s instanceof Resource) {
				Resource subject = (Resource) s;
				
				for (PredicateValuePair pair : list) {
					
					if (pair.javaValue != null) {
						
						Vertex vertex = graph.getVertex(subject);
						if (hasValue(vertex, pair)) {
							traverser.addResult(s);
						}
						
					} else if (graph.contains(subject, pair.predicate, pair.value)) {
						traverser.addResult(s);
					}
				}
				
				
			}
		}

	}

	private boolean hasValue(Vertex vertex, PredicateValuePair pair) {
		Set<Edge> set = vertex.outProperty(pair.predicate);
		for (Edge e : set) {
			Value value = e.getObject();
			if (value instanceof Literal) {
				Object object = RdfUtil.javaValue((Literal)value);
				if (RdfUtil.nearEqual(pair.javaValue, object)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void visit(SPARQLBuilder builder) {
		String comma = "";
		builder.append('[');
		for (PredicateValuePair pair : list) {
			builder.append(comma);
			builder.append(pair.predicate);
			builder.append(' ');
			builder.append(pair.value);
			comma = ",\n";
		}
		builder.append(']');
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		String comma = "";
		builder.append('[');
		for (PredicateValuePair pair : list) {
			builder.append(comma);
			builder.append(pair.predicate.getLocalName());
			builder.append(' ');
			builder.append(pair.value);
			comma = ",\n";
		}
		builder.append(']');
		return builder.toString();
	}

	@Override
	public void append(StringBuilder builder, NamespaceManager nsManager) {
		throw new RuntimeException("Not implemented");
		
	}
	
	public static class PredicateValuePair implements Comparable<PredicateValuePair> {
		private URI predicate;
		private Value value;
		private Object javaValue;
		public PredicateValuePair(URI predicate, Value object) {
			this.predicate = predicate;
			this.value = object;
			if (value instanceof Literal) {
				javaValue = RdfUtil.javaValue((Literal)value);
			}
		}
		
		public URI getPredicate() {
			return predicate;
		}

		public Value getValue() {
			return value;
		}

		@Override
		public int compareTo(PredicateValuePair other) {
			int result = predicate.stringValue().compareTo(other.predicate.stringValue());
			if (result == 0) {
				result = value.stringValue().compareTo(other.value.stringValue());
			}
			return result;
		}
		
		
		
		
	}

}
