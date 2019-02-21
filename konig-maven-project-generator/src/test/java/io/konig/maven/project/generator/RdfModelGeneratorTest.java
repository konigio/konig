package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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


import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import io.konig.maven.FileUtil;
import io.konig.maven.WorkbookProcessorConfig;

public class RdfModelGeneratorTest {

	@Test
	public void test() throws Exception {
		
		FileUtil.deleteDir("target/test/rdf-model");
		
		File workbookFile = new File("src/test/resources/rdf-model/bigquery-table.xlsx");
		
		MavenProjectConfig project = new MavenProjectConfig();
		project.setBaseDir(new File("target/test/rdf-model"));
		project.setGroupId("com.example");
		project.setArtifactId("demo");
		project.setVersion("1.0.0");
		project.setName("Demo");
		project.setKonigVersion("2.0.0-8");
		project.setRdfSourceDir(new File("target/generated/rdf-model"));
		project.setRdfSourcePath("target/generated/rdf-model");
		
		WorkbookProcessorConfig workbook = new WorkbookProcessorConfig();
		workbook.setWorkbookFile(workbookFile);
		
		RdfModelGenerator generator = new RdfModelGenerator(project, workbook);
		
		generator.run();
		
		File pom = new File("target/test/rdf-model/demo-rdf-model/pom.xml");
		File assembly = new File("target/test/rdf-model/demo-rdf-model/src/assembly/dep.xml");
		
		assertTrue(pom.exists());
		assertTrue(assembly.exists());
	}
	

}
