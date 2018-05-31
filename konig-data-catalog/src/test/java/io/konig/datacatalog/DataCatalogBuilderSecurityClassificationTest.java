package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class DataCatalogBuilderSecurityClassificationTest {
	private DataCatalogBuilder builder = new DataCatalogBuilder();

	private File exampleDir = new File("src/test/resources/DataCatalogBuilder-sec-class/examples");
	private File outDir = new File("target/test/DataCatalogBuilder-sec-class");
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
	public void testShape() throws Exception {
		URI ontologyId = uri("https://schema.pearson.com/ns/dcl/");
		test("src/test/resources/SecurityClassification/rdf", ontologyId);
		assertNotNull(ontologyId);
	}

	
	private void test(String sourcePath, URI ontologyId) throws Exception {
		load(sourcePath);
		DataCatalogBuildRequest request = new DataCatalogBuildRequest();
		String HtmlContent ="";
		request.setExampleDir(exampleDir);
		request.setOntologyId(ontologyId);
		request.setGraph(graph);
		request.setOutDir(outDir);
		request.setShapeManager(shapeManager);
		
		builder.build(request);
		File dclFile = new File(outDir.getPath()+"/dcl");
		assertTrue(dclFile.isDirectory());
		File secFile = new File(outDir.getPath()+"/sec");
		assertTrue(secFile.isDirectory());
		File shape = new File(outDir.getPath()+"/shape");
		File [] fileArray = shape.listFiles();
		for (int i = 0; i < fileArray.length; i++)
	     {
	        if (fileArray[i].isFile() && fileArray[i].getName().contains("Shape")) 
	        {
	        	HtmlContent  = 	parseHtml(fileArray[i]);
	        }
	}
	       assertTrue(HtmlContent.contains("Public"));
	       assertTrue(HtmlContent.contains("DCL1"));
	       System.out.println("Assertion Value of Public: "+ HtmlContent.contains("Public"));
	       System.out.println("Assertion Value of DCL1: "+ HtmlContent.contains("DCL1"));
	}
	private String parseHtml(File file){
		StringBuilder contentBuilder = new StringBuilder();
		try {
		    BufferedReader in = new BufferedReader(new FileReader(file.getAbsolutePath()));
		    String str;
		    while ((str = in.readLine()) != null) {
		        contentBuilder.append(str);
		    }
		    in.close();
		} catch (IOException e) {
		}
		String content = contentBuilder.toString();
		return content;
	}

	private void load(String path) throws Exception {
		File file = new File(path);
		RdfUtil.loadTurtle(file, graph, nsManager);
		ShapeLoader loader = new ShapeLoader(shapeManager);
		loader.load(graph);
		
		
	}
	
	private URI uri(String value) {
		return new URIImpl(value);
	}
	
	

}
