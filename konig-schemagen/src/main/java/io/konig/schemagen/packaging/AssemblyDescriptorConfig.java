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


import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AssemblyDescriptorConfig {
	
	private String packageId;
	private String sourcePath;
	private File descriptorFile;
	private List<MavenFileSet> fileSetList = new ArrayList<>();
	
	public AssemblyDescriptorConfig(String packageId, String sourcePath, File descriptorFile) {
		this.packageId = packageId;
		this.sourcePath = sourcePath;
		this.descriptorFile = descriptorFile;
	}
	
	public void addFileSet(MavenFileSet fileSet) {
		fileSetList.add(fileSet);
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public File getDescriptorFile() {
		return descriptorFile;
	}

	public List<MavenFileSet> getFileSetList() {
		return fileSetList;
	}

	public String getPackageId() {
		return packageId;
	}

	

}
