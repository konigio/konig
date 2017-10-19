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

import io.konig.shacl.PropertyConstraint;

/**
 * A PropertyModel that represents one step in an equivalent path.
 * @author Greg McFall
 *
 */
public class StepPropertyModel extends PropertyModel {

	private int stepIndex;
	
	public StepPropertyModel(URI predicate, PropertyConstraint propertyConstraint, PropertyGroup group, int stepIndex) {
		super(predicate, propertyConstraint, group);
		this.stepIndex = stepIndex;
	}

	public int getStepIndex() {
		return stepIndex;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}


	protected void appendProperties(StringBuilder builder) {
		super.appendProperties(builder);
		builder.append(", stepIndex=");
		builder.append(stepIndex);
	}
}
