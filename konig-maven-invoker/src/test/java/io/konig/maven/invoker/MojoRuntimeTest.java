package io.konig.maven.invoker;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileWriter;

import org.junit.Test;

public class MojoRuntimeTest {

	@Test
	public void test() throws Exception {
		
		File basedir = new File("target/test/MojoRuntimeTest/copy-file");
		File resourceFile = new File(basedir,"src/hello.txt");
		
		resourceFile.getParentFile().mkdirs();
		
		try (FileWriter writer = new FileWriter(resourceFile)) {
			writer.write("Hello world!");
		}
		
		
		MojoRuntime.builder()
			.basedir(basedir)
			.groupId("org.apache.maven.plugins")
			.artifactId("maven-resources-plugin")
			.version("3.1.0")
			.goal("copy-resources")
			.beginConfiguration()
				.property("outputDirectory", new File(basedir, "destination"))
				.begin("resources")
					.begin("resource")
						.property("directory", resourceFile.getParentFile())
					.end("resource")
				.end("resources")
			.endConfiguration()
			.build().execute();
			
		
		fail("Not yet implemented");
	}

}
