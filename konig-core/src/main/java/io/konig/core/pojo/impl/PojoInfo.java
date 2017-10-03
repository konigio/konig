package io.konig.core.pojo.impl;

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


import io.konig.core.Vertex;
import io.konig.core.pojo.PojoContext;

public class PojoInfo {

	private PojoContext context;
	private Vertex vertex;
	private Class<?> expectedJavaClass;
	private Object javaObject;
	
	
	public Vertex getVertex() {
		return vertex;
	}
	public void setVertex(Vertex vertex) {
		this.vertex = vertex;
	}
	public Class<?> getExpectedJavaClass() {
		return expectedJavaClass;
	}
	public void setExpectedJavaClass(Class<?> expectedJavaClass) {
		this.expectedJavaClass = expectedJavaClass;
	}
	public Object getJavaObject() {
		return javaObject;
	}
	public void setJavaObject(Object javaObject) {
		this.javaObject = javaObject;
	}
	public PojoContext getContext() {
		return context;
	}
	public void setContext(PojoContext context) {
		this.context = context;
	}
	
	
}
