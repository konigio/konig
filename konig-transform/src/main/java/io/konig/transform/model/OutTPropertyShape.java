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

import io.konig.formula.DirectionStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;

public class OutTPropertyShape extends BaseTPropertyShape implements TPathPropertyShape {

	private DirectionStep step;
	private SimpleTPropertyShape baseProperty;
	private TNodeShape valueShape;
	private TPathPropertyShape next;
	
	public OutTPropertyShape(TNodeShape owner, DirectionStep step, SimpleTPropertyShape baseProperty, boolean isTargetProperty) {
		super(owner);
		this.step = step;
		this.baseProperty = baseProperty;
		init(isTargetProperty);
	}

	@Override
	public URI getPredicate() {
		return step.getTerm().getIri();
	}

	@Override
	protected TExpression createValueExpression() {
		return (next == null) ?
			new ValueOfExpression(baseProperty) :
			new TStructExpression(this);
	}

	@Override
	public TProperty getValueExpressionGroup() {
		
		TProperty group = getPropertyGroup();
		if (group.getTargetProperty() != null) {
			return group;
		}
		
		group =  baseProperty.getPropertyGroup();
		return group.getTargetProperty()==null ? null : group;
	}

	@Override
	protected int doCountValues() {

		PathExpression path = PathExpression.toPathExpression(baseProperty.getConstraint().getFormula());
		List<PathStep> list = path.getStepList();
		if (list.get(list.size()-1) == step) {
			return 1;
		}
		return 0;
	}


	@Override
	public TNodeShape getValueShape() {
		return valueShape;
	}

	public void setValueShape(TNodeShape valueShape) {
		this.valueShape = valueShape;
	}
	
	@Override
	public TPathPropertyShape getNext() {
		return next;
	}

	@Override
	public void setNext(TPathPropertyShape next) {
		this.next = next;
	}

	public SimpleTPropertyShape getBaseProperty() {
		return baseProperty;
	}

}
