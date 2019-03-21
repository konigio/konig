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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
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

import static io.konig.datacatalog.OwlClassCategory.classes;
import static io.konig.datacatalog.OwlClassCategory.entities;
import static io.konig.datacatalog.OwlClassCategory.enums;

import static io.konig.datacatalog.ClassDisplayName.business;
import static io.konig.datacatalog.ClassDisplayName.technical;

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
	public static final URI DATASOURCE_SUMMARY_URI = new URIImpl("urn:datacatalog/datasources");
	public static final URI INDEX_ALL_URI = new URIImpl("urn:datacatalog/index-all");
	public static final URI SETTINGS_URI = new URIImpl("urn:datacatalog/settings");
	public static final URI SETTINGS_ICON = new URIImpl("urn:datacatalog/.images/settings.png");
	
	private static List<MenuItem> menu = new ArrayList<>();
	static {
		menu.add(new MenuItem(OVERVIEW_URI, "Overview"));
		menu.add(new MenuItem(INDEX_ALL_URI, "Index"));
		menu.add(new MenuItem(DATASOURCE_SUMMARY_URI, "Datasources"));
		menu.add(new MenuItem(SETTINGS_URI, "Settings", SETTINGS_ICON));
		
		Collections.reverse(menu);
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
		graph.getNamespaceManager().add("konig", Konig.NAMESPACE);
			
		this.outDir = buildRequest.getOutDir();
		buildRequest.setFileFactory(new CatalogFileFactory(outDir));
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
			buildImages();
			buildSettingsPage(request);
			buildOntologyPages(request);
			buildShapePages(request, exampleDir);
			buildClassPages(request);
			buildPropertyPages(request);
			buildClassIndex(request, business, classes);
			buildClassIndex(request, technical, classes);
			buildClassIndex(request, business, entities);
			buildClassIndex(request, technical, entities);
			buildClassIndex(request, business, enums);
			buildClassIndex(request, technical, enums);
			buildOntologyIndex(request);
			buildIndexPage(request);
			buildOverviewPage(request);
			buildDatasourceSummary(request);
			buildIndexAllPage(request);
		} catch (IOException e) {
			throw new DataCatalogException(e);
		}
		
	
		
	}


	private void buildSettingsPage(PageRequest request) throws IOException, DataCatalogException {
				
		File overviewFile = new File(outDir, "settings.html");
		try (PrintWriter out = new PrintWriter(new FileWriter(overviewFile))) {
			PageResponse response = new PageResponseImpl(out);
			SettingsPage page = new SettingsPage();
			page.render(request, response);
		}
		
		
	}


	private void buildImages() throws IOException {
		
		File imagesFolder = new File(outDir, ".images");
		File settings = new File(imagesFolder, "settings.png");
		
		copyResource("data-catalog/velocity/images/settings.png", settings);
		
		
	}
	
	private void copyResource(String path, File file) throws IOException {
		file.getParentFile().mkdirs();
		InputStream input = getClass().getClassLoader().getResourceAsStream(path);
		try (FileOutputStream out = new FileOutputStream(file)) {
			byte[] array = new byte[1024];
			int len = 0;
			while ( (len=input.read(array)) > 0) {
				out.write(array, 0, len);
			}
		} finally {
			IOUtil.close(input, "settings.png");
		}
	}



	private void buildDatasourceSummary(PageRequest request) throws IOException, DataCatalogException {

		File overviewFile = new File(outDir, "datasources.html");
		try (PrintWriter out = new PrintWriter(new FileWriter(overviewFile))) {
			PageResponse response = new PageResponseImpl(out);
			DataSourceSummaryPage page = new DataSourceSummaryPage();
			page.render(request, response);
		}	
		
	}


	private void buildPropertyPages(PageRequest baseRequest) throws IOException, DataCatalogException {
		
		
		PropertyPage page = new PropertyPage();
		
		for (PropertyStructure p : baseRequest.getClassStructure().listProperties()) {
			PropertyRequest request = new PropertyRequest(baseRequest, p);
			try (PrintWriter writer = resourceWriterFactory.createWriter(request, p.getPredicate()) ) {
		
				PageResponse response = new PageResponseImpl(writer);
				request.setContext(new VelocityContext());
				page.render(request, response);
			}
		}
	}


	public List<Link> setActiveItem(PageRequest request, URI pageId) throws DataCatalogException {
		List<Link> list = new ArrayList<>();
		for (MenuItem item : menu) {
			String name = item.getName();
			String href = request.relativePath(item.getItemId());
			String className = item.getItemId().equals(pageId) ? "activeLink" : null;
			String iconSrc = request.relativePath(item.getIcon());
			
			list.add(new Link(name, href, className, iconSrc));
			
		}
		request.getContext().put("Menu", list);
		
		return list;
	}

	private void buildOverviewPage(PageRequest request) throws IOException, DataCatalogException {
		
		File overviewFile = new File(outDir, "overview.html");
		try (PrintWriter out = new PrintWriter(new FileWriter(overviewFile))) {
			PageResponse response = new PageResponseImpl(out);
			OverviewPage page = new OverviewPage();
			page.render(request, response);
		}
		
	}

	
	private void buildIndexAllPage(PageRequest request) throws IOException, DataCatalogException {
		File indexAllFile = new File(outDir, "index-all.html");
		try (PrintWriter out = new PrintWriter(new FileWriter(indexAllFile))) {
			PageResponse response = new PageResponseImpl(out);
			IndexAllPage page = new IndexAllPage();
			page.render(request, response);
		}
		
	}

	private void buildIndexPage(PageRequest request) throws IOException {
		
		File index = new File(outDir, "index.html");
		VelocityEngine engine = request.getEngine();
		VelocityContext context = request.getContext();
		try (FileWriter out = new FileWriter(index)) {
			Template template = engine.getTemplate("data-catalog/velocity/index.vm");
			template.merge(context, out);
		}
		
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
				try (PrintWriter out = resourceWriterFactory.createWriter(ontologyRequest, summary)) {
					PageResponse response = new PageResponseImpl(out);
					page.render(ontologyRequest, response);
				}
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
				try (PrintWriter writer = resourceWriterFactory.createWriter(classRequest, classId)) {
					PageResponse response = new PageResponseImpl(writer);
					page.render(classRequest, response);
				}
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
		try (PrintWriter writer = resourceWriterFactory.createWriter(classRequest, classId)) {
			PageResponse response = new PageResponseImpl(writer);
			page.render(classRequest, response);
		}
		
	}


	private void buildClassIndex(PageRequest request, ClassDisplayName displayName, OwlClassCategory classCategory) throws IOException, DataCatalogException {
		
		String fileName = "index-" + displayName.name() + "-" + classCategory.name() + ".html";
		ClassIndexPage page = new ClassIndexPage(displayName, classCategory);
		File indexFile = new File(outDir, fileName);
		try (PrintWriter out = new PrintWriter(new FileWriter(indexFile))) {
			PageResponse response = new PageResponseImpl(out);
			page.render(request, response);
		}
	}
	


	private void buildOntologyIndex(PageRequest request) throws IOException, DataCatalogException {
		File file = new File(outDir, DataCatalogUtil.ONTOLOGY_INDEX_FILE);
		try (PrintWriter out = new PrintWriter(new FileWriter(file))) {
			PageResponse response = new PageResponseImpl(out);
			OntologyIndexPage page = new OntologyIndexPage();
			page.render(request, response);
		}
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
				try (PrintWriter out = resourceWriterFactory.createWriter(request, shapeURI)) {
					PageResponse response = new PageResponseImpl(out);
					
					shapePage.render(request, response);
				}
			}
		}
		
	}

	


}
