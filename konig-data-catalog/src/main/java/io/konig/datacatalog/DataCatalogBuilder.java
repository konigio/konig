package io.konig.datacatalog;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.IOUtil;
import io.konig.shacl.ClassManager;
import io.konig.shacl.LogicalShapeBuilder;
import io.konig.shacl.LogicalShapeNamer;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.BasicLogicalShapeNamer;
import io.konig.shacl.impl.MemoryClassManager;

public class DataCatalogBuilder {
	
	private ResourceWriterFactory resourceWriterFactory;
	private ClassIndexWriterFactory classIndexWriterFactory;
	private File outDir;
	
	public DataCatalogBuilder() {
	}

	public void build(URI ontologyId, File outDir, Graph graph, ShapeManager shapeManager) throws DataCatalogException {

		this.outDir = outDir;
		classIndexWriterFactory = new ClassIndexWriterFactory(outDir);
		resourceWriterFactory = new ResourceWriterFactory(outDir);
		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		
		VelocityEngine engine = new VelocityEngine(properties);
		VelocityContext context = new VelocityContext();
		
		Vertex targetOntology = graph.getVertex(ontologyId);
		if (targetOntology == null) {
			throw new DataCatalogException("Target Ontology not defined: " + ontologyId.stringValue());
		}

		PageRequest request = new PageRequest(targetOntology, engine, context, graph, shapeManager);
		try {
			buildOntologyPages(request);
			buildShapePages(request);
			buildClassPages(request);
			buildClassIndex(request);
			buildOntologyIndex(request);
			buildIndexPage(request);
			buildOverviewPage(request);
		} catch (IOException e) {
			throw new DataCatalogException(e);
		}
		
	
		
	}

	private void buildOverviewPage(PageRequest request) throws IOException, DataCatalogException {
		
		File overviewFile = new File(outDir, "overview.html");
		PrintWriter out = new PrintWriter(new FileWriter(overviewFile));
		PageResponse response = new PageResponseImpl(out);
		OverviewPage page = new OverviewPage();
		page.render(request, response);
		IOUtil.close(out, "overview.html");
		
	}

	private void buildIndexPage(PageRequest request) throws IOException {
		
		File index = new File(outDir, "index.html");
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		FileWriter out = new FileWriter(index);
		Template template = engine.getTemplate("data-catalog/velocity/index.vm");
		template.merge(context, out);
		
		IOUtil.close(out, "index.html");
		
	}

	private void buildOntologyPages(PageRequest request) throws IOException, DataCatalogException {
		
		OwlReasoner reasoner = new OwlReasoner(request.getGraph());
		List<Vertex> list = reasoner.ontologyList();
		OntologyPage page = new OntologyPage();
		for (Vertex v : list) {
			if (v.getId() instanceof URI) {
				URI ontologyId = (URI) v.getId();
				OntologyRequest ontologyRequest = new OntologyRequest(request, v);
				URI summary = DataCatalogUtil.ontologySummary(ontologyId.stringValue());
				PrintWriter out = resourceWriterFactory.createWriter(ontologyRequest, summary);
				PageResponse response = new PageResponseImpl(out);
				page.render(ontologyRequest, response);
				IOUtil.close(out, ontologyId.stringValue());
			}
		}
		
	}

	private void buildClassPages(PageRequest request) throws IOException, DataCatalogException {
		String baseURI = "http://www.io.konig.com/logical/shapes/";
		ClassManager classManager = new MemoryClassManager();
		OwlReasoner reasoner = new OwlReasoner(request.getGraph());
		LogicalShapeNamer shapeNamer = new BasicLogicalShapeNamer(baseURI, request.getGraph().getNamespaceManager());
		LogicalShapeBuilder builder = new LogicalShapeBuilder(reasoner, shapeNamer);
		builder.setUsePropertyConstraintComment(true);
		builder.buildLogicalShapes(request.getShapeManager(), classManager);
		
		ClassRequest classRequest = new ClassRequest(request, classManager);
		List<Vertex> classList = request.getGraph().v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		ClassPage page = new ClassPage();
		for (Vertex v : classList) {
			if (v.getId() instanceof URI) {
				URI classId = (URI) v.getId();
				classRequest.setOwlClass(v);
				PrintWriter writer = resourceWriterFactory.createWriter(classRequest, classId);
				PageResponse response = new PageResponseImpl(writer);
				page.render(classRequest, response);
				IOUtil.close(writer, classId.stringValue());
			}
		}
		
	}

	private void buildClassIndex(PageRequest request) throws IOException, DataCatalogException {
		
		ClassIndexPage page = new ClassIndexPage();
		PrintWriter out = classIndexWriterFactory.createWriter(request, null);
		PageResponse response = new PageResponseImpl(out);
		page.render(request, response);
		IOUtil.close(out, "allclasses-index.html" );
	}
	


	private void buildOntologyIndex(PageRequest request) throws IOException, DataCatalogException {
		File file = new File(outDir, DataCatalogUtil.ONTOLOGY_INDEX_FILE);
		PrintWriter out = new PrintWriter(new FileWriter(file));
		PageResponse response = new PageResponseImpl(out);
		OntologyIndexPage page = new OntologyIndexPage();
		page.render(request, response);
		IOUtil.close(out, DataCatalogUtil.ONTOLOGY_INDEX_FILE);
	}

	private void buildShapePages(PageRequest baseRequest) throws IOException, DataCatalogException {

		ShapeRequest request = new ShapeRequest(baseRequest);
		ShapeManager shapeManager = request.getShapeManager();
		ShapePage shapePage = new ShapePage();
		
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				URI shapeURI = (URI) shapeId;
				request.setShape(shape);
				PrintWriter out = resourceWriterFactory.createWriter(request, shapeURI);
				PageResponse response = new PageResponseImpl(out);
				
				shapePage.render(request, response);
				IOUtil.close(out, shapeURI.stringValue());
			}
		}
		
	}

}
