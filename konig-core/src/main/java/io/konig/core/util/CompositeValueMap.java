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


import java.util.ArrayList;

public class CompositeValueMap extends ArrayList<ValueMap> implements ValueMap {
	private static final long serialVersionUID = 1L;
	
	public CompositeValueMap() {
		
	}
	
	public CompositeValueMap(ValueMap...maps) {
		for (ValueMap m : maps) {
			add(m);
		}
	}

	@Override
	public String get(String name) {
		String result = null;
		for (ValueMap map : this) {
			result = map.get(name);
			if (result != null) {
				break;
			}
		}
		return result;
	}

}
