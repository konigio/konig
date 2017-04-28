package io.konig.schemagen.jsonld;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.openrdf.model.Namespace;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Context;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.io.ContextWriter;
import io.konig.core.vocab.Konig;
import io.konig.schemagen.SchemaGeneratorException;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.jsonld.ContextGenerator;
import io.konig.shacl.jsonld.ContextNamer;

/**
 * A utility that generates a JSON-LD context for a given data shape.
 * @author Greg McFall
 *
 */
public class ShapeToJsonldContext {
	private static final Logger logger = LoggerFactory.getLogger(ShapeToJsonldContext.class);
	
	private ShapeManager shapeManager;
	private NamespaceManager nsManager;
	private ContextNamer contextNamer;
	private Graph owlGraph;
	
	

	public ShapeToJsonldContext(ShapeManager shapeManager, NamespaceManager nsManager, ContextNamer contextNamer,
			Graph owlGraph) {
		this.shapeManager = shapeManager;
		this.nsManager = nsManager;
		this.contextNamer = contextNamer;
		this.owlGraph = owlGraph;
	}
	
	
	public void generateAll(File baseDir) throws SchemaGeneratorException, IOException {
		baseDir.mkdirs();
		List<Shape> list = shapeManager.listShapes();
		for (Shape shape : list) {
			
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI uri = (URI) shapeId;
				Namespace ns = nsManager.findByName(uri.getNamespace());
				if (ns == null) {
					throw new SchemaGeneratorException("Namespace not found: " + uri.getNamespace());
				}
				StringBuilder fileName = new StringBuilder();
				fileName.append(ns.getPrefix());
				fileName.append('.');
				fileName.append(uri.getLocalName());
				
				File file = new File(baseDir, fileName.toString());
				Context context = generateJsonldContext(shape, file);
				
				URI contextId = new URIImpl(context.getContextIRI());
				shape.setPreferredJsonldContext(contextId);
				owlGraph.edge(shapeId, Konig.preferredJsonldContext, contextId);
			}
		}
	}

	public Context generateJsonldContext(Shape shape, File contextFile) throws SchemaGeneratorException, IOException {
		ContextGenerator generator = new ContextGenerator(shapeManager, nsManager, contextNamer, owlGraph);
		Context context = generator.forShape(shape);
		
		FileWriter fileWriter = new FileWriter(contextFile);
		try {

			ContextWriter writer = new ContextWriter();
			writer.write(context, fileWriter);
		} finally {
			close(fileWriter);
		}
		return context;
	}

	private void close(FileWriter fileWriter) {
		try {
			fileWriter.flush();
			fileWriter.close();
		} catch (IOException ignore) {
			logger.warn("Failed to close file", fileWriter);
		}
		
	}
}
