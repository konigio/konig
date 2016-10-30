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


import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;

public class BNodeKey extends BNodeImpl {
	private static final long serialVersionUID = 1L;
	private Map<String, URI> part;
	private transient BNodeKeyFactory factory;
	
	public BNodeKey(String hash, Map<String, URI> part, BNodeKeyFactory factory) {
		super(hash);
		this.part = part;
		this.factory = factory;
	}

	public String getHash() {
		return getID();
	}

	public BNodeKeyFactory getFactory() {
		return factory;
	}
	
	public URI keyPart(URI predicate) {
		return part.get(predicate.stringValue());
	}
	
	
}
