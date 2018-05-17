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
public class AuroraInfo {
	@Parameter(property="konig.gcp.aurora.directory", defaultValue="${konig.aws.directory}/aurora")
	private File directory;
	@Parameter(property="konig.aws.aurora.tables", defaultValue="${konig.gcp.aurora.directory}/tables")
	private File tables;
	@Parameter(property="konig.aws.aurora.shapeIriPattern")
	private String shapeIriPattern;
	@Parameter(property="konig.aws.aurora.shapeIriReplacement")
	private String shapeIriReplacement;
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

	

	public File getTables() {
		return tables;
	}

	public void setTables(File tables) {
		this.tables = tables;
	}

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	


}
