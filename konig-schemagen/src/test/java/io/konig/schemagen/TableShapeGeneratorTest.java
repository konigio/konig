package io.konig.schemagen;

/*
 * #%L
 * Konig Schema Generator
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


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.model.FileSet;
import org.junit.Test;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.aws.datasource.AwsShapeConfig;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.impl.MemoryGraph;
import io.konig.core.impl.MemoryNamespaceManager;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.IOUtil;
import io.konig.maven.TableShapeGeneratorConfig;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.impl.MemoryShapeManager;
import io.konig.shacl.io.ShapeLoader;

public class TableShapeGeneratorTest {

	@Test
	public void test() throws Exception {
		List<String> inputDataSourceList = new ArrayList<String>();
		inputDataSourceList.add("AwsAuroraTable");
		AwsShapeConfig.init();
		TableShapeGeneratorConfig tableConfig = new TableShapeGeneratorConfig();
		tableConfig.setPropertyNamespace("http://schema.org/");
		FileSet[] tableFiles = new FileSet[1];
		FileSet fileset = new FileSet();
		fileset.setDirectory("src/test/resources/table-shape-generator/table");
		tableFiles[0] = fileset;
		tableConfig.setTableFiles(tableFiles);
		
		NamespaceManager nsManager = new MemoryNamespaceManager();
		nsManager.add("schema", "http://schema.org/");
		nsManager.add("shape", "http://example.com/shapes/");
		nsManager.add("sh", "http://www.w3.org/ns/shacl#");
		nsManager.add("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		nsManager.add("konig", "http://www.konig.io/ns/core/");
		nsManager.add("xsd", "http://www.w3.org/2001/XMLSchema#");
		
		File outDir = new File("target/test/table-shape-generator");
		TableShapeGenerator tableShapeGenerator = new TableShapeGenerator(nsManager, tableConfig);	
		tableShapeGenerator.generateTable(outDir);
		
	}
	

}
