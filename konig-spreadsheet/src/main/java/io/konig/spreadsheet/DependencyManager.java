package io.konig.spreadsheet;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DependencyManager<T> {
	
	public void sort(List<T> list) {
		List<Entity<T>> serviceList = new ArrayList<>();
		Map<Class<?>, Entity<T>> map = new HashMap<>();
		for (T t : list) {
			Entity<T> service = new Entity<T>(t);
			serviceList.add(service);
			map.put(t.getClass(), service);
			
		}
		
		buildDependencies(serviceList, map);
		Collections.sort(serviceList);
		
		
		list.clear();
		for (Entity<T> service : serviceList) {
			list.add(service.getBean());
		}
	}
	
	private void buildDependencies(List<Entity<T>> serviceList, Map<Class<?>, Entity<T>> map) {
		for (Entity<T> s : serviceList) {
			List<Entity<T>> dependencies = new ArrayList<>();
			s.setDependencies(dependencies);
			for (Constructor<?> ctor : s.getBean().getClass().getConstructors()) {
				for (Class<?> paramType : ctor.getParameterTypes()) {
					Entity<T> other = map.get(paramType);
					if (other != null) {
						dependencies.add(other);
					}
				}
			}
		}
		
	}

	private static class Entity<T> implements Comparable<Entity<T>> {
		private T bean;
		private List<Entity<T>> dependencies;
		
		public Entity(T bean) {
			this.bean = bean;
		}

		public void setDependencies(List<Entity<T>> dependencies) {
			this.dependencies = dependencies;
		}

		public T getBean() {
			return bean;
		}
		
		public boolean dependsOn(Entity<T> other) {
			return dependencies.contains(other);
		}

		@Override
		public int compareTo(Entity<T> o) {
			if (this.dependsOn(o)) {
				return 1;
			}
			if (o.dependsOn(this)) {
				return -1;
			}
			return 0;
		}

		
		
	}
	
	

}
