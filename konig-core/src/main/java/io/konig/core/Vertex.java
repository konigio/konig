package io.konig.core;

import java.util.List;

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


import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public interface Vertex {
	
	/**
	 * Get a representation of this Vertex as a named graph.
	 * @return The Graph representation of this Vertex, or null if this Vertex is not a named graph
	 */
	Graph asNamedGraph();
	
	/**
	 * Assert that this Vertex is a named graph and get that graph.
	 * @return The Graph representation of this Vertex.  A graph representation will be created
	 * if it does not exist.
	 */
	Graph assertNamedGraph();

	/**
	 * Get the graph that contains this vertex.
	 * @return The graph that contains this vertex.
	 */
	Graph getGraph();
	
	/**
	 * The identifier for this Vertex
	 * @return
	 */
	Resource getId();
	
	/**
	 * Returns a set of entries for the outgoing edges
	 */
	Set<Entry<URI, Set<Edge>>> outEdges();
	
	Set<Entry<URI, Set<Edge>>> inEdges();
	
	/**
	 * Returns a set containing all of the incoming edges
	 */
	Set<Edge> inEdgeSet();
	
	/**
	 * Returns a set containing all of the outgoing edges
	 */
	Set<Edge> outEdgeSet();
	
	boolean hasEdge(Edge edge);
	
	void addProperty(URI property, Value value);
	
	/**
	 * Get the Set of outgoing edges with the specified predicate.
	 * @param predicate The predicate for the outgoing edges
	 * @return The set of outgoing edges with the specified predicate, or an empty set
	 * 		if there are no such outgoing edges.
	 */
	Set<Edge> outProperty(URI predicate);
	
	/**
	 * Get the value of a specified property.
	 * @param predicate The predicate that identifies the property whose value is to be returned.
	 * @return A value for the specified property, or null if no such property exists.  If the vertex
	 * contains more than one value for the given property, the returned value is indeterminate.
	 */
	Value getValue(URI predicate);
	
	/**
	 * Get the set of values of a specified property
	 */
	Set<Value> getValueSet(URI predicate);
	
	/**
	 * Get the value of a specified property as a URI.
	 * @param predicate The predicate that identifies the properties whose value is to be returned.
	 * @return The URI value for the specified property.  If no such property exists or does not have 
	 * a URI value, this method returns null.
	 */
	URI getURI(URI predicate);
	
	/**
	 * Get the value of a property as a Vertex
	 * @param predicate The predicate that identifies the property
	 * @return A Vertex representing the value of the specified property, or null if no such value exists.
	 * @throws KonigException If there is more than one value for the specified property
	 */
	Vertex getVertex(URI predicate) throws KonigException;
	
	Integer integerValue(URI predicate);
	
	String stringValue(URI predicate);
	
	Double doubleValue(URI predicate);
	
	Vertex vertexValue(URI predicate);
	
	/**
	 * Test whether the Vertex has a given value for a specified property. 
	 * @param predicate The property to be tested
	 * @param value The value to be tested
	 * @return True if the vertex has the given value for the specified property and false otherwise
	 */
	boolean hasProperty(URI predicate, Value value);
	
	/**
	 * Get the Set of incoming edges with the specified predicate.
	 * @param predicate The predicate for the incoming edges
	 * @return The set of incoming edges with the specified predicate
	 */
	Set<Edge> inProperty(URI predicate);
	
	Traversal asTraversal();
	
	void remove(Edge edge);
	
	List<Value> asList();
	
	/**
	 * Test whether the Vertex has any incoming edges.
	 * @return True if there are no incoming edges and false otherwise
	 */
	boolean isOrphan();
	
}
