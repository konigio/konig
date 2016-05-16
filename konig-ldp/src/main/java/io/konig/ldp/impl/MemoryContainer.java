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


import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import io.konig.ldp.BasicContainer;
import io.konig.ldp.Container;
import io.konig.ldp.ResourceFile;
import io.konig.ldp.ResourceHandler;
import io.konig.ldp.ResourceType;

public class MemoryContainer extends RdfSourceImpl implements BasicContainer, ResourceHandler {
	
	private Map<String, ResourceFile> map = new LinkedHashMap<>();

	public MemoryContainer(String contentLocation, String contentType, ResourceType type, byte[] body) {
		super(contentLocation, contentType, type, body);
	}

	@Override
	public void add(ResourceFile resource) {
		map.put(resource.getContentLocation(), resource);
	}

	@Override
	public Iterable<ResourceFile> getMembers() {
		return map.values();
	}

	@Override
	public Iterable<String> getMemberIds() {
		return map.keySet();
	}

	@Override
	public void handle(ResourceFile resource) throws IOException {
		add(resource);
	}

	@Override
	public void remove(String resourceId) throws IOException {
		map.remove(resourceId);
		
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public Container asContainer() {
		return (Container) this;
	}
}
