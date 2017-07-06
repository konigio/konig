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

import io.konig.schemagen.maven.WorkbookProcessor;

public class ParentProjectGeneratorTest {

	@Test
	public void test() throws Exception {

		File baseDir = new File("target/test/demo");
		FileUtil.delete(baseDir);
		
		File workbookFile = new File("src/test/resources/rdf-model/bigquery-table.xlsx");
		
		MavenProjectConfig project = new MavenProjectConfig();
		project.setBaseDir(baseDir);
		project.setGroupId("com.example");
		project.setArtifactId("demo");
		project.setVersion("1.0.0");
		project.setName("Demo");
		project.setKonigVersion("2.0.0-8");
		
		WorkbookProcessor workbook = new WorkbookProcessor();
		workbook.setWorkbookFile(workbookFile);
		ParentProjectGenerator parent = new ParentProjectGenerator(project);
		RdfModelGenerator rdfModel = new RdfModelGenerator(project, workbook);
		
		parent.add(rdfModel);
		
		parent.run();
		
		assertTrue(project != null);
	}

}
