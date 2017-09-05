package io.konig.dao.core;

import java.io.Serializable;

/*
 * #%L
 * Konig DAO Core
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


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ChartGeoLocationMapping implements Serializable, Map<String, Object> {

	private static final long serialVersionUID = 1L;

	public static class FusionMapping implements Serializable {
		private static final long serialVersionUID = 1L;
		private String name;
    	private String type;
    	private String fusionId;
    	private String containedInPlace;

        public FusionMapping(String name, String type, String fusionId, String containedInPlace) {
            this.name = name;
            this.type = type;
            this.fusionId = fusionId;
            this.containedInPlace = containedInPlace;
        }
        
        public String getFusionId(){
        	return this.fusionId;
        }
        
        public String getName(){
        	return this.name;
        }
        
        public String getType(){
        	return this.type;
        }
        
        public String getContainedInPlace(){
        	return this.containedInPlace;
        }
    }
	
	private final Map<String, Object> map = new HashMap<>();

	@Override
	public void clear() {
		 map.clear();
	}
	
	@Override
	public boolean containsKey(Object key) {
		 return map.containsKey(key);
	}
	
	@Override
	public boolean containsValue(Object value) {
		return map.containsValue(value);
	}
	@Override
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return map.entrySet();
	}
	
	@Override
	public Object get(Object key) {
		return map.get(key);
	}
	
	@Override
	public boolean isEmpty() {
		return map.isEmpty();
	}
	
	@Override
	public Set<String> keySet() {
		return map.keySet();
	}
	
	@Override
	public Object put(String key, Object object) {
		 return map.put(key, object);
	}
	
	@Override
	public void putAll(Map map) {
		Iterator iterator = map.keySet().iterator();
		while (iterator.hasNext()) {
			String key = (String) iterator.next();
			put(key, map.get(key));
		}		
	}
	
	@Override
	public Object remove(Object object) {
		return map.remove(object);
	}
	
	@Override
	public int size() {
		return map.size();
	}
	
	@Override
	public Collection<Object> values() {
		 return map.values();
	}
	
	public String getFusionId(String key) {
		return ((FusionMapping)map.get(key)).getFusionId();	
	}
	
	public String getName(String key) {
		return ((FusionMapping)map.get(key)).getName();	
	}
	
	public String getType(String key) {
		return ((FusionMapping)map.get(key)).getType();	
	}
	
	public String getContainedInPlace(String key) {
		return ((FusionMapping)map.get(key)).getContainedInPlace();	
	}
}
