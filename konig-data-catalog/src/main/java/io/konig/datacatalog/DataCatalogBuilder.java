package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.NamespaceImpl;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.IOUtil;
import io.konig.core.util.SimpleValueFormat;
import io.konig.core.vocab.Konig;
import io.konig.shacl.ClassStructure;
import io.konig.shacl.PropertyStructure;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class DataCatalogBuilder {
	public static final String DCAT_PREFIX = "_dcat_";
	public static final String OVERVIEW = "Overview";
	public static final String CLASSES = "Classes";
	
	public static final String CATALOG_BASE_URI = "urn:datacatalog/";
	public static final URI OVERVIEW_URI = new URIImpl("urn:datacatalog/overview");
	public static final URI INDEX_ALL_URI = new URIImpl("urn:datacatalog/index-all");
	
	private static List<MenuItem> menu = new ArrayList<>();
	static {
		menu.add(new MenuItem(OVERVIEW_URI, "Overview"));
		menu.add(new MenuItem(INDEX_ALL_URI, "Index"));
	}
	
	private ResourceWriterFactory resourceWriterFactory;
	private ClassIndexWriterFactory classIndexWriterFactory;
	private File outDir;
	private File velocityLog;
	
	public DataCatalogBuilder() {
	}
	

	public File getVelocityLog() {
		return velocityLog;
	}


	public void setVelocityLog(File velocityLog) {
		this.velocityLog = velocityLog;
	}


	public void build(DataCatalogBuildRequest buildRequest) throws DataCatalogException {

		Graph graph = buildRequest.getGraph();
		ShapeManager shapeManager = buildRequest.getShapeManager();
		File exampleDir = buildRequest.getExampleDir();
		
		graph.getNamespaceManager().add(new NamespaceImpl(DCAT_PREFIX, CATALOG_BASE_URI));
		graph.getNamespaceManager().add("rdf", RDF.NAMESPACE);
			
		this.outDir = buildRequest.getOutDir();
		classIndexWriterFactory = new ClassIndexWriterFactory(outDir);
		resourceWriterFactory = new ResourceWriterFactory(outDir);
		Properties properties = new Properties();
		properties.put("resource.loader", "class");
		properties.put("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		if (velocityLog != null) {
			properties.put("runtime.log", velocityLog.getAbsolutePath());
		}
		
		VelocityEngine engine = new VelocityEngine(properties);
		buildRequest.setCatalogBuilder(this);
		buildRequest.setEngine(engine);
		OwlReasoner reasoner = new OwlReasoner(graph);
		SimpleValueFormat iriTemplate = new SimpleValueFormat("http://example.com/shapes/canonical/{targetClassNamespacePrefix}/{targetClassLocalName}");
		ClassStructure classStructure = new ClassStructure(iriTemplate, shapeManager, reasoner);
		buildRequest.setClassStructure(classStructure);
		
		PageRequest request = new PageRequest(buildRequest);
		try {
			buildOntologyPages(request);
			buildShapePages(request, exampleDir);
			buildClassPages(request);
			buildPropertyPages(request);
			buildClassIndex(request);
			buildOntologyIndex(request);
			buildIndexPage(request);
			buildOverviewPage(request);
			buildIndexAllPage(request);
		} catch (IOException e) {
			throw new DataCatalogException(e);
		}
		
	
		
	}


	private void buildPropertyPages(PageRequest baseRequest) throws IOException, DataCatalogException {
		
		
		PropertyPage page = new PropertyPage();
		
		for (PropertyStructure p : baseRequest.getClassStructure().listProperties()) {
			PropertyRequest request = new PropertyRequest(baseRequest, p);
			PrintWriter writer = resourceWriterFactory.createWriter(request, p.getPredicate());
			PageResponse response = new PageResponseImpl(writer);
			request.setContext(new VelocityContext());
			page.render(request, response);
		}
	}


	public List<Link> setActiveItem(PageRequest request, URI pageId) throws DataCatalogException {
		List<Link> list = new ArrayList<>();
		for (MenuItem item : menu) {
			String name = item.getName();
			String href = request.relativePath(item.getItemId());
			
			if (item.getItemId().equals(pageId)) {
				list.add(new Link(name, href, "activelink"));
			} else {
				list.add(new Link(name, href));
			}
		}
		request.getContext().put("Menu", list);
		
		return list;
	}

	private void buildOverviewPage(PageRequest request) throws IOException, DataCatalogException {
		
		File overviewFile = new File(outDir, "overview.html");
		PrintWriter out = new PrintWriter(new FileWriter(overviewFile));
		PageResponse response = new PageResponseImpl(out);
		OverviewPage page = new OverviewPage();
		page.render(request, response);
		IOUtil.close(out, "overview.html");
		
	}

	
	private void buildIndexAllPage(PageRequest request) throws IOException, DataCatalogException {
		File indexAllFile = new File(outDir, "index-all.html");
		PrintWriter out = new PrintWriter(new FileWriter(indexAllFile));
		PageResponse response = new PageResponseImpl(out);
		IndexAllPage page = new IndexAllPage();
		page.render(request, response);
		IOUtil.close(out, "index-all.html");
		
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
		
		List<Vertex> classList = request.getGraph().v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		ClassPage page = new ClassPage();
		for (Vertex v : classList) {
			if (v.getId() instanceof URI) {
				URI classId = (URI) v.getId();
				ClassRequest classRequest = new ClassRequest(request, v, resourceWriterFactory);
				PrintWriter writer = resourceWriterFactory.createWriter(classRequest, classId);
				PageResponse response = new PageResponseImpl(writer);
				page.render(classRequest, response);
				IOUtil.close(writer, classId.stringValue());
			}
		}
		
		if (request.getBuildRequest().isShowUndefinedClass()) {
			buildUndefinedClassPage(request);
		}
		
	}

	private void buildUndefinedClassPage(PageRequest request) throws IOException, DataCatalogException {

		UndefinedClassPage page = new UndefinedClassPage();
		URI classId = Konig.Undefined;
		ClassRequest classRequest = new ClassRequest(request, null, resourceWriterFactory);
		PrintWriter writer = resourceWriterFactory.createWriter(classRequest, classId);
		PageResponse response = new PageResponseImpl(writer);
		page.render(classRequest, response);
		IOUtil.close(writer, classId.stringValue());
		
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

	private void buildShapePages(PageRequest baseRequest, File exampleDir) throws IOException, DataCatalogException {

		ShapeManager shapeManager = baseRequest.getShapeManager();
		ShapePage shapePage = new ShapePage();
		
		for (Shape shape : shapeManager.listShapes()) {
			Resource shapeId = shape.getId();
			if (shapeId instanceof URI) {
				
				if (shape.getTargetClass()==null && baseRequest.getBuildRequest().isShowUndefinedClass()) {
					shape = shape.clone();
					shape.setTargetClass(Konig.Undefined);
				}
				URI shapeURI = (URI) shapeId;
				ShapeRequest request = new ShapeRequest(baseRequest, shape, exampleDir);
				PrintWriter out = resourceWriterFactory.createWriter(request, shapeURI);
				PageResponse response = new PageResponseImpl(out);
				
				shapePage.render(request, response);
				IOUtil.close(out, shapeURI.stringValue());
			}
		}
		
	}
	


}
