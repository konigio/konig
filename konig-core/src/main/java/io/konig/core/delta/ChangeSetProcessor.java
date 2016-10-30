package io.konig.core.delta;

import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.vocab.CS;

/**
 * A processor that applies a ChangeSet to a source graph.
 * @author Greg McFall
 *
 */
public class ChangeSetProcessor {

	


	public void applyChanges(Graph source, Graph changeSet, Graph target) {
		// First copy all edges from source to target
		for (Edge e : source) {
			target.edge(e);
		}
		
		// Now edit the target in accordance with the changeSet
		for (Edge e : changeSet) {
			
			Value value = e.getAnnotation(RDF.TYPE);
			
			
			if (e.matches(CS.Falsity, value)) {
				target.remove(e);
			} else if (e.matches(CS.Dictum, value)) {
				target.edge(e);
			}
		}
		
	}

}
