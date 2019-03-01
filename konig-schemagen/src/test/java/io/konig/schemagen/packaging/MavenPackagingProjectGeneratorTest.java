package io.konig.schemagen.packaging;

/*
 * #%L
 * Konig Schema Generator
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import io.konig.maven.FileUtil;

public class MavenPackagingProjectGeneratorTest {
	

	@Test
	public void test() throws Exception {
		File source = new File("src/test/resources/MavenPackagingProjectGeneratorTest");
		File basedir = new File("target/test/MavenPackagingProjectGeneratorTest");
		FileUtil.delete(basedir);
		FileUtil.copyDirectory(source, basedir);
		File logDir = new File("target/logs");
		MavenProject project = new MavenProject();
		project.setGroupId("io.example");
		project.setArtifactId("example-test");
		project.setName("Example Test");
		project.setVersion("1.0.0");
		
		PackagingProjectRequest request = new PackagingProjectRequest();
		request.setBasedir(basedir);
		request.setMavenProject(project);
		request.setVelocityLogFile(logDir);
		request.addPackage(new MavenPackage(ResourceKind.gcp)
			.include("$'{'basedir'}'/src/main/resources/env/{0}/gcp", "/gcp", "config.yaml"));
		
		MavenPackagingProjectGenerator generator = new MavenPackagingProjectGenerator();
		generator.generate(request);
		
		File pomFile = new File("target/test/MavenPackagingProjectGeneratorTest/target/deploy/pom.xml");
		
		assertTrue(pomFile.exists());
		
		String pom = FileUtil.readString(pomFile);

		assertTrue(pom.contains("<groupId>io.example</groupId>"));
		assertTrue(pom.contains("<artifactId>example-test</artifactId>"));
		assertTrue(pom.contains("<version>1.0.0</version>"));
		assertTrue(pom.contains("<name>Example Test</name>"));
		assertTrue(pom.contains("<id>zip-dev-gcp</id>"));
		assertTrue(pom.contains("<descriptor>${project.basedir}/src/assembly/dev-gcp.xml</descriptor>"));
		assertTrue(pom.contains("<id>zip-test-gcp</id>"));
		assertTrue(pom.contains("<descriptor>${project.basedir}/src/assembly/test-gcp.xml</descriptor>"));
		
		File devAssemblyFile = new File("target/test/MavenPackagingProjectGeneratorTest/target/deploy/src/assembly/dev-gcp.xml");
		
		assertTrue(devAssemblyFile.exists());
		String devAssembly = FileUtil.readString(devAssemblyFile);
//		System.out.println(devAssembly);
		assertTrue(devAssembly.contains("<directory>${basedir}/src/main/resources/env/dev/gcp</directory>"));
		assertTrue(devAssembly.contains("<outputDirectory>/gcp</outputDirectory>"));
		assertTrue(devAssembly.contains("<outputDirectory>/gcp</outputDirectory>"));
		assertTrue(devAssembly.contains("<include>config.yaml</include>"));
	}

}
