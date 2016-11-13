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


import java.util.Collection;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public interface Graph extends Set<Edge> {
	
	GraphBuilder builder();
	
	NamespaceManager getNamespaceManager();
	void setNamespaceManager(NamespaceManager nsManager);
	
	/**
	 * Get the identifier this Graph as a named graph.
	 * @return The identifier for this Graph or null if it has no identifier.
	 */
	Resource getId();
	
	/**
	 * Set the identifier for this Graph as a named graph.
	 * @param id The identifier for this Graph
	 */
	void setId(Resource id);
	
	/**
	 * A convenience method that creates a new Vertex with a BNode identifier.
	 * @return The newly created Vertex with a BNode identifier.
	 */
	Vertex vertex();
	
	/**
	 * A convenience method that gets or creates a Vertex from a string IRI identifier.
	 * @param id The IRI for the vertex that is to be returned.
	 * @return The Vertex with the specified IRI.  If no such vertex exists in this Graph, it will be created.
	 */
	Vertex vertex(String id);
	
	/**
	 * Returns the set of vertices in this Graph
	 */
	Collection<Vertex> vertices();

	/**
	 * Create or get a Vertex with the specified id.
	 * @param id The identifier for the Vertex.
	 * @return The Vertex with the specified id.  If no such vertex exists in this Graph, it will be created.
	 */
	Vertex vertex(Resource id);
	
	/**
	 * Get the Vertex with the specified id.
	 * @param id The identifier for the Vertex that is to be returned
	 * @return The Vertex with the specified id, or null if no such Vertex exists in this Graph.
	 */
	Vertex getVertex(Resource id);
	
	/**
	 * A convenience method that gets the Vertex with the specified id.
	 * @param id The identifier for the Vertex that is to be returned
	 * @return The Vertex with the specified id, or null if no such Vertex exists in this Graph.
	 */
	Vertex getVertex(String id);
	
	/**
	 * Add an edge to this Graph.
	 * @param subject The subject in the statement expressed by the edge.
	 * @param predicate The predicate in the statement expressed by the edge
	 * @param object The value in the statement expressed by the edge.
	 * @return The edge that was added to this Graph
	 */
	Edge edge(Resource subject, URI predicate, Value object);
	Edge edge(Resource subject, URI predicate, Value object, Resource context);
	
	Edge edge(Vertex subject, URI predicate, Vertex object);

	
	Edge edge(Edge edge);
	
	/**
	 * Add all the outgoing statements from the given vertex.
	 * If the object of any outgoing statement is a BNode, then recursively add its 
	 * outgoing statements also.
	 * 
	 * @param vertex The vertex whose outgoing statements will be added to this graph.
	 */
	void add(Vertex vertex);
	
	/**
	 * Remove the specified edge from this graph
	 * @param edge The edge to be removed.
	 */
	void remove(Edge edge);
	
	/**
	 * Remove a vertex from this graph.
	 * Removes all statements that include the vertex as a subject or predicate.
	 * All BNodes orphaned by this process will also be removed.
	 * @param v The vertex to be removed
	 */
	void remove(Vertex v);
	
	/**
	 * Remove a resource from this graph.
	 * This is a convenience method equivalent to the following snippet:
	 * <pre>
	 *   Vertex v = graph.getVertex(resource);
	 *   graph.remove(v);
	 * </pre>
	 * @param resource
	 */
	void remove(Resource resource);
	
	/**
	 * Return a traversal for the specified subject.
	 * A vertex for this subject will be added to the graph as a side-effect if it does not already exist.
	 * @param subject
	 * @return
	 */
	Traversal v(Resource subject);
	
	/**
	 * Get the Transactional view of this Graph.
	 * 
	 * @return The Transactional view of this Graph, or null if the Graph is not transactional
	 */
	Transaction tx();
	
	boolean contains(Resource subject, URI predicate, Value object);
}
