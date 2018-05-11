package io.konig.transform.proto;

import io.konig.transform.rule.SimpleResultSet;

/**
 * A strategy for resolving IRI references by looking up a named individual from the pre-defined, static OWL ontology.
 * @author Greg McFall
 *
 */
public class LookupPredefinedNamedIndividual implements IriResolutionStrategy {
	
	private SimpleResultSet resultSet;

	public LookupPredefinedNamedIndividual(SimpleResultSet resultSet) {
		this.resultSet = resultSet;
	}

	public SimpleResultSet getResultSet() {
		return resultSet;
	}

	

}
