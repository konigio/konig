package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
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

import org.apache.maven.project.MavenProject;

public class DataServicesConfig {
	public static final String INFO_FILE_PATH = "src/dataservices/openapi-info.yaml";
	
	@Parameter(property="konig.gcp.dataServices.basedir", defaultValue="../${konig.gcp.dataServices.artifactId}")
	private File basedir;
	
	@Parameter(property="konig.gcp.dataServices.infoFile", defaultValue="${project.basedir}/" + INFO_FILE_PATH)
	private File infoFile;
	
	@Parameter(property="konig.gcp.dataServices.openApiFile", defaultValue="${konig.gcp.dataServices.webappDir}/openapi.yaml")
	private File openApiFile;
	
	@Parameter(property="konig.gcp.dataServices.configFile", defaultValue="${konig.gcp.dataServices.webappDir}/WEB-INF/classes/app.yaml")
	private File configFile;
	
	@Parameter(property="konig.gcp.dataServices.webappDir", defaultValue="${konig.gcp.dataServices.basedir}/src/main/webapp")
	private File webappDir;
	
	@Parameter(property="konig.gcp.dataServices.artifactId", defaultValue="${project.artifactId}-appengine")
	private String artifactId;
	
	public DataServicesConfig() {
		
	}

	public File getBasedir() {
		return basedir;
	}

	public void setBasedir(File basedir) {
		this.basedir = basedir;
	}

	public File getInfoFile() {
		return infoFile;
	}

	public void setInfoFile(File infoFile) {
		this.infoFile = infoFile;
	}

	public File getOpenApiFile() {
		return openApiFile;
	}

	public void setOpenApiFile(File openApiFile) {
		this.openApiFile = openApiFile;
	}

	public File getConfigFile() {
		return configFile;
	}

	public void setConfigFile(File configFile) {
		this.configFile = configFile;
	}

	public File getWebappDir() {
		return webappDir;
	}

	public void setWebappDir(File webappDir) {
		this.webappDir = webappDir;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	
}
