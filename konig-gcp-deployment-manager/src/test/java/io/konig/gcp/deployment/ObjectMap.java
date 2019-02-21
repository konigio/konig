package io.konig.gcp.deployment;

/*
 * #%L
 * Konig GCP Deployment Manager
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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMap {
	
	private Map<String, Object> map;

	public ObjectMap(Map<String, Object> map) {
		this.map = map;
	}
	
	public String stringValue(String fieldName) {
		return (String) map.get(fieldName);
	}
	
	@SuppressWarnings("unchecked")
	public ObjectMap objectValue(String fieldName) {
		Object value = map.get(fieldName);
		if (value instanceof Map) {
			return new ObjectMap((Map<String,Object>)value);
		}
		return new ObjectMap(new HashMap<>());
	}
	
	@SuppressWarnings("unchecked")
	public List<ObjectMap> objectList(String fieldName) {
		List<ObjectMap> result = new ArrayList<>();
		Object value = map.get(fieldName);
		if (value instanceof List) {
			for (Object e : (List<?>) value) {
				result.add(new ObjectMap((Map<String,Object>) e));
			}
		}
		
		return result;
	}

}
