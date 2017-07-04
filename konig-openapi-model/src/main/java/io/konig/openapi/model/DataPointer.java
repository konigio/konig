package io.konig.openapi.model;

import java.lang.reflect.Method;
import java.util.Map;

import io.konig.yaml.YamlProperty;



public class DataPointer {

	@SuppressWarnings("unchecked")
	public static <T> T resolve(Class<T> type, Object root, String reference) {
		return (T) resolve(root, reference);
	}
	public static Object resolve(Object root, String reference) {
		
		String[] path = reference.split("/");
		
		Object value = root;
		if ("#".equals(path[0])) {
			for (int i=1; i<path.length && value != null; i++) {
				String fieldName = path[i];
				
				if (value instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Object> map = (Map<String,Object>) value;
					value = map.get(fieldName);
				} else {

					Class<?> type = value.getClass();
					Method getterMethod = getterMethod(fieldName, type);
					try {
						value = getterMethod==null ? null :
							getterMethod.invoke(value);
					} catch (Throwable e) {
						throw new RuntimeException(e);
					}
				}
			}
		}
		
		return value;
	}

	private static Method getterMethod(String fieldName, Class<?> type) {

		String getterName = getterName(fieldName);
		Method[] methodList = type.getMethods();
		for (Method method : methodList) {
			YamlProperty note = method.getAnnotation(YamlProperty.class);
			
			if (note != null) {
				if (note.value().equals(fieldName)) {
					return method;
				} 
			} else {
				if (method.getParameterTypes().length==0 && method.getName().equals(getterName)) {
					return method;
				}
			}
		}
		return null;
	}

	private static String getterName(String fieldName) {
		StringBuilder builder = new StringBuilder();
		builder.append("get");
		builder.append(Character.toUpperCase(fieldName.charAt(0)));
		for (int i=1; i<fieldName.length(); i++) {
			builder.append(fieldName.charAt(i));
		}
		return builder.toString();
	}
	

}
