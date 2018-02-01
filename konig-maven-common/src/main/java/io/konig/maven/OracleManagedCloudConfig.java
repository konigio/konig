package io.konig.maven;

/*
 * #%L
 * Konig Maven Common
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


public class OracleManagedCloudConfig {
	
	@Parameter(property="konig.omcs.directory", defaultValue="${project.basedir}/target/generated/omcs")
	private File directory;
	
	@Parameter(property="konig.omcs.tables", defaultValue="${konig.omcs.directory}/tables")
	private File tables;
	
	@Parameter(property="konig.omcs.deployment.script.file", defaultValue="${konig.omcs.directory}/scripts/deploy.groovy")
	private File omcsScriptFile;

	@Parameter(property="konig.omcs.deployment.version", defaultValue="${konig.version}")
	private String konigVersion;

	
	public OracleManagedCloudConfig() {
			
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}
	
	public File getTables() {
		return tables;
	}

	public void setTables(File tables) {
		this.tables = tables;
	}
	
	public String getKonigVersion() {
		return konigVersion;
	}

	public void setKonigVersion(String konigVersion) {
		this.konigVersion = konigVersion;
	}

	public File getOmcsScriptFile() {
		return omcsScriptFile;
	}

	public void setOmcsScriptFile(File omcsScriptFile) {
		this.omcsScriptFile = omcsScriptFile;
	}
}
