package io.konig.schemagen.java;

/*
 * #%L
 * Konig Schema Generator
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


import java.util.Collections;
import java.util.List;

import org.openrdf.model.URI;

import io.konig.schemagen.maven.FilterPart;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class Filter {

	List<FilterPart> parts;
	

	public Filter(List<FilterPart> parts) {
		if (parts == null) {
			parts = Collections.emptyList();
		}
		this.parts = parts;
	}

	public List<FilterPart> getParts() {
		return parts;
	}

	public void setParts(List<FilterPart> parts) {
		this.parts = parts;
	}
	
	public boolean acceptNamespace(String namespace) {
		boolean result = true;
			
		for (FilterPart part : parts) {
			result = part.acceptNamespace(namespace);
		}
		return result;
	}
	
	public boolean acceptIndividual(String classId) {
		String namespace = namespace(classId);
		return acceptNamespace(namespace);
	}
	
	public boolean acceptShape(Shape shape) {
		for (PropertyConstraint p : shape.getProperty()) {
			URI predicate = p.getPredicate();
			if (acceptIndividual(predicate.stringValue())) {
				return true;
			}
		}
		return false;
	}

	private String namespace(String stringValue) {
		int slash = stringValue.lastIndexOf('/');
		int hash = stringValue.lastIndexOf('#');
		int colon = stringValue.lastIndexOf(':');
		
		int mark = Math.max(slash, hash);
		mark = Math.max(mark, colon);
		
		return stringValue.substring(0, mark+1);
	}
	

}
