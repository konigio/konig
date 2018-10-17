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


import org.openrdf.model.URI;

import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;

public class TIdPropertyShape extends BaseTPropertyShape implements TPropertyShape {

	private TExpressionFactory expressionFactory;
	
	public TIdPropertyShape(TExpressionFactory expressionFactory, TNodeShape owner) {
		super(owner);
		this.expressionFactory = expressionFactory;
		init();
	}

	@Override
	public URI getPredicate() {
		return Konig.id;
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
		
		IriTemplate template = getOwner().getShape().getIriTemplate();
		if (template == null) {
			return new ValueOfExpression(this);
		}
		
		return expressionFactory.createIriTemplate(this, template);
	}

	@Override
	protected int doCountValues() {
		return 1;
	}

}
