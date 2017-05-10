package io.konig.triplestore.core;

import java.util.Collection;

import org.openrdf.model.Statement;
import org.openrdf.model.URI;

public interface Triplestore {

	void save(URI resourceId, Collection<Statement> outEdges) throws TriplestoreException;
	Collection<Statement> getOutEdges(URI resourceId) throws TriplestoreException;
	Collection<Statement> getInEdges(URI resourceId) throws TriplestoreException;
	void remove(URI resourceId) throws TriplestoreException;
}
