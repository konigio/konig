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

import org.openrdf.model.Resource;
import org.openrdf.model.Value;

import io.konig.core.KonigException;

abstract public class AbstractResourceSetter implements ValueBuilder {
	
	private Method setter;

	public AbstractResourceSetter(Method setter) {
		this.setter = setter;
	}

	@Override
	public void beginValue(ValueExchange exchange) throws KonigException {

		Object pojo = exchange.getJavaSubject();
		Value object = exchange.getObject();
		try {
			set(pojo, setter, (Resource) object);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new KonigException(e);
		}

	}

	
	abstract protected void set(Object pojo, Method setter, Resource object) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException;

	@Override
	public void endValue(ValueExchange exchange) throws KonigException {
	

	}

}
