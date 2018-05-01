package io.konig.transform.proto;

import java.util.List;

import io.konig.core.Vertex;

/**
 * A strategy for resolving IRI references by looking up a named individual from the pre-defined, static OWL ontology.
 * @author Greg McFall
 *
 */
public class LookupPredefinedNamedIndividual implements IriResolutionStrategy {
	
	private List<Vertex> individuals;

	public LookupPredefinedNamedIndividual(List<Vertex> individuals) {
		this.individuals = individuals;
	}

	public List<Vertex> getIndividuals() {
		return individuals;
	}

}
