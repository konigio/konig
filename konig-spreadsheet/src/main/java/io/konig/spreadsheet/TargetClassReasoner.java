package io.konig.spreadsheet;

import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class TargetClassReasoner implements ShapeVisitor {
	private Graph graph;
	
	

	public TargetClassReasoner(Graph graph) {
		this.graph = graph;
	}



	@Override
	public void visit(Shape shape) {
		
		if (shape.getTargetClass() == null) {
			Vertex v = graph.getVertex(shape.getId());	
			if (v != null) {
				List<Vertex> list = v.asTraversal().in(SH.shape).toVertexList();
				URI targetClass = null;
				for (Vertex w : list) {
					URI classValue = w.getURI(SH.valueClass);
					if (classValue == null) {
						return;
					}
					if (targetClass == null) {
						targetClass = classValue;
					} else if (!targetClass.equals(classValue)) {
						return;
					}
				}
				if (targetClass != null) {
					shape.setTargetClass(targetClass);
					graph.edge(shape.getId(), SH.targetClass, targetClass);
				}
			}
		}

	}

}
