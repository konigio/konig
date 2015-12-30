package io.konig.shacl;

/*
 * #%L
 * konig-shacl
 * %%
 * Copyright (C) 2015 Gregory McFall
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
import org.openrdf.model.Value;
import org.openrdf.model.impl.BNodeImpl;

import io.konig.core.UidGenerator;

public class PropertyConstraint {

	private Resource id;
	private URI predicate;
	private List<Value> allowedValues;
	private Integer minCount;
	private Integer maxCount;
	private URI datatype;
	private URI type;
	private URI directType;
	private URI valueShapeId;
	private Shape valueShape;


	public PropertyConstraint(URI predicate) {
		this.id = new BNodeImpl(UidGenerator.INSTANCE.next());
		this.predicate = predicate;
	}
	public PropertyConstraint(Resource id, URI predicate) {
		this.id = id;
		this.predicate = predicate;
	}
	
	public Resource getId() {
		return id;
	}



	public URI getPredicate() {
		return predicate;
	}



	public void addAllowedValue(Value value) {
		if (allowedValues == null) {
			allowedValues = new ArrayList<Value>();
		}
		allowedValues.add(value);
	}
	
	/**
	 * Get read-only list of allowed-values for this property constraint.
	 * @return
	 */
	public List<Value> getAllowedValues() {
		return allowedValues;
	}

	public Integer getMinCount() {
		return minCount;
	}

	public void setMinCount(Integer minCount) {
		this.minCount = minCount;
	}

	public Integer getMaxCount() {
		return maxCount;
	}

	public void setMaxCount(Integer maxCount) {
		this.maxCount = maxCount;
	}

	public URI getDatatype() {
		return datatype;
	}

	public void setDatatype(URI datatype) {
		this.datatype = datatype;
	}

	public URI getType() {
		return type;
	}

	public void setType(URI type) {
		this.type = type;
	}

	public URI getDirectType() {
		return directType;
	}

	public void setDirectType(URI directType) {
		this.directType = directType;
	}

	public URI getValueShapeId() {
		return valueShapeId;
	}

	public void setValueShapeId(URI valueShape) {
		this.valueShapeId = valueShape;
	}

	public Shape getValueShape() {
		return valueShape;
	}

	public void setValueShape(Shape valueShape) {
		this.valueShape = valueShape;
	}
	
	
	
}
