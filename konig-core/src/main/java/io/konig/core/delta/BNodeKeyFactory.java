package io.konig.core.delta;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

/**
 * An factory that creates a BNodeKey for a given BNode.
 * @author Greg McFall
 *
 */
public interface BNodeKeyFactory {
	
	/**
	 * Create a key for a given BNode that is the object of some statement.
	 * @param predicate The predicate of the statement
	 * @param object The BNode whose key is to be created.
	 * @return The key for the given BNode which is accessed via the given predicate
	 */
	BNodeKey createKey(URI predicate, Vertex object);
	
	/**
	 * Determine whether a given predicate is part of the key for BNodes handled by this factory.
	 * @param predicate The predicate to be tested
	 * @return True if the supplied predicate is part of the key BNodes handled by this factory, and
	 * false otherwise.
	 */
	boolean isKeyPart(URI predicate);

}
