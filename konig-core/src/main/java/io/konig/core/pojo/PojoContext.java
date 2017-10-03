package io.konig.core.pojo;

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


import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.openrdf.model.Resource;

import io.konig.core.pojo.impl.MasterPojoHandler;
import io.konig.core.pojo.impl.PojoHandler;

public class PojoContext {

	private Map<Resource, Class<?>> classMap;
	private Map<Class<?>, PojoHandler> pojoHandlerMap = new HashMap<>();
	private Map<Resource,Object> individualMap = new HashMap<>();
	private PojoHandler pojoHandler;
	private PojoListener pojoListener;
	
	public PojoContext() {
		classMap = new HashMap<>();
		pojoHandler = new MasterPojoHandler();
	}
	
	public PojoContext(PojoContext source) {
		this.classMap = source.classMap;
		this.pojoHandlerMap = source.pojoHandlerMap;
		this.pojoHandler = source.pojoHandler;
	}

	
	public void mapClass(Resource owlClass, Class<?> javaClass) {
		classMap.put(owlClass, javaClass);
	}
	
	public Map<Resource, Class<?>> getClassMap() {
		return classMap;
	}

	public Class<?> getJavaClass(Resource resource) {
		return classMap.get(resource);
	}
	
	public PojoHandler getPojoHandler(Class<?> javaClass) {
		return pojoHandlerMap.get(javaClass);
	}
	
	public PojoHandler putPojoHandler(Class<?> javaClass, PojoHandler pojoHandler) {
		return pojoHandlerMap.put(javaClass, pojoHandler);
	}

	public PojoHandler getPojoHandler() {
		return pojoHandler;
	}

	public void setPojoHandler(PojoHandler pojoHandler) {
		this.pojoHandler = pojoHandler;
	}

	
	@SuppressWarnings("unchecked")
	public <T> T getIndividual(Resource resource, Class<T> javaClass) {
		return (T) getIndividual(resource);
	}
	
	public Object getIndividual(Resource resource) {
		return individualMap.get(resource);
	}

	public Map<Resource, Object> getIndividualMap() {
		return individualMap;
	}
	
	public void mapObject(Resource resource, Object pojo) {
		individualMap.put(resource, pojo);
	}

	public PojoListener getListener() {
		return pojoListener;
	}

	public void setListener(PojoListener pojoListener) {
		this.pojoListener = pojoListener;
	}
	
	public void notify(Resource resource, Object pojo) {
		if (pojoListener != null) {
			pojoListener.map(resource, pojo);
		}
	}

}
