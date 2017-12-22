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

public class FormulaPropertyRule extends AbstractPropertyRule {

	private PropertyConstraint targetProperty;
	private PropertyConstraint sourceProperty;

	public FormulaPropertyRule(DataChannel channel, PropertyConstraint targetProperty, PropertyConstraint sourceProperty) {
		super(channel);
		this.targetProperty = targetProperty;
		this.sourceProperty = sourceProperty;
	}

	@Override
	public URI getPredicate() {
		return targetProperty.getPredicate();
	}

	public PropertyConstraint getSourceProperty() {
		return sourceProperty;
	}

	public PropertyConstraint getTargetProperty() {
		return targetProperty;
	}

	@Override
	protected void printLocalFields(PrettyPrintWriter out) {
		out.beginObject(sourceProperty);
		out.field("predicate", sourceProperty.getPredicate());
		out.field("formula", sourceProperty.getFormula().getText());
		out.endObject();

	}
	
	public FormulaPropertyRule clone() {
		FormulaPropertyRule clone = new FormulaPropertyRule(getDataChannel(), targetProperty, sourceProperty);
		return clone;
	}

}
