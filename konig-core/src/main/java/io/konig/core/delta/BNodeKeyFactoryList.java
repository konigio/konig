package io.konig.core.delta;

/*
 * #%L
 * Konig Core
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


import java.util.ArrayList;
import java.util.Collection;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class BNodeKeyFactoryList extends ArrayList<BNodeKeyFactory> implements BNodeKeyFactory {
	private static final long serialVersionUID = 1L;

	public BNodeKeyFactoryList() {
	}

	public BNodeKeyFactoryList(int arg0) {
		super(arg0);
	}

	public BNodeKeyFactoryList(Collection<BNodeKeyFactory> arg0) {
		super(arg0);
	}

	@Override
	public BNodeKey createKey(URI predicate, Vertex object) {
		
		for (BNodeKeyFactory factory : this) {
			BNodeKey key = factory.createKey(predicate, object);
			if (key != null) {
				return key;
			}
		}
		
		return null;
	}

}
