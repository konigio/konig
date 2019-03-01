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


import java.util.ArrayList;
import java.util.List;

public class FileInclude {
	
	private String sourceDirPattern;
	private String targetPath;
	private List<String> includeList;

	public FileInclude(String sourceDirPattern, String targetPath) {
		this.sourceDirPattern = sourceDirPattern;
		this.targetPath = targetPath;
	}

	/**
	 * A MessageFormat pattern for the source directory that is to be included.
	 * The pattern must include "{0}" as a placeholder for the environment name.
	 * The 
	 * For example,
	 * <pre>
	 *    ${project.basedir}/src/main/resources/env/{0}/gcp
	 * </pre>
	 * @return
	 */
	public String getSourceDirPattern() {
		return sourceDirPattern;
	}

	public String getTargetPath() {
		return targetPath;
	}
	
	public void addInclude(String include) {
		if (includeList == null) {
			includeList = new ArrayList<>();
		}
		includeList.add(include);
	}

	public List<String> getIncludeList() {
		return includeList;
	}
	
	

}
