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

import io.konig.maven.OracleManagedCloudConfig;

public class OracleManagedCloudProjectGenerator  extends ConfigurableProjectGenerator<OracleManagedCloudConfig> {

	public OracleManagedCloudProjectGenerator(MavenProjectConfig mavenProject, OracleManagedCloudConfig config) {
		super(config, "oracleManagedCloud");
		setTemplatePath("konig/generator/oracleManagedCloud/pom.xml");
		setArtifactSuffix("-omcs-model");
		setNameSuffix("Oracle Managed Cloud");
		init(mavenProject);
		
		config.setDirectory(new File("${project.basedir}/target/generated/omcs"));
		config.setTables(new File("${project.basedir}/target/generated/omcs/tables"));
		
	}
	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	}
}
