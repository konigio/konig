package io.konig.transform.rule;

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
import io.konig.shacl.Shape;

public class RenamePropertyRule extends AbstractPropertyRule {
	
	private URI focusPredicate;
	private PropertyConstraint sourceProperty;
	private int pathIndex;
	private ValueTransform valueTransform;

	public RenamePropertyRule(
		URI focusPredicate, 
		DataChannel channel, 
		PropertyConstraint sourceProperty,
		int pathIndex
	) {
		super(channel);
		this.focusPredicate = focusPredicate;
		this.sourceProperty = sourceProperty;
		this.pathIndex = pathIndex;
	}

	public int getPathIndex() {
		return pathIndex;
	}




	public PropertyConstraint getSourceProperty() {
		return sourceProperty;
	}



	@Override
	public URI getPredicate() {
		return focusPredicate;
	}

	@Override
	public DataChannel getDataChannel() {
		return channel;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {

		out.beginObjectField("sourceProperty", sourceProperty);
		out.field("predicate", sourceProperty.getPredicate());
		out.endObjectField(sourceProperty);
		
	}

	/**
	 * Get a rule that specifies how to transform values from the source channel to the target property.
	 */
	public ValueTransform getValueTransform() {
		return valueTransform;
	}

	/**
	 * Set a rule that specifies how to transform values from the source channel to the target property.
	 */
	public void setValueTransform(ValueTransform valueTransform) {
		this.valueTransform = valueTransform;
	}
	
	

}
