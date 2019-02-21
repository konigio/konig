package io.konig.shacl;

/*
 * #%L
 * konig-shacl
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
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;

/**
 * A service that selects the set of vertices that match a given shape
 * @author Greg McFall
 *
 */
public class GraphFilter {
	
	public static final GraphFilter INSTANCE = new GraphFilter();
	
	/**
	 * Test whether a given vertex matches a given shape.
	 * @param vertex The vertex to be tested.
	 * @param shape The shape to be matched against the vertex
	 * @return True if the vertex matches the shape, and false otherwise.
	 */
	public boolean matches(Vertex vertex, Shape shape) {
		
		List<PropertyConstraint> propertyList = shape.getProperty();
		
		for (PropertyConstraint p : propertyList) {
			if (!satisfies(vertex, p)) {
				return false;
			}
		}
		
		return accept(shape.getOr(), vertex) && accept(shape.getAnd(), vertex);
	}


	private boolean accept(Constraint constraint, Vertex vertex) {
		
		return constraint==null || constraint.accept(vertex);
	}


	/**
	 * Test whether a given vertex satisfies a given property constraint
	 * @param vertex The vertex to be tested
	 * @param p The PropertyConstraint that is being tested
	 * @return True if the vertex satisfies the property constraint, and false otherwise.
	 */
	private boolean satisfies(Vertex vertex, PropertyConstraint p) {
		Graph graph = vertex.getGraph();
		
		URI predicate = p.getPredicate();
		Integer minCount = p.getMinCount();
		Integer maxCount = p.getMaxCount();
		URI datatype = p.getDatatype();
		URI directType = p.getDirectValueType();
		Shape valueShape = p.getShape();
		List<? extends Value> allowedValues = p.getIn();
		
		Set<Edge> set = vertex.outProperty(predicate);
		if (minCount !=null && set.size()<minCount) {
			return false;
		}
		if (maxCount != null && set.size()>maxCount) {
			return false;
		}
		if (allowedValues != null) {
			for (Edge e : set) {
				Value object = e.getObject();
				if (!allowedValues.contains(object)) {
					return false;
				}
			}
		}
		if (datatype != null) {
			for (Edge e : set) {
				Value object = e.getObject();
				if (object instanceof Literal) {
					Literal literal = (Literal) object;
					URI type = literal.getDatatype();
					if (!datatype.equals(type)) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		if (directType != null) {
			for (Edge e : set) {
				Value object = e.getObject();
				if (object instanceof Resource) {
					Vertex v = graph.vertex((Resource)object);
					Set<Edge> rdfType = v.outProperty(RDF.TYPE);
					boolean ok = false;
					if (!rdfType.isEmpty()) {
						for (Edge te : rdfType) {
							Object t = te.getObject();
							if (directType.equals(t)) {
								ok = true;
								break;
							}
						}
					}
					if (!ok) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		if (valueShape != null) {
			for (Edge e : set) {
				Value object = e.getObject();
				if (object instanceof Resource) {
					Vertex v = graph.vertex((Resource)object);
					if (!matches(v, valueShape)) {
						return false;
					}
				} else {
					return false;
				}
			}
		}
		
		
		return true;
	}

	/**
	 * From a particular graph, select the set of vertices that match a given shape.
	 * @param source The graph from which vertices will be selected
	 * @param shape The shape that vertices must match.
	 * @return The list of vertices that match the given shape.
	 */
	public List<Vertex> select(Graph source, Shape shape) {
		List<Vertex> list = new ArrayList<Vertex>();
		
		return list;
	}

}
