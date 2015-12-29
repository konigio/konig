package io.konig.shacl;

import io.konig.core.Vertex;

public interface Constraint {

	/**
	 * Test whether a given vertex satisfies this constraint
	 * @param v The vertex to be tested
	 * @return True if the vertex satisfies this constraint and false otherwise
	 */
	boolean accept(Vertex v);
}
