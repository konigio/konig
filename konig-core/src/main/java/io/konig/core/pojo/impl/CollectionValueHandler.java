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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.konig.core.KonigException;

public class CollectionValueHandler implements ThreePhaseValueHandler {
	
	private Class<?> collectionType;
	private Method collectionSetter;
	private Method collectionGetter;
	private ValueHandler elementHandler;
	


	public CollectionValueHandler(Class<?> collectionType, Method collectionSetter, Method collectionGetter,
			ValueHandler elementHandler) {
		this.collectionType = collectionType;
		this.collectionSetter = collectionSetter;
		this.collectionGetter = collectionGetter;
		this.elementHandler = elementHandler;
	}



	@Override
	public void setUp(PropertyInfo propertyInfo) {
		try {
			Object container = propertyInfo.getContainer();
			Object collection = null;
			
			if (collectionGetter != null) {
				collection = collectionGetter.invoke(container);
			}
			if (collection == null) {
				collection = collectionType.newInstance();
				collectionSetter.invoke(container, collection);
			}
			
			PojoInfo pojoInfo = new PojoInfo();
			pojoInfo.setVertex(propertyInfo.getSubject().getVertex());
			pojoInfo.setContext(propertyInfo.getContext());
			pojoInfo.setJavaObject(collection);

			ElementInfo elementInfo = new ElementInfo(propertyInfo);
			elementInfo.setSubject(pojoInfo);
			elementInfo.setPredicate(propertyInfo.getPredicate());
			
			propertyInfo.setElementInfo(elementInfo);
			
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			throw new KonigException(e);
		}
		
	}


	@Override
	public void tearDown(PropertyInfo propertyInfo) {
		propertyInfo.setElementInfo(null);
	}

	@Override
	public void handleValue(PropertyInfo propertyInfo) throws KonigException {
					
		ElementInfo elementInfo = propertyInfo.getElementInfo();
		elementInfo.setObject(propertyInfo.getObject());
		
		elementHandler.handleValue(elementInfo);
			
	}



}
