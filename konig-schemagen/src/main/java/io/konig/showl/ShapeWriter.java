package io.konig.showl;

import java.io.Closeable;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.rio.RDFHandlerException;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.extract.ResourceExtractor;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.SH;

/**
 * A utility that writes the SHACL Shape descriptions to files.
 * @author Greg McFall
 *
 */
public class ShapeWriter {

	private FileGetter fileGetter;
	private ResourceExtractor extractor;

	public ShapeWriter(FileGetter fileGetter) {
		this.fileGetter = fileGetter;
		extractor = new ResourceExtractor();
	}
	
	public void writeShapes(Graph graph) throws IOException, RDFHandlerException {
		List<Vertex> list = graph.v(SH.Shape).in(RDF.TYPE).toVertexList();
		for (Vertex shape : list) {
			writeShape(shape);
		}
	}

	private void writeShape(Vertex shape) throws IOException, RDFHandlerException {
		Resource shapeId = shape.getId();
		if (shapeId instanceof URI) {
			URI shapeURI = (URI) shapeId;
			File file = fileGetter.getFile(shapeURI);
			FileWriter out = new FileWriter(file);
			Graph target = new MemoryGraph();
			extractor.extract(shape, target);
			target.setNamespaceManager(shape.getGraph().getNamespaceManager());
			
			try {
				RdfUtil.prettyPrintTurtle(target, out);
			} finally {
				close(out);
			}
		}
		
	}

	private void close(Closeable out) {
		try {
			out.close();
		} catch (Throwable ignore) {
			
		}
		
	}
	
	
}
