package io.konig.core;

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
	
	/**
	 * Get the Set of outgoing edges with the specified predicate.
	 * @param predicate The predicate for the outgoing edges
	 * @return The set of outgoing edges with the specified predicate, or an empty set
	 * 		if there are no such outgoing edges.
	 */
	Set<Edge> outProperty(URI predicate);
	
	/**
	 * Get the Set of incoming edges with the specified predicate.
	 * @param predicate The predicate for the incoming edges
	 * @return The set of incoming edges with the specified predicate
	 */
	Set<Edge> inProperty(URI predicate);
	
	Traversal asTraversal();
	
	void remove(Edge edge);
	
	
}
