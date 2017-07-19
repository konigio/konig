package io.konig.maven.project.generator;

import java.io.File;

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

import java.io.IOException;

import org.apache.velocity.VelocityContext;

import io.konig.maven.datacatalog.ContentSystemConfig;
import io.konig.maven.datacatalog.DataCatalogConfig;

public class DataCatalogProjectGenerator extends ConfigurableProjectGenerator<DataCatalogConfig> {

	private ContentSystemConfig contentSystem;
	
	public DataCatalogProjectGenerator(MavenProjectConfig mavenProject, DataCatalogConfig config) {
		super(config, "dataCatalog");
		setTemplatePath("konig/generator/dataCatalog/pom.xml");
		setArtifactSuffix("-data-catalog");
		setNameSuffix("Data Catalog");
		init(mavenProject);
		
		mavenProject = getMavenProject();
		String rdfPath = mavenProject.getRdfSourcePath();
		
		config.setRdfDir(rdfPath);
		config.setSiteDir("${basedir}/target/generated/data-catalog");
		contentSystem = config.getContentSystem();
		config.setContentSystem(null);
		if (contentSystem != null) {
			contentSystem.setBaseDir("${basedir}/target/generated/data-catalog");
		}
		
	}

	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	}
	
	protected VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		putProperties(4, getTagName(), config);
		if (contentSystem != null) {
			putProperties(7, "contentSystem", contentSystem);
		}
		return context;
	}

}
