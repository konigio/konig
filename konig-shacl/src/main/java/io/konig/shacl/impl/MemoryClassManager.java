package io.konig.shacl.impl;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
import java.util.Map;

import org.openrdf.model.Resource;

import io.konig.shacl.ClassManager;
import io.konig.shacl.Shape;

public class MemoryClassManager implements ClassManager {
	private Map<String, Shape> shapeMap = new HashMap<String, Shape>();
	
	public MemoryClassManager() {}
	

	@Override
	public Shape getLogicalShape(Resource owlClass) {
		
		return (owlClass==null) ? null : shapeMap.get(owlClass.stringValue());
	}

	@Override
	public Collection<Shape> list() {
		return shapeMap.values();
	}

	@Override
	public void addLogicalShape(Shape shape) {
		shapeMap.put(shape.getTargetClass().stringValue(), shape);
	}

}
