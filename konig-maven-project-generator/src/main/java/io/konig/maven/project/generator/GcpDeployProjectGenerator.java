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
import java.io.InputStream;

import org.apache.velocity.VelocityContext;

import io.konig.maven.FileUtil;
import io.konig.maven.GoogleCloudPlatformConfig;

public class GcpDeployProjectGenerator extends ConfigurableProjectGenerator<GoogleCloudPlatformConfig> {

	private String artifactPrefix;
	
	public GcpDeployProjectGenerator(MavenProjectConfig mavenProject, GoogleCloudPlatformConfig config) {
		super(config, "gcp");

		artifactPrefix = mavenProject.getArtifactId();
		
		setTemplatePath("konig/generator/gcpDeploy/pom.xml");
		setArtifactSuffix("-gcp-deploy");
		setNameSuffix("Google Cloud Platform Deployment");
		init(mavenProject);
	}
	

	protected VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		context.put("gcpModelArtifactId", artifactPrefix + GoogleCloudPlatformModelGenerator.ARTIFACT_SUFFIX);
		return context;
	}

	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		if (config == null) {
			throw new MavenProjectGeneratorException("workbook file must be defined");
		}
		super.run();
		copyAssembly();
	}

	@Override
	protected void copyAssembly() throws MavenProjectGeneratorException, IOException {
		
			File targetFile = new File(baseDir(), "src/assembly/dep.xml");
			targetFile.getParentFile().mkdirs();
			
			InputStream source = getClass().getClassLoader().getResourceAsStream("konig/generator/gcpDeploy/src/assembly/dep.xml");
			FileUtil.copyAndCloseSource(source, targetFile);
			
	}

}
