package io.konig.core.path;

import io.konig.core.TraversalException;

public interface Step {

	void traverse(Traverser traverser) throws TraversalException;
}
