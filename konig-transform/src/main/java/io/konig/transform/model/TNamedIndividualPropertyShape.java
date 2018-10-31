package io.konig.transform.model;

/*
 * #%L
 * Konig Transform
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


import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Vertex;

public class TNamedIndividualPropertyShape extends BaseTPropertyShape {

	private URI predicate;
	private List<Vertex> individuals;
	private TPropertyShape keyProperty;
	
	public TNamedIndividualPropertyShape(TNodeShape owner, URI predicate, TPropertyShape keyProperty, List<Vertex> individuals) {
		super(owner);
		this.predicate = predicate;
		init(false);
		this.individuals = individuals;
		this.keyProperty = keyProperty;
	}

	@Override
	public URI getPredicate() {
		return predicate;
	}

	public TPropertyShape getKeyProperty() {
		return keyProperty;
	}

	@Override
	public TProperty getValueExpressionGroup() {

		TProperty result = getPropertyGroup();
		if (result.getTargetProperty() == null) {
			result = null;
		}
		return result;
	}

	@Override
	protected TExpression createValueExpression() throws ShapeTransformException {
		return new TReferenceDataExpression(this, keyProperty, individuals);
	}

	@Override
	protected int doCountValues() {
		return 1;
	}

}
