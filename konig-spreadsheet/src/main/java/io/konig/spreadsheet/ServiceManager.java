package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.konig.core.KonigException;

public class ServiceManager {
	
	private Map<Class<?>, Object> serviceMap = new HashMap<>();
	private Map<Class<?>, ServiceFactory<?>> factoryMap = new HashMap<>();
	private ServiceListener listener;
	
	public ServiceManager() {
		addService(this);
	}
	public ServiceListener getListener() {
		return listener;
	}

	public void setListener(ServiceListener listener) {
		this.listener = listener;
	}
	public void addFactory(Class<?> type, ServiceFactory<?> factory) {
		factoryMap.put(type, factory);
	}
	

	public void addService(Object service) {
		addService(service.getClass(), service);
	}
	
	public void addService(Class<?> type, Object service) {
		serviceMap.put(type, service);
		if (listener != null) {
			listener.onRegister(service);
		}
	}
	
	/**
	 * Get the service registered for the given Java Class.
	 * @param javaClass
	 * @return The service registered for the given Java Class, 
	 * or null if no such service has been registered.
	 */
	@SuppressWarnings("unchecked")
	public <T> T getService(Class<T> javaClass) {
		return (T) serviceMap.get(javaClass);
	}

	/**
	 * Get the registered implementation of a given Java Class.
	 * If no such implementation has been registered one will be created and 
	 * registered.
	 * 
	 * @param javaClass  The Java Class of the service to be returned.
	 * @return An implementation of the given Java Class.
	 */
	@SuppressWarnings("unchecked")
	public <T> T service(Class<T> javaClass) {
		
		T result = (T) serviceMap.get(javaClass);
		if (result == null) {
			result = createService(javaClass);
			if (result != null) {
				addService(javaClass, result);
				if (result != null && listener!=null) {
					listener.onCreateService(result);
				}
			}
		}
		
		return result;
	}

	protected <T> T createService(Class<T> javaClass) {
		
		ServiceFactory<T> factory = getFactory(javaClass);
		
		if (factory != null) {
			return factory.createInstance();
		}
		
		List<Constructor<T>> ctorList = serviceConstructor(javaClass);
		for (Constructor<T> ctor : ctorList) {
			
			if (ctor != null) {
				Object[] args = serviceArgs(ctor);
				if (args != null) {
					try {
						return ctor.newInstance(args);
					} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {
						throw new KonigException(e);
					}
				}
			}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	private <T> ServiceFactory<T> getFactory(Class<T> javaClass) {
		
		return (ServiceFactory<T>) factoryMap.get(javaClass);
	}
	private Object[] serviceArgs(Constructor<?> ctor) {
		Object[] array = new Object[ctor.getParameterCount()];
	
		Class<?>[] paramTypes = ctor.getParameterTypes();
		for (int i = 0; i<array.length; i++) {
			Object value = service(paramTypes[i]);
			if (value == null) {
				return null;
			}
			array[i] = value;
		}
		return array;
	}

	private <T> List<Constructor<T>> serviceConstructor(Class<T> javaClass) {
		@SuppressWarnings("unchecked")
		Constructor<T>[] array = (Constructor<T>[]) javaClass.getConstructors();
		
		List<Constructor<T>> list = Arrays.asList(array);
		
		// Sort the constructors by decreasing number of parameters
		Collections.sort(list, new Comparator<Constructor<T>>() {

			@Override
			public int compare(Constructor<T> a, Constructor<T> b) {
				int delta = b.getParameterCount() - a.getParameterCount();
				return delta;
			}
		});
		
		return list;
	}

}
