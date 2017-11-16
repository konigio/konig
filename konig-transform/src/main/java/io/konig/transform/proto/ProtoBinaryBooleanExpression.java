package io.konig.transform.proto;

import io.konig.core.io.BasePrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

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


import io.konig.transform.rule.TransformBinaryOperator;

public class ProtoBinaryBooleanExpression extends BasePrettyPrintable implements ProtoBooleanExpression {

	private TransformBinaryOperator operator;
	private PropertyModel left;
	private PropertyModel right;
	
	public ProtoBinaryBooleanExpression(TransformBinaryOperator operator, PropertyModel left, PropertyModel right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}
	
	public TransformBinaryOperator getOperator() {
		return operator;
	}
	
	public PropertyModel getLeft() {
		return left;
	}
	
	public PropertyModel getRight() {
		return right;
	}

	@Override
	protected void printProperties(PrettyPrintWriter out) {
		out.field("operator", operator);
		out.field("left", left);
		out.field("right", right);
		
	}
	
	

}
