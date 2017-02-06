package io.konig.core.util;

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

public class BeanValueMap implements ValueMap {
	
	private Object bean;

	public BeanValueMap(Object bean) {
		this.bean = bean;
	}

	@Override
	public String get(String name) {
		String methodName = "get" + StringUtil.capitalize(name);
		try {
			Method m = bean.getClass().getMethod(methodName);
			Object result = m.invoke(bean);
			if (result != null) {
				return result.toString();
			}
			
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new KonigException("Cannot get value: " + name, e);
		}
		
		return null;
	}

}
