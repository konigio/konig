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


public class AssetMetadata {

	private AssetBundleKey bundleKey;
	private String path;
	private String contentType;
	private String etag;
	
	
	public AssetBundleKey getBundleKey() {
		return bundleKey;
	}
	public void setBundleKey(AssetBundleKey bundleKey) {
		this.bundleKey = bundleKey;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = ContentSystemUtil.trimSlashes(path);
	}
	public String getContentType() {
		return contentType;
	}
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	public String getEtag() {
		return etag;
	}
	public void setEtag(String etag) {
		this.etag = etag;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{bundleKey: ");
		builder.append(bundleKey);
		builder.append(", contentType: ");
		builder.append(contentType);
		builder.append(", etag: ");
		builder.append(etag);
		builder.append(", path: ");
		builder.append(path);
		builder.append('}');
		return builder.toString();
	}
	
	
}
