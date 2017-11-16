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

import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

/**
 * @deprecated Use {@link PropertyComparison} instead.
 * @author Greg McFall
 *
 */
public class BinaryBooleanExpression extends AbstractPrettyPrintable implements BooleanExpression {

	private TransformBinaryOperator operator;
	private URI leftPredicate;
	private URI rightPredicate;
	
	public BinaryBooleanExpression(TransformBinaryOperator operator, URI leftPredicate, URI rightPredicate) {
		this.operator = operator;
		this.leftPredicate = leftPredicate;
		this.rightPredicate = rightPredicate;
	}
	public TransformBinaryOperator getOperator() {
		return operator;
	}
	public URI getLeftPredicate() {
		return leftPredicate;
	}
	public URI getRightPredicate() {
		return rightPredicate;
	}
	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("operator", operator.toString());
		out.field("leftPredicate", leftPredicate);
		out.field("rightPredicate", rightPredicate);
		out.endObject();
		
	}
	
	
}
