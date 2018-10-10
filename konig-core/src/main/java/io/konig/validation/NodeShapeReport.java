package io.konig.validation;

/*
 * #%L
 * Konig Core
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

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

public class NodeShapeReport implements Comparable<NodeShapeReport>{

	private Resource shapeId;
	private List<PropertyShapeReport> propertyReports = new ArrayList<>();
	private boolean nameHasWrongCase;

	public NodeShapeReport(Resource shapeId) {
		this.shapeId = shapeId;
	}

	public Resource getShapeId() {
		return shapeId;
	}
	
	public void add(PropertyShapeReport r) {
		propertyReports.add(r);
	}

	public List<PropertyShapeReport> getPropertyReports() {
		return propertyReports;
	}
	
	public boolean isValid() {
		return 
			!nameHasWrongCase &&	
			propertyReports.isEmpty();
	}

	public PropertyShapeReport findPropertyReport(URI propertyId) {
		for (PropertyShapeReport r : propertyReports) {
			URI predicate = r.getPropertyShape().getPredicate();
			if (propertyId.equals(predicate)) {
				return r;
			}
		}
		return null;
	}

	public boolean getNameHasWrongCase() {
		return nameHasWrongCase;
	}

	public void setNameHasWrongCase(boolean wrongCase) {
		this.nameHasWrongCase = wrongCase;
	}

	@Override
	public int compareTo(NodeShapeReport o) {
		return shapeId.stringValue().compareTo(o.getShapeId().stringValue());
	}
	
}
