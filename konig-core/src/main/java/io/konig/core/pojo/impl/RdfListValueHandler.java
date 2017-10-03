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
import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Graph;
import io.konig.core.KonigException;
import io.konig.core.Vertex;

public class RdfListValueHandler implements ValueHandler {
	
	private Class<?> collectionType;
	private Method collectionSetter;
	private Method collectionGetter;
	private ValueHandler elementHandler;
	
	
	
	public RdfListValueHandler(Class<?> collectionType, Method collectionSetter, Method collectionGetter,
			ValueHandler elementHandler) {
		this.collectionType = collectionType;
		this.collectionSetter = collectionSetter;
		this.collectionGetter = collectionGetter;
		this.elementHandler = elementHandler;
	}



	@Override
	public void handleValue(PropertyInfo propertyInfo) throws KonigException {
		
		
		Value object = propertyInfo.getObject();
		if (object instanceof Resource) {
			Resource listId = (Resource) object;
			Graph graph = propertyInfo.getSubject().getVertex().getGraph();
			Vertex listVertex = graph.getVertex(listId);
			
			List<Value> valueList = listVertex.asList();
			if (valueList != null) {
				
				before(propertyInfo);
				ElementInfo elementInfo = propertyInfo.getElementInfo();
				elementInfo.getSubject().setVertex(listVertex);
				
				for (Value value : valueList) {
					elementInfo.setObject(value);
					elementHandler.handleValue(elementInfo);
				}
				after(propertyInfo);
			}
		}
		

	}


	public void before(PropertyInfo propertyInfo) {
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
			elementInfo.setPredicate(RDF.FIRST);
			
			propertyInfo.setElementInfo(elementInfo);
			
			
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			throw new KonigException(e);
		}
		
	}


	public void after(PropertyInfo propertyInfo) {
		propertyInfo.setElementInfo(null);
	}

}
