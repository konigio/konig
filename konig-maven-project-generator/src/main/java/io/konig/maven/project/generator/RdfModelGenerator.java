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
		
		File workbookDir = config.getWorkbookDir();
		File workbookFile = config.getWorkbookFile();
		config.setWorkbookFile(localWorkbookFile());
		config.setWorkbookDir(localWorkbookDir());
		super.run();
		config.setWorkbookFile(workbookFile);
		config.setWorkbookDir(workbookDir);
		copyAssembly();
		copyWorkbooks();
		
		
	}

	private File localWorkbookDir() {
		File dir = config.getWorkbookDir();
		return dir==null ? null : new File("src/workbooks");
	}


	private File localWorkbookFile() {
		
		File file = config.getWorkbookFile();
		
		return file==null ? null : new File("src/" + file.getName());
	}


	private void copyWorkbooks() throws MavenProjectGeneratorException, IOException {
		
		File workbookFile = config.getWorkbookFile();
		if (workbookFile != null) {
		
			File targetFile = new File(baseDir(), "src/" + workbookFile.getName());
			targetFile.getParentFile().mkdirs();
			
			FileUtil.copy(config.getWorkbookFile(), targetFile);
		} else {
			File dir = config.getWorkbookDir();
			if (dir == null) {
				throw new MavenProjectGeneratorException("Either workbookFile or workbookDir must be defined");
			}
			
			File targetDir = new File(baseDir(), "src/workbooks");
			targetDir.mkdirs();
			
			File[] list = dir.listFiles();
			for (File file : list) {
				File target = new File(targetDir, file.getName());
				FileUtil.copy(file, target);
			}
		}
	}

	
}
