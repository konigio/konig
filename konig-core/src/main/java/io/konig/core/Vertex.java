package io.konig.core;

import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public interface Vertex {

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
	
	
}
