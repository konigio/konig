package io.konig.maven.project.generator;

import io.konig.schemagen.maven.GoogleCloudPlatformConfig;

public class GoogleCloudPlatformModelGenerator extends MavenProjectGenerator {
	
	GoogleCloudPlatformConfig config;

	public GoogleCloudPlatformModelGenerator(GoogleCloudPlatformConfig config) {
		this.config = config;

		setTemplatePath("konig/generator/gcpModel/pom.xml");
		setArtifactSuffix("-gcp-model");
		setNameSuffix("Google Cloud Platform Model");
	}
}
