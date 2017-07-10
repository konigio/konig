package io.konig.content;

/*
 * #%L
 * Konig Content System, Shared Library
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


public class AssetBundleKey {

	private String name;
	private String version;
	
	public AssetBundleKey(String name, String version) {
		this.name = ContentSystemUtil.trimSlashes(name);
		this.version = ContentSystemUtil.trimSlashes(version);
	}
	
	public String getName() {
		return name;
	}
	public String getVersion() {
		return version;
	}
	
	public String url(String baseURL) {
		StringBuilder builder = new StringBuilder();
		builder.append(baseURL);
		if (!baseURL.endsWith("/")) {
			builder.append('/');
		}
		builder.append(name);
		builder.append('/');
		builder.append(version);
		builder.append('/');
		return builder.toString();
	}
	
	public String assetURL(String baseURL, AssetMetadata metadata) {

		StringBuilder builder = new StringBuilder();
		builder.append(baseURL);
		if (!baseURL.endsWith("/")) {
			builder.append('/');
		}
		String path = metadata.getPath();
		builder.append(name);
		builder.append('/');
		builder.append(version);
		if (!path.startsWith("/")) {
			builder.append('/');
		}
		builder.append(metadata.getPath());
		return builder.toString();
	}
	
	public String trimBundlePath(String path) {
		int bundleNameEnd = path.indexOf('/', 1);
		int versionEnd = path.indexOf('/', bundleNameEnd+1);
		return path.substring(versionEnd+1);
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append(':');
		builder.append(version);
		return builder.toString();
	}
	
}
