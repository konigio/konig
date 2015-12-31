package io.konig.core.io.impl;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 Gregory McFall
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

import io.konig.core.io.ResourceFile;
import io.konig.core.io.ResourceManager;

public class MemoryResourceManager implements ResourceManager {
	private Map<String, ResourceFile> map = new HashMap<>();

	@Override
	public void delete(String contentLocation) throws IOException {
		map.remove(contentLocation);
	}

	@Override
	public ResourceFile get(String contentLocation) throws IOException {
		return map.get(contentLocation);
	}

	@Override
	public void put(ResourceFile file) throws IOException {
		map.put(file.getContentLocation(), file);
	}


}
