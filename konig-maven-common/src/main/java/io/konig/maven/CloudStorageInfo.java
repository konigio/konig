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

import io.konig.yaml.Yaml;

public class CloudStorageInfo {
	
	@Parameter(property="konig.gcp.cloudstorage.directory", defaultValue="${konig.gcp.directory}/cloudstorage")
	private File directory;
	
	@Parameter(property="konig.gcp.cloudstorage.data", defaultValue="${konig.gcp.cloudstorage.directory}/data")
	private File data;

	@Parameter(property="konig.gcp.cloudstorage.bucketSuffix", defaultValue="dev")
	private String bucketSuffix;
	
	

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getBucketSuffix() {
		return bucketSuffix;
	}

	public void setBucketSuffix(String bucketSuffix) {
		this.bucketSuffix = bucketSuffix;
	}

	public String toString() {
		return Yaml.toString(this);
	}

	public File getData() {
		return data;
	}

	public void setData(File data) {
		this.data = data;
	}
	

}