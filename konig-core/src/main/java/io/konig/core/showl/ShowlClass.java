package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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

public class ShowlClass {
	
	private URI owlClassId;
	
	private Set<ShowlProperty> domainOf = new HashSet<>();
	private Set<ShowlProperty> rangeOf = new HashSet<>();
	private Set<ShowlClass> superClasses = null;
	
	private Set<ShowlNodeShape> targetClassOf = new HashSet<>();
	
	public ShowlClass(URI owlClassId) {
		this.owlClassId = owlClassId;
	}

	public URI getId() {
		return owlClassId;
	}

	
	public void addSuperClass(ShowlClass superclass) {
		if (superClasses == null) {
			superClasses = new HashSet<>();
		}
		superClasses.add(superclass);
	}
	
	public Set<ShowlClass> getSuperClasses() {
		return superClasses;
	}

	public String toString() {
		return "ShowlClass[" + owlClassId.getLocalName() + "]";
	}
	
	public Set<ShowlProperty> getRangeOf() {
		return rangeOf;
	}
	
	public void addRangeOf(ShowlProperty p) {
		rangeOf.add(p);
	}
	
	public Set<ShowlProperty> getDomainOf() {
		return domainOf;
	}
	
	public void addDomainOf(ShowlProperty p) {
		domainOf.add(p);
	}
	
	public void addTargetClassOf(ShowlNodeShape nodeShape) {
		targetClassOf.add(nodeShape);
	}

	public Set<ShowlNodeShape> getTargetClassOf() {
		return targetClassOf;
	}
	
}
