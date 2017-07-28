package io.konig.maven;

/*
 * #%L
 * Konig Google Cloud Platform Deployment Model
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

public class GroovyDeploymentScript {

	@Parameter(property="konig.gcp.deployment.script.file", defaultValue="${konig.gcp.directory}/scripts/deploy.groovy")
	private File scriptFile;
	
	@Parameter(property="konig.gcp.deployment.version", defaultValue="${konig.version}")
	private String konigVersion;

	public File getScriptFile() {
		return scriptFile;
	}

	public void setScriptFile(File scriptFile) {
		this.scriptFile = scriptFile;
	}

	public String getKonigVersion() {
		return konigVersion;
	}

	public void setKonigVersion(String konigVersion) {
		this.konigVersion = konigVersion;
	}
	
	

	
}
