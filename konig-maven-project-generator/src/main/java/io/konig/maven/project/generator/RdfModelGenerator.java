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
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.velocity.VelocityContext;

public class RdfModelGenerator extends MavenProjectGenerator {
	private File workbook;
	
	public RdfModelGenerator() {
		setTemplatePath("konig/generator/rdf-model/pom.xml");
		setArtifactSuffix("-rdf-model");
		setNameSuffix("RDF Model");
	}
	
	public File getWorkbook() {
		return workbook;
	}

	public void setWorkbook(File workbook) {
		this.workbook = workbook;
	}


	@Override
	protected VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		context.put("workbookFileName", workbook.getName());
		return context;
	}

	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		if (workbook == null) {
			throw new MavenProjectGeneratorException("workbook file must be defined");
		}
		super.run();
		copyAssembly();
		copyWorkbook();
	}

	private void copyWorkbook() throws MavenProjectGeneratorException, IOException {
		File targetFile = new File(baseDir(), "src/" + workbook.getName());
		targetFile.getParentFile().mkdirs();
		
		Path source = workbook.toPath();
		Path target = targetFile.toPath();
		Files.copy(source, target);
	}

	
}
