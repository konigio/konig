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


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.konig.core.KonigException;

public class CollectionSetter implements CollectionBuilder {
	private Class<?> collectionType;
	private Method collectionSetter;
	private ValueBuilder adder;
	
	

	public CollectionSetter(Class<?> collectionType, Method collectionSetter, ValueBuilder adder) {
		this.collectionType = collectionType;
		this.collectionSetter = collectionSetter;
		this.adder = adder;
	}

	@Override
	public void beginValue(ValueExchange exchange) throws KonigException {
		
	}

	@Override
	public void endValue(ValueExchange exchange) throws KonigException {
		
	}

	@Override
	public ValueExchange beginCollection(ValueExchange exchange) {
		try {
			exchange.setValueBuilder(this);
			Object javaSubject = exchange.getJavaSubject();
			Object javaObject = collectionType.newInstance();
			collectionSetter.invoke(javaSubject, javaObject);
			exchange.setJavaObject(javaObject);
			ValueExchange child = exchange.push();
			child.setValueBuilder(adder);
			return child;
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new KonigException(e);
		} 
	}

	@Override
	public ValueExchange endCollection(ValueExchange exchange) {
		exchange.setChild(null);
		return exchange;
	}
}
