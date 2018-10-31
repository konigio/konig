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


import java.util.ArrayList;
import java.util.List;

import io.konig.core.impl.RdfUtil;

abstract public class BaseTPropertyShape implements TPropertyShape {
	
	private TNodeShape owner;
	private TExpression valueExpression;
	private TProperty group;
	
	
	public BaseTPropertyShape(TNodeShape owner) {
		this.owner = owner;
	}
	
	protected void init(boolean isTarget) {

		owner.add(this);
		TProperty group = owner.getTclass().produceOut(getPredicate());
		if (isTarget) {
			group.setTargetProperty(this);
		} else {
			group.addCandidateSourceProperty(this);
		}
	}

	@Override
	public TNodeShape getOwner() {
		return owner;
	}


	@Override
	public TProperty getPropertyGroup() {
		return group;
	}

	@Override
	public void setPropertyGroup(TProperty group) {
		this.group = group;
	}

	@Override
	public TExpression getValueExpression() throws ShapeTransformException {
		if (valueExpression == null) {
			valueExpression = createValueExpression();
		}
		return valueExpression;
	}
	
	public String toString() {
		return getClass().getSimpleName() + "[" + getPath() + "]";
	}


	@Override
	public TNodeShape getValueShape() {
		return null;
	}

	@Override
	public int countValues() {
		TProperty group = getValueExpressionGroup();
		if (group!=null && group.getValueExpression()==null) {
			return doCountValues();
		}
		return 0;
	}

	@Override
	public TProperty assignValue() throws ShapeTransformException {
		TProperty group = getValueExpressionGroup();
		if (group!=null && group.getValueExpression() == null) {
			TExpression value = createValueExpression();
			group.setValueExpression(value);
			
			return group;
		}
		
		return null;
		
	}
	
	abstract protected TExpression createValueExpression() throws ShapeTransformException;
	abstract protected int doCountValues();

	@Override
	public String getPath() {
		StringBuilder builder = new StringBuilder();
		List<String> list = new ArrayList<>();
		TPropertyShape p = this;
		for (;;) {
			list.add(p.getPredicate().getLocalName());
			if (p.getOwner().getAccessor()==null) {
				builder.append(RdfUtil.localName(p.getOwner().getShape().getId()));
				break;
			}
			p = p.getOwner().getAccessor();
		}
		
		for (int i=list.size()-1; i>=0; i--) {
			String value = list.get(i);
			builder.append('.');
			builder.append(value);
		}
		
		return builder.toString();
	}
}
