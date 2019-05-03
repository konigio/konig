package io.konig.lineage;

/*
 * #%L
 * Konig Core
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


import java.util.ArrayList;

import org.openrdf.model.URI;

import io.konig.annotation.RdfList;

@SuppressWarnings("serial")
@RdfList
public class DatasourcePropertyPath extends ArrayList<URI> {

	public DatasourcePropertyPath(URI predicate) {
		super(1);
		add(predicate);
	}
	
	public DatasourcePropertyPath() {
		
	}
	
	@Override
	public boolean add(URI predicate) {
		return super.add(predicate);
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof DatasourcePropertyPath) {
			DatasourcePropertyPath list = (DatasourcePropertyPath) obj;
			if (list.size() == size()) {
				for (int i=0; i<size(); i++) {
					URI a = get(i);
					URI b = list.get(i);
					if (!a.equals(b)) {
						return false;
					}
				}
				return true;
			}
		}
		
		return false;
	}
	
	public URI pop() {
		return remove(size()-1);
	}
	
	public String simpleName() {
		if (size() == 1) {
			return get(0).getLocalName();
		}
		if (isEmpty()) {
			return "null";
		}
		
		StringBuilder builder = new StringBuilder();
		String dot = "";
		for (URI predicate : this) {
			builder.append(dot);
			builder.append(predicate.getLocalName());
			dot = ".";
		}
		return builder.toString();
	}

}
