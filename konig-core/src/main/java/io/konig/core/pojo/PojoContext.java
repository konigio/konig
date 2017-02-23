package io.konig.core.pojo;

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


import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Resource;

import io.konig.core.Graph;

public class PojoContext {
	
	private Map<Resource, Class<?>> classMap;
	private Map<Resource, Object> individualMap;
	private Graph ontology;
	private PojoListener listener;

	public PojoContext() {
		classMap = new LinkedHashMap<>();
		individualMap = new LinkedHashMap<>();
	}
	
	public PojoContext(PojoContext copy) {
		classMap = copy.classMap;
		individualMap = new LinkedHashMap<>();
	}

	public PojoContext(Graph ontology) {
		this.ontology = ontology;
	}

	public Graph getOntology() {
		return ontology;
	}

	public void setOntology(Graph ontology) {
		this.ontology = ontology;
	}
	
	public Map<Resource, Class<?>> getClassMap() {
		return classMap;
	}

	public Map<Resource, Object> getIndividualMap() {
		return individualMap;
	}
	
	public void mapObject(Resource resource, Object pojo) {
		individualMap.put(resource, pojo);
		if (listener != null) {
			listener.map(resource, pojo);
		}
	}

	public Class<?> getJavaClass(Resource resource) {
		return classMap.get(resource);
	}
	
	public Object getIndividual(Resource resource) {
		return individualMap.get(resource);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T getIndividual(Resource resource, Class<T> javaClass) {
		return (T) getIndividual(resource);
	}
	
	public void mapClass(Resource owlClass, Class<?> javaClass) {
		classMap.put(owlClass, javaClass);
	}

	public PojoListener getListener() {
		return listener;
	}

	public void setListener(PojoListener listener) {
		this.listener = listener;
	}
	
	

}
