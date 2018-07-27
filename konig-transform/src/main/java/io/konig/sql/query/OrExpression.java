package io.konig.sql.query;

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


import io.konig.core.io.PrettyPrintWriter;

public class OrExpression extends AbstractExpression implements BooleanTerm {
	
	private AndExpression left;
	private AndExpression right;
	

	public OrExpression(AndExpression left, AndExpression right) {
		this.left = left;
		this.right = right;
	}
	
	

	public AndExpression getLeft() {
		return left;
	}



	public AndExpression getRight() {
		return right;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		out.print(left);
		out.print(" OR ");
		out.print(right);

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visitor.visit(this, "left", left);
		visitor.visit(this, "right", right);
	}

}
