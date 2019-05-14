package io.konig.maven;

/*
 * #%L
 * koing-gcp-maven-plugin
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


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@Mojo(name = "generate")
public final class KonigGcpMojo extends AbstractMojo {

	@Component
	private MavenProject mavenProject;

	public void execute() throws MojoExecutionException {
		String fileName = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
		File jsonKey = new File(fileName);
		String projectId;
		try {
			projectId = readProjectId(jsonKey);
		} catch (IOException e) {
			throw new MojoExecutionException("Invalid JSON key file: " + jsonKey);
		}
		
		Properties projectProps = mavenProject.getProperties();
		copyProperty("gcpProjectId", projectId, projectProps);

	}

	private void copyProperty(String name, String value, Properties projectProps) {
		projectProps.setProperty(name, value.toString());
	}

	private String readProjectId(File jsonKey) throws MojoExecutionException, FileNotFoundException, IOException {
		try (FileInputStream input = new FileInputStream(jsonKey)) {

			ObjectMapper mapper = new ObjectMapper();
			JsonNode node = mapper.reader().readTree(input);
			if (node instanceof ObjectNode) {
				ObjectNode obj = (ObjectNode) node;
				node = obj.get("project_id");
				if (node != null) {
					return node.asText();
				}
			}
		}

		throw new MojoExecutionException("Invalid JSON key file: " + jsonKey);
	}

}
