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

import io.konig.maven.DataServicesConfig;
import io.konig.maven.FileUtil;
import io.konig.maven.GoogleCloudPlatformConfig;

public class AppEngineGenerator extends ConfigurableProjectGenerator<GoogleCloudPlatformConfig> {

	private static final String ARTIFACT_SUFFIX = "-appengine";

	public AppEngineGenerator(
		MavenProjectConfig mavenProject,
		GoogleCloudPlatformConfig config
	) {
		super(config, "googleCloudPlatform");

		setTemplatePath("konig/generator/appEngine/pom.xml");
		setArtifactSuffix(ARTIFACT_SUFFIX);
		setNameSuffix("Data Services powered by App Engine");
		init(mavenProject);
		
		
		DataServicesConfig services = config.getDataServices();
		mavenProject = getMavenProject();
		File basedir = mavenProject.getBaseDir();
		File infoFile = services.getInfoFile();
		if (infoFile == null) {
			File file = new File(DataServicesConfig.INFO_FILE_PATH);
			if (file.exists()) {
				infoFile = file;
			}
		}
		if (infoFile != null) {
			basedir.mkdirs();
			String path = FileUtil.relativePath(basedir, infoFile);
			services.setInfoFile(new File("${project.basedir}/" + path));
		}
		services.setOpenApiFile(new File("${project.basedir}/src/main/webapp/openapi.yaml"));
		services.setBasedir(new File("${project.basedir}"));
		services.setWebappDir(new File("${project.basedir}/src/main/webapp"));
	}

}
