package io.konig.core;

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
