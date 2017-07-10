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


public class Asset {
	
	private AssetMetadata metadata;
	private byte[] body;
	
	public Asset(AssetMetadata metadata, byte[] body) {
		this.metadata = metadata;
		this.body = body;
	}

	public AssetMetadata getMetadata() {
		return metadata;
	}

	public byte[] getBody() {
		return body;
	}
}
