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


import io.konig.core.io.AbstractPrettyPrintable;
import io.konig.core.io.PrettyPrintWriter;

public class JoinStatement0 extends AbstractPrettyPrintable {
	
	private DataChannel left;
	private DataChannel right;
	private BooleanExpression condition;
	
	public JoinStatement0(DataChannel left, DataChannel right, BooleanExpression condition) {
		this.left = left;
		this.right = right;
		this.condition = condition;
	}

	public DataChannel getLeft() {
		return left;
	}

	public DataChannel getRight() {
		return right;
	}

	public BooleanExpression getCondition() {
		return condition;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		
		out.beginObjectField("left", left);
		out.field("name", left.getName());
		out.endObjectField(left);

		out.beginObjectField("right", right);
		out.field("name", right.getName());
		out.endObjectField(right);
		
		out.field("condition", condition);
		out.endObject();
		
	}
	
	
}