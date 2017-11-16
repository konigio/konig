package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
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


import org.openrdf.model.URI;

import io.konig.core.io.PrettyPrintWriter;
import io.konig.shacl.PropertyConstraint;

public class BasicPropertyModel extends PropertyModel {
	private PropertyConstraint propertyConstraint;
	private ShapeModel valueModel;

	public BasicPropertyModel(URI predicate, PropertyGroup group, PropertyConstraint propertyConstraint) {
		super(predicate, group);
		this.propertyConstraint = propertyConstraint;
	}

	public PropertyConstraint getPropertyConstraint() {
		return propertyConstraint;
	}


	public ShapeModel getValueModel() {
		return valueModel;
	}

	public void setValueModel(ShapeModel valueModel) {
		this.valueModel = valueModel;
	}


	@Override
	protected void printProperties(PrettyPrintWriter out) {
		super.printProperties(out);
		out.field("valueModel", valueModel);
	}
}
