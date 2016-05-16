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
import io.konig.ldp.Container;
import io.konig.ldp.RdfSource;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceType;

public class ResourceFileImpl implements ResourceFile {
	
	protected String contentLocation;
	protected String contentType;
	protected ResourceType type;
	protected byte[] body;
	
	public ResourceFileImpl(String contentLocation, String contentType, ResourceType type, byte[] body) {
		this.contentLocation = contentLocation;
		this.contentType = contentType;
		this.type = type;
		this.body = body;
	}

	@Override
	public String getContentLocation() {
		return contentLocation;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public byte[] getEntityBody() {
		return body;
	}

	@Override
	public ResourceType getType() {
		return type;
	}

	@Override
	public String getEntityBodyText() {
		return new String(body);
	}

	@Override
	public boolean isBasicContainer() {
		return false;
	}

	@Override
	public BasicContainer asBasicContainer() {
		throw new RuntimeException("Cannot convert to BasicContainer");
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public Container asContainer() {
		throw new RuntimeException("Cannot convert to BasicContainer");
	}


	@Override
	public void setEntityBody(byte[] body) {
		this.body = body;
	}

	@Override
	public boolean isRdfSource() {
		return false;
	}

	@Override
	public RdfSource asRdfSource() {
		throw new RuntimeException("Cannot convert to RdfSource");
	}
}
