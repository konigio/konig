package io.konig.datacatalog;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.OwlReasoner;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.CompositeSourceNodeSelector;
import io.konig.core.showl.ExplicitDerivedFromSelector;
import io.konig.core.showl.GoogleStorageBucketSourceNodeSelector;
import io.konig.core.showl.HasDataSourceTypeSelector;
import io.konig.core.showl.LineageShowlNodeShapeConsumer;
import io.konig.core.showl.RawCubeSourceNodeSelector;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlTargetNodeSelector;
import io.konig.core.util.IOUtil;
import io.konig.core.vocab.Konig;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class DataCatalogBuilderTest {
	private static Logger logger = LoggerFactory.getLogger(DataCatalogBuilderTest.class);
	private DataCatalogBuilder builder = new DataCatalogBuilder();

	private File exampleDir = new File("src/test/resources/DataCatalogBuilder/examples");
	private File outDir = new File("target/test/DataCatalogBuilder");
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private OwlReasoner reasoner = new OwlReasoner(graph);
	private ShapeManager shapeManager = new MemoryShapeManager();
	private ShowlManager showlManager;
	
	
	
	@Test
	public void testMappings() throws Exception {

		GcpShapeConfig.init();
		URI ontologyId = uri("http://schema.org/");


		ShowlTargetNodeSelector targetNodeSelector = new HasDataSourceTypeSelector(Konig.GoogleBigQueryTable);
		showlManager = new ShowlManager(
				shapeManager, reasoner, targetNodeSelector, nodeSelector(shapeManager), 
				new LineageShowlNodeShapeConsumer());
		
		
		
		build("src/test/resources/MappingTest", ontologyId);

		File htmlFile = new File("target/test/DataCatalogBuilder/MappingTest/shape/TargetPersonShape.html");
		
		Document doc = Jsoup.parse(htmlFile, "UTF-8");
		Element e = findResource(doc, "http://schema.org/givenName");
		assertTrue(e != null);
		Element mappedField = findProperty(e, "konig:mappedField");
		assertTrue(mappedField != null);
		
		Element div = mappedField.getAllElements().first();
		assertTrue(div != null);
		assertEquals("<gs://person-staging>.first_name", div.text());
	}


	private CompositeSourceNodeSelector nodeSelector(ShapeManager shapeManager) {
		return new CompositeSourceNodeSelector(
				new RawCubeSourceNodeSelector(shapeManager),
				new GoogleStorageBucketSourceNodeSelector(shapeManager),
				new ExplicitDerivedFromSelector());
	}

	private Element findProperty(Element parent, String propertyId) {
		for (Element e : parent.getAllElements()) {
			String value = e.attr("property");
			if (propertyId.equals(value)) {
				return e;
			}
		}
		return null;
	}

	private Element findResource(Document doc, String resourceId) {
		Elements list = doc.getAllElements();
		for (Element e : list) {
			String value = e.attr("resource");
			if (resourceId.equals(value)) {
				return e;
			}
		}
		return null;
	}

	@Test
	public void testSubjectArea() throws Exception {
		GcpShapeConfig.init();
    	AwsShapeConfig.init();
		URI ontologyId = uri("http://schema.org/");
		build("src/test/resources/SubjectAreaTest", ontologyId);
		
		File ontologyIndexFile = new File("target/test/DataCatalogBuilder/SubjectAreaTest/ontology-index.html");
		
		Document doc = Jsoup.parse(ontologyIndexFile, "UTF-8");
		Element subjectList = doc.getElementById("subjectList");
		assertTrue(subjectList != null);
		Elements subjectElements = subjectList.children();
		assertEquals(4, subjectElements.size());
	}

	@Test
	public void testDatasourceSummary() throws Exception {
		GcpShapeConfig.init();
    	AwsShapeConfig.init();
		URI ontologyId = uri("http://schema.org/");
		build("src/test/resources/DatasourceSummary", ontologyId);
	}

	@Test
	public void testShape() throws Exception {
		URI ontologyId = uri("http://schema.pearson.com/ns/fact/");
		build("src/test/resources/ShapePageTest/rdf", ontologyId);
	}

	@Test
	public void testHierarchicalNames() throws Exception {
		URI ontologyId = uri("http://example.com/ns/categories/");
		build("src/test/resources/DataCatalogBuilderTest/hierarchicalNames", ontologyId);
	}
	
	@Test
	public void testEnumNamespace() throws Exception {
		URI ontologyId = uri("http://example.com/");
		build("src/test/resources/EnumNamespaceTest", ontologyId);
	}
	
	private void build(String sourcePath, URI ontologyId) throws Exception {

		File file = new File(sourcePath);
		File targetDir = new File(outDir, file.getName());
		if (targetDir.exists()) {
			try {
				FileUtils.deleteDirectory(targetDir);
			} catch (Throwable e) {
				logger.warn("Failed to delete directory: {}", targetDir.getPath());
			}
		}
		load(file);
		if (showlManager != null) {
			showlManager.load();
		}
		DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		request.setExampleDir(exampleDir);
		request.setOntologyId(ontologyId);
		request.setGraph(graph);
		request.setOutDir(targetDir);
		request.setShapeManager(shapeManager);
		
		builder.build(request);
	}



	private void load(File file) throws Exception {
		RdfUtil.loadTurtle(file, graph, nsManager);
		
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	

}
