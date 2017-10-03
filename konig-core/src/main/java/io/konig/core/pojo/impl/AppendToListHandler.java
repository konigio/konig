package io.konig.core.pojo.impl;

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


import java.util.List;

import org.openrdf.model.Value;

import io.konig.core.KonigException;
import io.konig.core.Vertex;

public class AppendToListHandler implements ValueHandler {
	
	private ValueHandler elementHandler;
	
	public AppendToListHandler(ValueHandler elementHandler) {
		this.elementHandler = elementHandler;
	}

	@Override
	public void handleValue(PropertyInfo propertyInfo) throws KonigException {
		
		
		Vertex vertex = propertyInfo.getObjectVertex();
		if (vertex != null) {
			List<Value> list = vertex.asList();
			if (list != null) {
				for (Value value : list) {
					propertyInfo.setObject(value);
					elementHandler.handleValue(propertyInfo);
				}
			}
		}
		
	}


}
