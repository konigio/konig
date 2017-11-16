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


import java.io.File;
import java.io.IOException;

import io.konig.maven.FileUtil;
import io.konig.maven.WorkbookProcessor;

public class RdfModelGenerator extends ConfigurableProjectGenerator<WorkbookProcessor> {
	
	public RdfModelGenerator(MavenProjectConfig mavenProject, WorkbookProcessor workbook) {
		super(workbook, "workbook");
		setTemplatePath("konig/generator/rdf-model/pom.xml");
		setArtifactSuffix("-rdf-model");
		setNameSuffix("RDF Model");
		init(mavenProject);
	}


	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		if (config == null) {
			throw new MavenProjectGeneratorException("workbook file must be defined");
		}
		
		File workbookFile = config.getWorkbookFile();
		config.setWorkbookFile(localWorkbookFile());
		super.run();
		config.setWorkbookFile(workbookFile);
		copyAssembly();
		copyWorkbook();
		
		
	}

	private File localWorkbookFile() {
		return new File("src/" + config.getWorkbookFile().getName());
	}


	private void copyWorkbook() throws MavenProjectGeneratorException, IOException {
		File targetFile = new File(baseDir(), "src/" + config.getWorkbookFile().getName());
		targetFile.getParentFile().mkdirs();
		
		FileUtil.copy(config.getWorkbookFile(), targetFile);
	}

	
}
