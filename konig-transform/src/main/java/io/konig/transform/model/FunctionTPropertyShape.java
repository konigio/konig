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

import io.konig.formula.Expression;
import io.konig.formula.FunctionExpression;
import io.konig.shacl.PropertyConstraint;

public class FunctionTPropertyShape extends BaseTPropertyShape {
	private TExpressionFactory expressionFactory;
	private PropertyConstraint constraint;


	public FunctionTPropertyShape(TExpressionFactory expressionFactory, TNodeShape owner, PropertyConstraint constraint, boolean isTargetProperty) {
		super(owner);
		this.expressionFactory = expressionFactory;
		this.constraint = constraint;
		init(isTargetProperty);
	}

	@Override
	public URI getPredicate() {
		return constraint.getPredicate();
	}

	@Override
	public TProperty getValueExpressionGroup() {
		TProperty group = getPropertyGroup();
		return (group.getTargetProperty() != null) ? group : null;
	}

	public PropertyConstraint getConstraint() {
		return constraint;
	}

	@Override
	protected TExpression createValueExpression() throws ShapeTransformException {
		
		TFunctionExpression func = new TFunctionExpression(this);
		FunctionExpression fe = func.getFunctionExpression();
		
		for (Expression e : fe.getArgList()) {
			func.addArg(expressionFactory.createExpression(this, e));
		}
		return func;
	}

	@Override
	protected int doCountValues() {
		return 1;
	}

}
