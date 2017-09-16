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


import java.lang.reflect.Method;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.KonigException;

abstract public class AbstractLiteralSetter implements LiteralSetter, ValueBuilder {

	private Method method;

	public AbstractLiteralSetter(Method method) {
		this.method = method;
	}

	@Override
	public void set(Object pojo, Value value) throws KonigException {
		
	
		
		try {
			if (value instanceof URI) {
				method.invoke(pojo, value.stringValue());
			} else {
				invoke(pojo, method, (Literal) value);
			}
		} catch (Throwable e) {
			throw new KonigException("Failed to invoke method: " + method, e);
		}
		
	}

	protected abstract void invoke(Object pojo, Method method, Literal value) throws Throwable;
	

	public void beginValue(ValueExchange exchange) throws KonigException {
		Object pojo = exchange.getJavaSubject();
		Value object = exchange.getObject();
		set(pojo, object);
	}
	
	public void endValue(ValueExchange exchange) throws KonigException {
		// Do nothing
	}

}
