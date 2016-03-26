package io.konig.services.impl;

/*
 * #%L
 * Konig Services
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


import io.konig.core.Vertex;

public class NonUniqueMappingException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public NonUniqueMappingException(Vertex x, Vertex y1, Vertex y2) {
		super("Node " + x.getId().stringValue() + " maps to more than one node: " + y1.getId().stringValue() + " and " + y2.getId().stringValue());
	}


}
