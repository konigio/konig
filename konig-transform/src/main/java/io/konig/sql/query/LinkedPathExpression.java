package io.konig.sql.query;

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


import io.konig.core.io.PrettyPrintWriter;

/**
 * An expression for a path to a column within a nested record.
 * @author Greg McFall
 *
 */
public class LinkedPathExpression extends AbstractExpression implements PathExpression {
	
	private PathExpression first;
	private LinkedPathExpression rest;

	public LinkedPathExpression(PathExpression first, LinkedPathExpression rest) {
		this.first = first;
		this.rest = rest;
	}
	
	public PathExpression getFirst() {
		return first;
	}

	public LinkedPathExpression getRest() {
		return rest;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		first.print(out);
		if (rest != null) {
			out.print('.');
			rest.print(out);
		}

	}

	@Override
	protected void dispatchProperties(QueryExpressionVisitor visitor) {
		visit(visitor, "first", first);
		visit(visitor, "rest", rest);
		
	}

}
