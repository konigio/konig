package io.konig.spreadsheet;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.konig.core.KonigException;
import io.konig.core.util.StringUtil;
import io.konig.core.util.ValueMap;

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
