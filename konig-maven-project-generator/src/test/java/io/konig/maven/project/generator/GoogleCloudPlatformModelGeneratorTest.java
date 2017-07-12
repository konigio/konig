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


import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.konig.schemagen.maven.GoogleCloudPlatformConfig;

public class GoogleCloudPlatformModelGeneratorTest {

	@Test
	public void test() throws Exception {
		FileUtil.deleteDir("target/test/gcp-model");
		
		
		MavenProjectConfig project = new MavenProjectConfig();
		project.setBaseDir(new File("target/test/gcp-model"));
		project.setGroupId("com.example");
		project.setArtifactId("demo");
		project.setVersion("1.0.0");
		project.setName("Demo");
		project.setKonigVersion("2.0.0-8");
		project.setRdfSourceDir(new File("target/generated/rdf"));
		project.setRdfSourcePath("target/generated/rdf");
		
		GoogleCloudPlatformConfig gcpConfig = new GoogleCloudPlatformConfig();
		gcpConfig.setEnumShapeDir(new File("src/main/rdf/enumShapes"));
		GoogleCloudPlatformModelGenerator generator = new GoogleCloudPlatformModelGenerator(project, gcpConfig);
		
		generator.run();
		
		File pom = new File("target/test/gcp-model/demo-gcp-model/pom.xml");
		File assembly = new File("target/test/gcp-model/demo-gcp-model/src/assembly/dep.xml");
		
		assertTrue(pom.exists());
		assertTrue(assembly.exists());
	}

}
