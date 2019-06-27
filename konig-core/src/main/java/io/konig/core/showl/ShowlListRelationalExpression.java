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


import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.OwlReasoner;

public class ShowlListRelationalExpression implements ShowlExpression {
	private ShowlPropertyShape referencedBy;
	private ShowlExpression left;
	private ShowlContainmentOperator operator;
	private List<ShowlExpression> right;

	public ShowlListRelationalExpression(ShowlPropertyShape referencedBy, ShowlExpression left,
			ShowlContainmentOperator operator, List<ShowlExpression> right) {
		this.referencedBy = referencedBy;
		this.left = left;
		this.operator = operator;
		this.right = right;
	}

	public ShowlContainmentOperator getOperator() {
		return operator;
	}

	public ShowlPropertyShape getReferencedBy() {
		return referencedBy;
	}

	public ShowlExpression getLeft() {
		return left;
	}

	public List<ShowlExpression> getRight() {
		return right;
	}

	@Override
	public String displayValue() {
		StringBuilder builder = new StringBuilder();
		builder.append(left.displayValue());
		builder.append(" IN ( ");
		
		String comma = "";
		for (ShowlExpression e : right) {
			builder.append(comma);
			comma = " , ";
			builder.append(e.displayValue());
		}
		
		builder.append(" )");
		
		return builder.toString();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set)
			throws ShowlProcessingException {
		left.addDeclaredProperties(sourceNodeShape, set);
		for (ShowlExpression e : right) {
			e.addDeclaredProperties(sourceNodeShape, set);
		}
		
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {

		left.addProperties(set);
		for (ShowlExpression e : right) {
			e.addProperties(set);
		}
		
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return XMLSchema.BOOLEAN;
	}

	@Override
	public ShowlListRelationalExpression transform() {
		return new ShowlListRelationalExpression(referencedBy, left.transform(), operator, ShowlUtil.transform(right));
	}

}
