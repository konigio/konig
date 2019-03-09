package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.cadl.Cube;
import io.konig.core.LocalNameService;

public class CubeLocalNameService implements LocalNameService {

	private LocalNameService defaultService;
	private Cube cube;

	public CubeLocalNameService(LocalNameService defaultService, Cube cube) {
		this.defaultService = defaultService;
		this.cube = cube;
	}

	@Override
	public Set<URI> lookupLocalName(String localName) {
		if (cube.getSource() != null) {
			URI sourceId = cube.getSource().getId();
			if (localName.equals(sourceId.getLocalName())) {
				Set<URI> set = new HashSet<>();
				set.add(sourceId);
				return set;
			}
		}
		return defaultService.lookupLocalName(localName);
	}

}
