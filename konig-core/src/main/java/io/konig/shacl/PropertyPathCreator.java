package io.konig.shacl;

/*
 * #%L
 * Konig Core
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


import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.core.Vertex;
import io.konig.core.pojo.PojoCreator;

public class PropertyPathCreator implements PojoCreator<PropertyPath> {

	@Override
	public PropertyPath create(Vertex v) {
		
		List<Value> list = v.asList();
		
		
		if (list != null) {
			return new SequencePath();
		}
		Resource id = v.getId();
		if (id instanceof URI) {
			URI uri = (URI) id;
			return new PredicatePath(uri);
		}
		throw new KonigException("Unsupported PropertyPath: " + v);
	}
}
