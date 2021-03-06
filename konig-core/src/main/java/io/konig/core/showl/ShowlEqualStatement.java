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

public class ShowlEqualStatement implements ShowlExpression, ShowlStatement {
	
	private ShowlExpression left;
	private ShowlExpression right;
	
	public ShowlEqualStatement(ShowlExpression left, ShowlExpression right) {
		this.left = left;
		this.right = right;
	}

	public ShowlExpression getLeft() {
		return left;
	}

	public ShowlExpression getRight() {
		return right;
	}
	
	public String toString() {
		return left.displayValue() + " = " + right.displayValue();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNode, Set<ShowlPropertyShape> set) {
		left.addDeclaredProperties(sourceNode, set);
		right.addDeclaredProperties(sourceNode, set);
	}

	public ShowlExpression otherExpression(ShowlExpression e) {
		
		return 
			left==e  ? right :
			right==e ? left :
			null;
	}
	
	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		left.addProperties(set);
		right.addProperties(set);
	}

	public ShowlExpression expressionOf(ShowlNodeShape node) {
		ShowlExpression e = expressionOf(node, left);
		if (e != null) {
			return e;
		}
		return expressionOf(node, right);
	}

	private ShowlExpression expressionOf(ShowlNodeShape node, ShowlExpression e) {
		if (e instanceof ShowlPropertyExpression) {
			ShowlPropertyExpression pe = (ShowlPropertyExpression)e;
			ShowlPropertyShape p = pe.getSourceProperty();
			if (node.encapsulates(p)) {
				return e;
			}
		}
		return null;
	}

	@Override
	public ShowlExpression transform() {
		return new ShowlEqualStatement(left.transform(), right.transform());
	}

	@Override
	public String displayValue() {
		return toString();
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		return XMLSchema.BOOLEAN;
	}

}
