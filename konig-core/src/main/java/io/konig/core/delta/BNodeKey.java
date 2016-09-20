package io.konig.core.delta;

/*
 * #%L
 * konig-core
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


import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;

/**
 * A structure that captures a key suitable for indexing an anonymous individual within a parent resource.
 * @author Greg McFall
 *
 */
public class BNodeKey extends BNodeImpl {
	private static final long serialVersionUID = 1L;
	
	private String key;
	private List<URI> predicates;
	
	public BNodeKey(String key, List<URI> predicates) {
		this.key = key;
		this.predicates = predicates;
	}

	public String getKey() {
		return key;
	}

	public List<URI> getPredicates() {
		return predicates;
	}

}
