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

public class CloudSqlInfo {

	@Parameter(property="konig.gcp.cloudsql.directory", defaultValue="${konig.gcp.directory}/cloudsql")
	private File directory;

	@Parameter(property="konig.gcp.cloudsql.instances", defaultValue="${konig.gcp.cloudsql.directory}/instances")
	private File instances;

	@Parameter(property="konig.gcp.cloudsql.databases", defaultValue="${konig.gcp.cloudsql.directory}/databases")
	private File databases;

	@Parameter(property="konig.gcp.cloudsql.tables", defaultValue="${konig.gcp.cloudsql.directory}/tables")
	private File tables;
	
	@Parameter(property="konig.gcp.cloudsql.scripts", defaultValue="${konig.gcp.cloudsql.directory}/scripts")
	private File scripts;
	@Parameter(property="konig.gcp.cloudsql.shapeIriPattern")
	private String shapeIriPattern;
	@Parameter(property="konig.gcp.cloudsql.shapeIriReplacement")
	private String shapeIriReplacement;
	@Parameter(property="konig.aws.cloudsql.propertyNameSpace")
	private String propertyNameSpace;

	
	@Parameter(property="konig.gcp.cloudsql.transformScope")
	private TransformProcessingScope transformScope;
	
	public CloudSqlInfo() {
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

	public void setTables(File schema) {
		this.tables = schema;
	}

	public File getInstances() {
		return instances;
	}

	public void setInstances(File instances) {
		this.instances = instances;
	}

	public File getDatabases() {
		return databases;
	}

	public void setDatabases(File databases) {
		this.databases = databases;
	}
	
	public void setScripts(File scripts) {
		this.scripts = scripts;
	}
	
	public File getScripts() {
		return scripts;
	}

	public String getShapeIriPattern() {
		return shapeIriPattern;
	}

	public void setShapeIriPattern(String shapeIriPattern) {
		this.shapeIriPattern = shapeIriPattern;
	}

	public String getShapeIriReplacement() {
		return shapeIriReplacement;
	}

	public void setShapeIriReplacement(String shapeIriReplacement) {
		this.shapeIriReplacement = shapeIriReplacement;
	}
	public String getPropertyNameSpace() {
		return propertyNameSpace;
	}

	public void setPropertyNameSpace(String propertyNameSpace) {
		this.propertyNameSpace = propertyNameSpace;
	}

	public TransformProcessingScope getTransformScope() {
		return transformScope;
	}

	public void setTransformScope(TransformProcessingScope transformScope) {
		this.transformScope = transformScope;
	}
	
}
