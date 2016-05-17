package io.konig.ldp.impl;

/*
 * #%L
 * Konig Linked Data Platform
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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


import io.konig.ldp.BasicContainer;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceBuilder;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;

public class SimpleResourceBuilder implements ResourceBuilder {

	private String contentLocation;
	private String contentType;
	private ResourceType type;
	private byte[] body;

	@Override
	public ResourceBuilder type(ResourceType type) {
		this.type = type;
		return this;
	}

	@Override
	public ResourceBuilder contentType(String contentType) {
		this.contentType = contentType;
		return this;
	}

	@Override
	public ResourceBuilder contentLocation(String contentLocation) {
		this.contentLocation = contentLocation;
		return this;
	}

	@Override
	public ResourceBuilder entityBody(byte[] body) {
		this.body = body;
		return this;
	}

	@Override
	public ResourceBuilder body(String body) {
		this.body = body.getBytes();
		return this;
	}

	@Override
	public ResourceFile resource() {
		if (type == ResourceType.BasicContainer) {
			return createBasicContainer(contentLocation, contentType, type, body);
		}
		if (type == ResourceType.RDFSource) {
			return new RdfSourceImpl(contentLocation, contentType, type, body);
		}
		

		return new ResourceFileImpl(contentLocation, contentType, type, body);
	}
	
	protected BasicContainer createBasicContainer(String contentLocation, String contentType, ResourceType type, byte[] body) {
		return new BasicMemoryContainer(contentLocation, contentType, type, body);
	}

	@Override
	public BasicContainer basicContainer() {
		type = ResourceType.BasicContainer;
		return (BasicContainer) resource();
	}

	@Override
	public RdfSource rdfSource() {
		type = ResourceType.RDFSource;
		return (RdfSource) resource();
	}

}
