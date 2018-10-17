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

import io.konig.shacl.PropertyConstraint;

/**
 * A TPropertyShape corresponding to a PropertyConstraint with a simple PredicatePath
 * @author Greg McFall
 *
 */
public class SimpleTPropertyShape extends BaseTPropertyShape {
	private PropertyConstraint constraint;
	private TNodeShape tvalueShape;
	private boolean derived;

	public SimpleTPropertyShape(TNodeShape owner, PropertyConstraint constraint) {
		this(owner, constraint, false);
	}


	public SimpleTPropertyShape(TNodeShape owner, PropertyConstraint constraint, boolean derived) {
		super(owner);
		this.constraint = constraint;
		this.derived = derived;
		init();
	}


	@Override
	public URI getPredicate() {
		return constraint.getPredicate();
	}

	public PropertyConstraint getConstraint() {
		return constraint;
	}
	
	public boolean isDerived() {
		return derived;
	}


	@Override
	protected TExpression createValueExpression() {
		return new ValueOfExpression(this);
	}


	@Override
	public TProperty getValueExpressionGroup() {
		if (derived) {
			return null;
		}
		TProperty result = getPropertyGroup();
		if (result.getTargetProperty() == null) {
			result = null;
		}
		return result;
	}

	@Override
	public TNodeShape getValueShape() {
		return tvalueShape;
	}

	public void setValueShape(TNodeShape tvalueShape) {
		this.tvalueShape = tvalueShape;
	}


	@Override
	protected int doCountValues() {
		if (derived) {
			return 0;
		}
		return tvalueShape == null ? 1 : tvalueShape.countValues();
	}


	
}
