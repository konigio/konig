package io.konig.transform.proto;

import java.util.ArrayList;
import java.util.List;

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
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.formula.Direction;

/**
 * A PropertyModel that represents one step in an equivalent path.
 * @author Greg McFall
 *
 */
public class StepPropertyModel extends BasicPropertyModel {

	private int stepIndex;
	private Direction direction;
	private DirectPropertyModel declaringProperty;
	private StepPropertyModel nextStep;
	private StepPropertyModel previousStep;
	private List<PredicateValuePair> filter;
	
	private List<SourceShapeInfo> valueShapeInfo;
	private ClassModel valueClassModel;
	
	public StepPropertyModel(URI predicate, Direction direction, PropertyGroup group, DirectPropertyModel declaringProperty, int stepIndex) {
		super(predicate, group, declaringProperty.getPropertyConstraint());
		this.direction = direction;
		this.declaringProperty = declaringProperty;
		this.stepIndex = stepIndex;
	}
	
	public Direction getDirection() {
		return direction;
	}

	public int getStepIndex() {
		return stepIndex;
	}
	
	public StepPropertyModel getPathHead() {
		return previousStep == null ? this : previousStep.getPathHead();
	}
	
	public ClassModel getValueClassModel() {
		return valueClassModel;
	}

	public void setValueClassModel(ClassModel valueClassModel) {
		this.valueClassModel = valueClassModel;
	}

	public void addValueShapeInfo(SourceShapeInfo info) {
		if (valueShapeInfo==null) {
			valueShapeInfo = new ArrayList<>();
		}
		for (SourceShapeInfo n : valueShapeInfo) {
			if (n.getSourceShape() == info.getSourceShape()) {
				return;
			}
		}
		valueShapeInfo.add(info);
	}

	public List<SourceShapeInfo> getValueShapeInfo() {
		return valueShapeInfo;
	}

	public void setValueShapeInfo(List<SourceShapeInfo> valueShapeInfo) {
		this.valueShapeInfo = valueShapeInfo;
	}

	public StepPropertyModel getPreviousStep() {
		return previousStep;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}


	public StepPropertyModel getNextStep() {
		return nextStep;
	}

	public void setNextStep(StepPropertyModel nextStep) {
		this.nextStep = nextStep;
		if (nextStep != null) {
			nextStep.previousStep = this;
		}
	}

	public List<PredicateValuePair> getFilter() {
		return filter;
	}

	public void setFilter(List<PredicateValuePair> filter) {
		this.filter = filter;
	}
	

	public DirectPropertyModel getDeclaringProperty() {
		return declaringProperty;
	}

	@Override
	protected void printProperties(PrettyPrintWriter out) {

		super.printProperties(out);
		out.field("stepIndex", stepIndex);
		out.field("nextStep", nextStep);
		out.beginObjectField("declaringProperty", declaringProperty);
		out.field("propertyConstraint.predicate", declaringProperty.getPropertyConstraint().getPredicate());
		out.field("declaringShape.shape.id", declaringProperty.getDeclaringShape().getShape().getId());
		out.endObjectField(declaringProperty);
		
	}
}
