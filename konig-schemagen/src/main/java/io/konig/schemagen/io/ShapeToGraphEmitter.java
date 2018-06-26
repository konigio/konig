package io.konig.schemagen.io;

import java.io.IOException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeWriter;

public class ShapeToGraphEmitter implements Emitter {
	
	private ShapeManager shapeManager;

	public ShapeToGraphEmitter(ShapeManager shapeManager) {
		this.shapeManager = shapeManager;
	}


	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		ShapeWriter shapeWriter = new ShapeWriter();
		for (Shape shape : shapeManager.listShapes()) {
			shapeWriter.emitShape(shape, graph);
		}

	}

}
