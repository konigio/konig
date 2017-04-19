package io.konig.schemagen.gcp;

import java.io.File;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapeVisitor;
import io.konig.shacl.io.ShapeFileGetter;
import io.konig.shacl.io.ShapeWriter;

public class EnumShapeVisitor implements ShapeVisitor {
	
	private ShapeFileGetter fileGetter;
	private ShapeManager shapeManager;
	
	public EnumShapeVisitor(ShapeFileGetter fileGetter, ShapeManager shapeManager) {
		this.fileGetter = fileGetter;
		this.shapeManager = shapeManager;
	}

	@Override
	public void visit(Shape shape)  {

		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI shapeURI = (URI) shapeId;
			shapeManager.addShape(shape);
			ShapeWriter shapeWriter = new ShapeWriter();

			Graph graph = new MemoryGraph();
			shapeWriter.emitShape(shape, graph);
			File shapeFile = fileGetter.getFile(shapeURI);
			try {
				RdfUtil.prettyPrintTurtle(fileGetter.getNamespaceManager(), graph, shapeFile);
			} catch (RDFHandlerException | IOException e) {
				throw new KonigException(e);
			}
		}
	}

}
