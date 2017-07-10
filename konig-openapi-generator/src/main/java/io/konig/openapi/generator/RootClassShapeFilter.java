package io.konig.openapi.generator;

import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeFilter;

public class RootClassShapeFilter implements ShapeFilter {

	private Graph graph;
	
	public RootClassShapeFilter(Graph graph) {
		this.graph = graph;
	}
	
	
	/**
	 * Accept shapes with sh:nodeKind equal to sh:IRI and whose
	 * target class does not have a super class.
	 */
	@Override
	public boolean accept(Shape shape) {
		boolean ok = false;
		if ((shape.getNodeKind() == NodeKind.IRI) &&
				(shape.getMediaTypeBaseName()!=null) &&
				shape.hasDataSourceType(Konig.GoogleBigQueryTable)
		) {
			
			ok = true;
			
			URI targetClass = shape.getTargetClass();
			Vertex v = graph.getVertex(targetClass);
			if (v != null) {
				Set<Edge> set = v.outProperty(RDFS.SUBCLASSOF);
				if (!set.isEmpty()) {
					for (Edge e : set) {
						Value superClass = e.getObject();
						if (!superClass.equals(OWL.THING)) {
							ok = false;
							break;
						}
					}
				}
			}
		}
		return ok;
	}

}
