package io.konig.core.path;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.TraversalException;
import io.konig.core.Vertex;

public class OutStep implements Step {
	URI predicate;
	

	public OutStep(URI predicate) {
		this.predicate = predicate;
	}
	

	public URI getPredicate() {
		return predicate;
	}


	@Override
	public void traverse(Traverser traverser) throws TraversalException {
		
		Graph graph = traverser.getGraph();
		Set<Value> source = traverser.getSource();
		for (Value s : source) {
			if (s instanceof Resource) {
				Vertex subject = graph.vertex((Resource)s);
				Set<Edge> edges = subject.outProperty(predicate);
				for (Edge edge : edges) {
					Value object = edge.getObject();
					traverser.addResult(object);
				}
			}
		}
		
	}

}
