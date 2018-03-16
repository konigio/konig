package io.konig.maven.project.generator;

import java.io.File;

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


import java.io.IOException;

import io.konig.maven.AmazonWebServicesConfig;

public class AwsModelGenerator  extends ConfigurableProjectGenerator<AmazonWebServicesConfig> {

	public AwsModelGenerator(MavenProjectConfig mavenProject, AmazonWebServicesConfig config) {
		super(config, "amazonWebServices");
		setTemplatePath("konig/generator/awsModel/pom.xml");
		setArtifactSuffix("-aws-model");
		setNameSuffix("Aws Model");
		config.setDirectory(new File("${project.basedir}/target/generated/aws"));
		config.setTables(new File("${project.basedir}/target/generated/aws/tables"));
		config.setAwsScriptFile(new File("${project.basedir}/target/generated/aws/scripts/deploy.groovy"));
		init(mavenProject);
	}
	
	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	}
}