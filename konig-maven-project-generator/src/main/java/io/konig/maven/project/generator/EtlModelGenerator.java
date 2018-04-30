package io.konig.maven.project.generator;

/*
 * #%L
 * Konig Maven Project Generator
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


public class EtlModelGenerator extends MavenProjectGenerator {

private String artifactName;
private File baseDir;

	public File getBaseDir() {
	return baseDir;
}

public void setBaseDir(File baseDir) {
	this.baseDir = baseDir;
}

	public String getArtifactName() {
	return artifactName;
}

public void setArtifactName(String artifactName) {
	this.artifactName = artifactName;
}

	public EtlModelGenerator(MavenProjectConfig mavenProject,File baseDir,String artifactName) {	
		mavenProject.setName(artifactName);
		setTemplatePath("konig/generator/ETL/pom.xml");
		mavenProject.setArtifactId("etl-");
		setArtifactSuffix(artifactName.toLowerCase());		
		setNameSuffix("ETL Model");
		mavenProject.setBaseDir(baseDir);
		init(mavenProject);
	}
	
	public EtlModelGenerator() {
		
		}

	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	
	}
	
	

}
