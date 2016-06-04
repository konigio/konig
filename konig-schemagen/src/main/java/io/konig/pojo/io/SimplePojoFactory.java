package io.konig.pojo.io;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.annotation.RdfProperty;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;

public class SimplePojoFactory implements PojoFactory {

	private Map<String, ClassInfo<?>> classInfo = new HashMap<>();
	
	@Override
	public <T> T create(Vertex v, Class<T> type) throws ParseException {
		

		try {
			
			ClassInfo<T> info = getClassInfo(type);
			return create(v, info);
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new ParseException(e);
		}
	}

	private <T> T create(Vertex v, ClassInfo<T> info) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		T pojo = null;
		pojo = info.getJavaType().newInstance();
		Set<Edge> edgeSet = v.outEdgeSet();
		Graph g = v.getGraph();
		for (Edge e : edgeSet) {
			URI predicate = e.getPredicate();
			PropertyInfo p = info.getPropertyInfo(predicate);
			if (p != null) {
				Value value = e.getObject();
				p.set(g, pojo, value);
			}
		}
		
		return pojo;
	}
	
	
	private <T> ClassInfo<T> getClassInfo(Class<T> javaType) {
		@SuppressWarnings("unchecked")
		ClassInfo<T> result = (ClassInfo<T>) classInfo.get(javaType.getName());
		
		if (result == null) {
			result = new ClassInfo<T>(javaType);
			classInfo.put(javaType.getName(), result);
		}
		return result;
	}
	
	private class ClassInfo<T> {
		private Class<T> javaType;
		private Method[] methods;
		private Map<URI, PropertyInfo> propertyMap = new HashMap<>();
		
		public ClassInfo(Class<T> javaType) {
			this.javaType = javaType;
			methods = javaType.getMethods();
		}
		
		public Class<T> getJavaType() {
			return javaType;
		}

		public PropertyInfo getPropertyInfo(URI predicate) {
			
			PropertyInfo result = propertyMap.get(predicate);
			if (result == null) {
				String setterName = BeanUtil.setterName(predicate);
				String adderName = BeanUtil.adderName(predicate);
				
				for (Method m : methods) {
					String name = m.getName();
					if (setterName.equals(name) || adderName.equals(name)) {
						result = new PropertyInfo(predicate, m);
						break;
					}
					RdfProperty annotation = m.getAnnotation(RdfProperty.class);
					if (
						annotation != null && 
						predicate.stringValue().equals(annotation.value()) &&
						m.getParameterTypes().length==1
					) {
						result = new PropertyInfo(predicate, m);
						break;
					}
				}
				
				if (result != null) {
					add(result);
				}
			}
			
			return result;
		}
		
		private void add(PropertyInfo info) {
			propertyMap.put(info.getPredicate(), info);
		}
		
		
	}
	
	
	private class PropertyInfo {
		private URI predicate;
		private Method setter;
		
		public PropertyInfo(URI predicate, Method setter) {
			this.predicate = predicate;
			this.setter = setter;
		}

		public URI getPredicate() {
			return predicate;
		}

		void set(Graph g, Object instance, Value value) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
			
			Class<?>[] typeArray = setter.getParameterTypes();
			if (typeArray.length==1) {
				Class<?> type = typeArray[0];
				
				if (type == String.class) {
					setter.invoke(instance, value.stringValue());
				} else if ((type == int.class) || (type==Integer.class) && value instanceof Literal) {
					Literal literal = (Literal) value;
					setter.invoke(instance, new Integer(literal.intValue()));
				} else if (type == URI.class && value instanceof URI) {
					setter.invoke(instance,  (URI) value);
				} else if (value instanceof Resource) {
					Vertex vertex = g.vertex((Resource)value);
					
					Object object = create(vertex, type);
					setter.invoke(instance, object);
				}
			}
			
			
		}
		
	}
	

}
