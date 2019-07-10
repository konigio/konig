package io.konig.core.showl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2019 Gregory McFall
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


import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.OwlReasoner;

/**
 * An expression that evaluates to a single source property.
 * This expression is used to describe a one-to-one mapping between a source property 
 * and a target property.
 * @author Greg McFall
 *
 */
public abstract class ShowlPropertyExpression implements ShowlExpression {

	private ShowlPropertyShape sourceProperty;

	protected ShowlPropertyExpression(ShowlPropertyShape sourceProperty) {
		this.sourceProperty = sourceProperty;
	}
	
	public static ShowlPropertyExpression from(ShowlPropertyShape p) {
		if (p instanceof ShowlDirectPropertyShape) {
			return new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)p);
		} else {
			return new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape) p);
		}
	}

	/**
	 * Get the source property
	 */
	public ShowlPropertyShape getSourceProperty() {
		return sourceProperty;
	}

	@Override
	public String displayValue() {
		return sourceProperty.getPath();
	}
	
	public String toString() {
		return "ShowlPropertyExpression(" + sourceProperty.getPath() + ")";
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) throws ShowlProcessingException {
		if (sourceNodeShape.getRoot().equals(sourceProperty.getRootNode())) {
			set.add(sourceProperty);
		}
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		ShowlPropertyShape p = getSourceProperty();
		set.add(p);
//		if (p.getFormula() != null) {
//			p.getFormula().addProperties(set);
//		} else {
//			set.add(p);
//		}
	}
	

	@Override
	public ShowlExpression transform() {
		if (sourceProperty.getSelectedExpression() != null) {
			return sourceProperty.getSelectedExpression();
		}
		return this;
	}


	@Override
	public URI valueType(OwlReasoner reasoner) {
		ShowlPropertyShape p = sourceProperty.maybeDirect();
		return p.getValueType(reasoner);
	}

}
