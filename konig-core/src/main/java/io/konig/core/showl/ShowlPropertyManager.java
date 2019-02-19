package io.konig.core.showl;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.ShapeManager;

public class ShowlPropertyManager extends ShowlManager {

	public ShowlPropertyManager(ShapeManager shapeManager, OwlReasoner reasoner) {
		super(shapeManager, reasoner);
	}

	public void build() {
		buildClasses();
		buildProperties();
		loadShapes();
		inferTargetClasses();
		inferInverses();
	}

	

	private void buildProperties() {
		
		Graph graph = getReasoner().getGraph();
		
		for (Vertex property : graph.vertex(RDF.PROPERTY).asTraversal().in(RDF.TYPE).toVertexList()) {
			
			if (property.getId() instanceof URI) {
				produceShowlProperty((URI)property.getId());
			}
		}
		
	}

	private void buildClasses() {
		
		Graph graph = getReasoner().getGraph();
		
		
		for (Vertex owlClass : graph.vertex(OWL.CLASS).asTraversal().in(RDF.TYPE).toVertexList()) {
			if (owlClass.getId() instanceof URI) {
				this.produceOwlClass((URI) owlClass.getId());
			}
		}
		
		
		
	}


}
