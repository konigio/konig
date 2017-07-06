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

import io.konig.schemagen.maven.JavaCodeGeneratorConfig;

public class JavaModelGenerator extends ConfigurableProjectGenerator<JavaCodeGeneratorConfig> {
	
	
	public JavaModelGenerator(MavenProjectConfig mavenProject, JavaCodeGeneratorConfig javaConfig) {
		super(javaConfig, "java");
		setTemplatePath("konig/generator/javaModel/pom.xml");
		setArtifactSuffix("-java-model");
		setNameSuffix("Java Model");
		
		init(mavenProject);

		if (config.getJavaDir() == null) {
			config.setJavaDir(new File(baseDir(), "src/main/java"));
		}
		if (config.getPackageRoot() == null) {
			config.setPackageRoot(mavenProject.getGroupId());
		}
	}

	
}
