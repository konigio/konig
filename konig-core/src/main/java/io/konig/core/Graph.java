package io.konig.core;

import java.util.Collection;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

public interface Graph {
	
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
	
	Edge edge(Vertex subject, URI predicate, Vertex object);
	
	Traversal v(Resource subject);
	
	/**
	 * Get the Transactional view of this Graph.
	 * 
	 * @return The Transactional view of this Graph, or null if the Graph is not transactional
	 */
	Transaction tx();
}
