package io.konig.schemagen.maven;

/*
 * #%L
 * Konig Maven Common
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


import java.util.Set;

abstract public class FilterPart {
	
	private Set<String> namespaces;
	
	public Set<String> getNamespaces() {
		return namespaces;
	}

	public void setNamespaces(Set<String> namespaces) {
		this.namespaces = namespaces;
	}

	abstract public boolean acceptNamespace(String namespace);

}
