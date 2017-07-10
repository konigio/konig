package io.konig.schemagen.domain;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class DomainClass {
	
	private Vertex classVertex;
	private Set<URI> superClass = new HashSet<>();

	public DomainClass(Vertex classVertex) {
		this.classVertex = classVertex;
	}

	public Vertex getClassVertex() {
		return classVertex;
	}

	public Set<URI> getSuperClass() {
		return superClass;
	}
	
	public void addSuperClass(URI superClass) {
		this.superClass.add(superClass);
	}
}
