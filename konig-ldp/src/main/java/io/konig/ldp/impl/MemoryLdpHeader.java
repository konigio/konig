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


import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.konig.ldp.LdpHeader;

public class MemoryLdpHeader implements LdpHeader {
	private Map<String, String> map = new HashMap<>();

	@Override
	public Collection<String> getHeaderNames() {
		return map.keySet();
	}

	@Override
	public void put(String name, String value) {
		map.put(name, value);
	}

	@Override
	public String getHeader(String name) {
		return map.get(name);
	}

}
