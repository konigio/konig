package io.konig.transform.factory;

import java.io.File;
import java.io.IOException;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;

public class TransformTest {
	protected NamespaceManager nsManager = new MemoryNamespaceManager();
	protected Graph graph = new MemoryGraph(nsManager);
	protected ShapeManager shapeManager = new MemoryShapeManager();

	

	protected URI iri(String value) {
		return new URIImpl(value);
	}

	protected void load(String path) throws RDFParseException, RDFHandlerException, IOException {

		GcpShapeConfig.init();
		File sourceDir = new File(path);
		RdfUtil.loadTurtle(sourceDir, graph, shapeManager);
		
	}

}