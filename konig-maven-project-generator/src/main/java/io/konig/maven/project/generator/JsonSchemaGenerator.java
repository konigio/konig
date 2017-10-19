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
import io.konig.maven.JsonSchemaConfig;

public class JsonSchemaGenerator extends ConfigurableProjectGenerator<JsonSchemaConfig> {
	
	
	public JsonSchemaGenerator(MavenProjectConfig mavenProject, JsonSchemaConfig jsonSchemaConfig) {
		super(jsonSchemaConfig, "jsonSchema");
		setTemplatePath("konig/generator/jsonSchema/pom.xml");
		setArtifactSuffix("-json-schema");
		setNameSuffix("JSON Schema");
		
		init(mavenProject);

		if (config.getJsonSchemaDir() == null) {
			
			config.setJsonSchemaDir(new File("target/generated/json-schema"));
		}
	}


	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	}
}
