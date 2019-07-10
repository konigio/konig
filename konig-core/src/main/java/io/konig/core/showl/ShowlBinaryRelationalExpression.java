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
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;
import io.konig.formula.BinaryOperator;

public class ShowlBinaryRelationalExpression implements ShowlExpression {

	private BinaryOperator operator;
	private ShowlExpression left;
	private ShowlExpression right;
	
	public ShowlBinaryRelationalExpression(BinaryOperator operator, ShowlExpression left, ShowlExpression right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}

	public BinaryOperator getOperator() {
		return operator;
	}

	public ShowlExpression getLeft() {
		return left;
	}

	public ShowlExpression getRight() {
		return right;
	}

	@Override
	public String displayValue() {
		
		StringBuilder builder = new StringBuilder();
		builder.append(left.displayValue());
		builder.append(' ');
		builder.append(operator.getText());
		builder.append(' ');
		builder.append(right.displayValue());
		
		return builder.toString();
	}
	
	@Override
	public String toString() {
		return displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		left.addDeclaredProperties(sourceNodeShape, set);
		right.addDeclaredProperties(sourceNodeShape, set);
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		left.addProperties(set);
		right.addProperties(set);
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return XMLSchema.BOOLEAN;
	}

	@Override
	public ShowlBinaryRelationalExpression transform() {
		
		return new ShowlBinaryRelationalExpression(operator, left.transform(), right.transform());
	}
	
	

}
