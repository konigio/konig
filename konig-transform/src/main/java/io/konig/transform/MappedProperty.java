package io.konig.transform;

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


import java.util.List;

import io.konig.core.Path;
import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A structure that maps a property of some source shape to a corresponding property on the target shape.
 * The corresponding property is defined by the {@link TransformAttribute} that contains this MappedProperty.
 * <p>
 * A MappedProperty has the following fields:
 * <ul>
 *   <li> targetContext: A string that identifies the path relative to the targetRoot where the sourceShape is utilized.
 *   <li> sourceShape: The Shape of the source entity.
 *   <li> property:  A property on the sourceShape that is mapped to a corresponding property on the target shape.
 *   <li> stepIndex: The index of the step within property.equivalentPath that is
 *            associated with the target property identified by the parent TransformAttribute. This value is
 *            -1 if there is a direct mapping between the sourceShape property and the target property.
 *   <li> template: An {@link IriTemplateInfo} that prescribes a method for producing the target property from
 *        the sourceShape via an IriTemplate.
 *   
 *   
 * </ul>
 * </p>
 * @author Greg McFall
 *
 */
public class MappedProperty extends AbstractPrettyPrintable {
	
	private ShapePath shapePath;
	private PropertyConstraint property;
	private IriTemplateInfo template;
	private ShapePath templateShape;
	private int stepIndex = -1;
	private List<PredicateValuePair> hasValue;
	
	public MappedProperty(ShapePath shapePath, PropertyConstraint property, int stepIndex) {
		this.shapePath = shapePath;
		this.property = property;
		this.stepIndex = stepIndex;
	}

	public MappedProperty(ShapePath shapePath, PropertyConstraint property) {
		this.shapePath = shapePath;
		this.property = property;
	}
	
	public String getTargetContext() {
		return shapePath.getPath();
	}
	
	public ShapePath getShapePath() {
		return shapePath;
	}

	public boolean isLeaf() {
		Path path = property.getEquivalentPath();
		return template==null && (path==null || stepIndex == path.length()-1);
	}

	public void setTemplateInfo(IriTemplateInfo template) {
		this.template = template;
	}

	public IriTemplateInfo getTemplateInfo() {
		return template;
	}

	public Shape getSourceShape() {
		return shapePath.getShape();
	}

	public PropertyConstraint getProperty() {
		return property;
	}

	public int getStepIndex() {
		return stepIndex;
	}
	
	

	public ShapePath getTemplateShape() {
		return templateShape;
	}

	public void setTemplateShape(ShapePath templateShape) {
		this.templateShape = templateShape;
	}

	public List<PredicateValuePair> getHasValue() {
		return hasValue;
	}

	public void setHasValue(List<PredicateValuePair> hasValue) {
		this.hasValue = hasValue;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		if (property != null) {
			out.field("property", property.getPredicate());
		}
		out.field("shapePath", shapePath);
		out.field("stepIndex", stepIndex);
		out.field("template", template);
		out.field("templateShape", templateShape);
		if (hasValue != null) {
			out.fieldName("hasValue");
			out.pushIndent();
			for (PredicateValuePair pair : hasValue) {
				out.beginObject(pair);
				out.field("predicate", pair.getPredicate());
				out.field("value", pair.getValue());
				out.endObject();
			}
			out.popIndent();
		}
		out.endObject();
	}
	
	public boolean isDerivedProperty() {
		return property!=null && property.getFormula()!=null;
	}
	
	public void utilize() {
		shapePath.decrementCount();
	}
	
}
