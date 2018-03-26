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

public class DirectPropertyModel extends BasicPropertyModel {
	
	private StepPropertyModel pathTail;

	public DirectPropertyModel(URI predicate, PropertyGroup group, PropertyConstraint propertyConstraint) {
		super(predicate, group, propertyConstraint);
	}

	/**
	 * Get the StepPropertyModel that represents the last step in the
	 * PathExpression associated with this direct property.
	 * 
	 * @return A model for the last step in the PathExpression, or null if this
	 * direct property is not associated with a PathExpression.
	 */
	public StepPropertyModel getPathTail() {
		return pathTail;
	}
	
	public StepPropertyModel getPathHead() {
		return pathTail == null ? null : pathTail.getPathHead();
	}

	/**
	 * Set the StepPropertyModel that represents the last step in the PathExpression 
	 * associated with this direct property.
	 * 
	 * @param stepPropertyModel
	 */
	public void setPathTail(StepPropertyModel stepPropertyModel) {
		this.pathTail = stepPropertyModel;
	}

	@Override
	protected void printProperties(PrettyPrintWriter out) {
		super.printProperties(out);
		if (pathTail != null) {
			out.beginObjectField("stepPropertyModel", pathTail);
			out.field("declaringShape.shape.id", pathTail.getDeclaringShape().getShape().getId());
			out.field("predicate", pathTail.getPredicate());
			out.field("stepIndex", pathTail.getStepIndex());
			out.endObjectField(pathTail);
		}
		
	}
	
}
