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

import static org.junit.Assert.*;
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

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.gcp.datasource.GcpShapeConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class DataCatalogBuilderTest {
	
	private DataCatalogBuilder builder = new DataCatalogBuilder();

	private File exampleDir = new File("src/test/resources/DataCatalogBuilder/examples");
	private File outDir = new File("target/test/DataCatalogBuilder");
	private NamespaceManager nsManager = new MemoryNamespaceManager();
	private Graph graph = new MemoryGraph(nsManager);
	private ShapeManager shapeManager = new MemoryShapeManager();
	
	@Before
	public void setUp() {
		if (outDir.exists()) {
			IOUtil.recursiveDelete(outDir);
		}
		outDir.mkdirs();
	}
	

	@Test
	public void testSubjectArea() throws Exception {
		GcpShapeConfig.init();
    	AwsShapeConfig.init();
		URI ontologyId = uri("http://schema.org/");
		test("src/test/resources/SubjectAreaTest", ontologyId);
		
		File ontologyIndexFile = new File("target/test/DataCatalogBuilder/ontology-index.html");
		
		Document doc = Jsoup.parse(ontologyIndexFile, "UTF-8");
		Element subjectList = doc.getElementById("subjectList");
		assertTrue(subjectList != null);
		Elements subjectElements = subjectList.children();
		assertEquals(4, subjectElements.size());
	}

	@Ignore
	public void testDatasourceSummary() throws Exception {
		GcpShapeConfig.init();
    	AwsShapeConfig.init();
		URI ontologyId = uri("http://schema.org/");
		test("src/test/resources/DatasourceSummary", ontologyId);
	}

	@Ignore
	public void testShape() throws Exception {
		URI ontologyId = uri("http://schema.pearson.com/ns/fact/");
		test("src/test/resources/ShapePageTest/rdf", ontologyId);
	}

	@Ignore
	public void testHierarchicalNames() throws Exception {
		URI ontologyId = uri("http://example.com/ns/categories/");
		test("src/test/resources/DataCatalogBuilderTest/hierarchicalNames", ontologyId);
	}
	
	@Ignore
	public void testEnumNamespace() throws Exception {
		URI ontologyId = uri("http://example.com/");
		test("src/test/resources/EnumNamespaceTest", ontologyId);
	}
	
	private void test(String sourcePath, URI ontologyId) throws Exception {
		FileUtils.deleteDirectory(outDir);
		load(sourcePath);
		DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		request.setExampleDir(exampleDir);
		request.setOntologyId(ontologyId);
		request.setGraph(graph);
		request.setOutDir(outDir);
		request.setShapeManager(shapeManager);
		
		builder.build(request);
	}

	@Ignore
	public void test() throws Exception {
		
		load("src/test/resources/DataCatalogBuilderTest/rdf");
		URI ontologyId = uri("http://example.com/ns/core/");
		
		DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		request.setExampleDir(exampleDir);
		request.setGraph(graph);
		request.setOntologyId(ontologyId);
		request.setOutDir(outDir);
		request.setShapeManager(shapeManager);
		
		builder.build(request);
		
	}


	private void load(String path) throws Exception {
		File file = new File(path);
		System.out.println(file.getAbsolutePath());
		RdfUtil.loadTurtle(file, graph, nsManager);
		
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	

}
