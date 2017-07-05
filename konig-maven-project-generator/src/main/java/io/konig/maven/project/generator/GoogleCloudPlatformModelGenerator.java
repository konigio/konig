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


import java.io.IOException;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;

import io.konig.schemagen.maven.GoogleCloudPlatformConfig;
import io.konig.schemagen.maven.RdfConfig;

public class GoogleCloudPlatformModelGenerator extends MavenProjectGenerator {
	
	private GoogleCloudPlatformConfig gcp;

	public GoogleCloudPlatformModelGenerator(
		GoogleCloudPlatformConfig config
	) {
		this.gcp = config;

		setTemplatePath("konig/generator/gcpModel/pom.xml");
		setArtifactSuffix("-gcp-model");
		setNameSuffix("Google Cloud Platform Model");
	}
	

	@Override
	public VelocityContext createVelocityContext() {
		VelocityContext context = super.createVelocityContext();
		StringWriter buffer = new StringWriter();
		XmlSerializer xml = new XmlSerializer(buffer);
		xml.setIndent(5);
		xml.setIndentWidth(2);
		
		xml.indent();
		xml.write(gcp, "googleCloudPlatform");
		xml.flush();
		context.put("gcpConfig", buffer.toString());
		
		return context;
	}
	


	@Override
	public void run() throws MavenProjectGeneratorException, IOException {
		super.run();
		copyAssembly();
	}
}
