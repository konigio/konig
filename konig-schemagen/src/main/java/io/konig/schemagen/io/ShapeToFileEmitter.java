package io.konig.schemagen.io;

import java.io.File;
import java.io.IOException;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.io.VertexCopier;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.io.ShapeFileGetter;

public class ShapeToFileEmitter implements Emitter {

	private ShapeManager shapeManager;
	private File outDir;

	public ShapeToFileEmitter(ShapeManager shapeManager, File outDir) {
		this.shapeManager = shapeManager;
		this.outDir = outDir;
	}




	@Override
	public void emit(Graph graph) throws IOException, KonigException {
		if (!outDir.exists()) {
			outDir.mkdirs();
		}
		NamespaceManager nsManager = graph.getNamespaceManager();
		ShapeFileGetter fileGetter = new ShapeFileGetter(outDir, nsManager);
		VertexCopier copier = new VertexCopier();
		copier.excludeProperty(SH.shape, SH.path, SH.targetClass, SH.valueClass, Konig.aggregationOf, Konig.rollUpBy,
				Konig.defaultShapeFor, Konig.inputShapeOf, Konig.tabularOriginShape);
		copier.excludeClass(OWL.CLASS, OWL.DATATYPEPROPERTY, OWL.OBJECTPROPERTY, OWL.FUNCTIONALPROPERTY, RDF.PROPERTY);
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;

				Vertex shapeVertex = graph.getVertex(shapeURI);
				Graph shapeGraph = new MemoryGraph();
				copier.deepCopy(shapeVertex, shapeGraph);
				File shapeFile = fileGetter.getFile(shapeURI);
				try {
					RdfUtil.prettyPrintTurtle(nsManager, shapeGraph, shapeFile);
				} catch (RDFHandlerException e) {
					throw new KonigException("Failed to save Shape: " + shapeId, e);
				}
			}

		}

	}

}
