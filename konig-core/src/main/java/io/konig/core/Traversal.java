package io.konig.core;

import java.util.List;
import java.util.Set;

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


import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public interface Traversal {
	
	/**
	 * Filter this traversal to those elements that have the specified property.
	 * @param property The property used to filter the elements.
	 * @return A new traversal that is a filtered copy of this one.
	 */
	Traversal has(URI property);
	
	/**
	 * Get the number of elements in this traversal
	 * @return the number of elements in this traversal
	 */
	int size();
	
	
	
	/**
	 * Filter this traversal to those elements that have the specified property value
	 * @param property The property used to filter the elements
	 * @param value The target value of the property
	 * @return A new Traversal that is a filtered copy of this one.
	 */
	Traversal hasValue(URI property, Value value);
	
	/**
	 * Filter this traversal to those elements that have the specified property value
	 * @param property The property used to filter the elements
	 * @param value The string value of the property
	 * @return A new Traversal that is a filtered copy of this one.
	 */
	Traversal hasValue(URI property, String value);
	
	/**
	 * Filter this traversal so that it includes only named individuals, i.e. resources 
	 * identified by an IRI.
	 * @return A new Traversal that is a filtered copy of this one.
	 */
	Traversal isIRI();
	
	/**
	 * Add a property to all vertices in this traversal
	 * @param property The IRI for the property being added
	 * @param value The value of the property being added.
	 * @return This traversal
	 */
	Traversal addProperty(URI property, Value value);

	/**
	 * Add an object property to all vertices in this traversal.
	 * This is a convenience method that converts the supplied strings to URI values,
	 * and then invokes the addProperty method.
	 * @param property The IRI for the property that is being added.
	 * @param iri The IRI for the resource that is the value of the property
	 * @return This traversal
	 */
	Traversal addObject(String property, String iri);
	
	/**
	 * Filter the elements in this traversal so that each value is unique.
	 * @return This traversal
	 */
	Traversal distinct();
	
	/**
	 * Add a vertex to this Traversal.
	 * @param v The vertex to be added.
	 * @return This traversal
	 */
	Traversal union(Vertex v);
	
	/**
	 * Add a value to this Traversal.
	 * @param v The value to be added.
	 * @return This traversal
	 */
	Traversal union(Value... v);
	
	Traversal union(Set<? extends Value> set);
	
	
	/**
	 * Add a literal property value to all vertices in this traversal.
	 * @param property The IRI for the property that is being added.
	 * @param value The value of the property
	 * @return This traversal
	 */
	Traversal addLiteral(String property, String value);
	

	/**
	 * Add a literal property value to all vertices in this traversal.
	 * @param property The IRI for the property that is being added.
	 * @param value The value of the property
	 * @return This traversal
	 */
	Traversal addLiteral(URI property, String value);
	
	/**
	 * Add one or more vertices to the underlying graph
	 * @param iri The vertex (or vertices) to add.
	 * @return A new Traversal containing the newly added vertices.
	 */
	Traversal addV(Resource...iri);
	
	/**
	 * Iterate over the vertices in this traversal and get the value from the first edge having the specified predicate.
	 * @param predicate The predicate for the value requested.
	 * @return The Value requested, or null if no such value exists.
	 */
	Value firstValue(URI predicate);
	
	/**
	 * Iterate over the vertices in this traversal, get the object of the first outgoing edge having the specified predicate,
	 * and return it as a Vertex.
	 * @param predicate The predicate for the Vertex requested
	 * @return The Vertex requested, or null if no such Vertex exists.
	 */
	Vertex firstVertex(URI predicate);
	
	URI firstIRI();
	
	/**
	 * Move from the current set of vertices in this traversal to a new set of vertices by following outgoing edges
	 * labeled by a given predicate.
	 * 
	 * @param predicate  The label for the edge to be traversed
	 * @return A new Traversal encapsulating the vertices at the other end of the outgoing edges with the specified predicate.
	 */
	Traversal out(URI predicate);
	
	/**
	 * Move outward from the current set of vertices in this traversal to the transitive closure of a given predicate.
	 * @param predicate The predicate whose transitive closure is to be computed.
	 * @return This traversal updated with the transitive closure.
	 */
	Traversal outTransitive(URI predicate);
	
	/**
	 * Move inward from the current set of vertices in this traversal to the transitive closure of a given predicate.
	 * @param predicate The predicate whose transitive closure is to be computed.
	 * @return This traversal updated with the transitive closure.
	 */
	Traversal inTransitive(URI predicate);
	
	
	
	/**
	 * Move from the current set of vertices in this traversal to a new set of vertices by
	 * following incoming edges labeled by a given predicate.
	 * 
	 * @param predicate The label for the edges to be traversed.
	 * @return A new Traversal encapsulating the vertices at the other end of the incoming edges.
	 */
	Traversal in(URI predicate);
	
	/**
	 * From the current set of vertices in this traversal, get the first one.
	 * @return The first vertex in this traversal.
	 */
	Vertex firstVertex();
	
	List<Vertex> toVertexList();
	
	List<Value> toValueList();
	
	Set<URI> toUriSet();
	
	/**
	 * Add the values in this traversal to a given Set.
	 * @param set The set into which values should be added.
	 */
	void addValues(Set<Value> set);
	
}
