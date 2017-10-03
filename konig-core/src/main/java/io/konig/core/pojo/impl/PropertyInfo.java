package io.konig.core.pojo.impl;

import org.openrdf.model.Resource;

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


import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.pojo.PojoContext;

public class PropertyInfo {

	private PojoInfo subject;
	private URI predicate;
	private Value object;
	
	private ElementInfo elementInfo;
	
	public PojoInfo getSubject() {
		return subject;
	}
	public void setSubject(PojoInfo subject) {
		this.subject = subject;
	}
	public URI getPredicate() {
		return predicate;
	}
	public void setPredicate(URI predicate) {
		this.predicate = predicate;
	}
	public Value getObject() {
		return object;
	}
	public void setObject(Value object) {
		this.object = object;
	}
	
	public PojoContext getContext() {
		return subject.getContext();
	}
	
	public Object getContainer() {
		return subject.getJavaObject();
	}
	public ElementInfo getElementInfo() {
		return elementInfo;
	}
	public void setElementInfo(ElementInfo elementInfo) {
		this.elementInfo = elementInfo;
	}
	
	public Vertex getObjectVertex() {
		
		if (object instanceof Resource) {
			Graph graph = subject.getVertex().getGraph();
			return graph.getVertex((Resource)object);
		}
		
		return null;
	}
	
}
