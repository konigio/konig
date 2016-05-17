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
import java.util.HashMap;
import java.util.Map;

import io.konig.ldp.HttpStatusCode;
import io.konig.ldp.LdpResponse;
import io.konig.ldp.ResourceBuilder;
import io.konig.ldp.ResourceFile;

public class MemoryPlatform extends AbstractPlatform {

	private Map<String, ResourceFile> map = new HashMap<>();
	
	public MemoryPlatform(String root) {
		super(root);
		
	}
	@Override
	public ResourceFile get(String resourceIRI) throws IOException {
		return map.get(resourceIRI);
	}
	
	@Override
	protected int save(ResourceFile resource) throws IOException {
		
		int status = map.containsKey(resource.getContentLocation()) ?
			HttpStatusCode.OK : HttpStatusCode.CREATED;
		
		map.put(resource.getContentLocation(), resource);
		
		return status;
		
	}
	@Override
	protected void doDelete(String resourceIRI) {
		map.remove(resourceIRI);
	}
	@Override
	public ResourceBuilder getResourceBuilder() {
		return new SimpleResourceBuilder();
	}
}
