package io.konig.datacatalog;

/*
 * #%L
 * Konig Data Catalog
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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
import java.util.List;

import org.openrdf.model.URI;

import io.konig.shacl.PropertyConstraint;

public class PropertyUsage {
	private URI predicate;
	private List<PropertyConstraint> constraintList = new ArrayList<>();
	
	public PropertyUsage(URI predicate) {
		this.predicate = predicate;
	}

	public URI getPredicate() {
		return predicate;
	}

	public List<PropertyConstraint> getConstraintList() {
		return constraintList;
	}
	
	public void add(PropertyConstraint p) {
		constraintList.add(p);
	}

}
