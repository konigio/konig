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

public class PropertyComparison extends AbstractPrettyPrintable implements BooleanExpression {
	
	private TransformBinaryOperator operator;
	private ChannelProperty left;
	private ChannelProperty right;

	

	public PropertyComparison(TransformBinaryOperator operator, ChannelProperty left, ChannelProperty right) {
		this.operator = operator;
		this.left = left;
		this.right = right;
	}



	public TransformBinaryOperator getOperator() {
		return operator;
	}



	public ChannelProperty getLeft() {
		return left;
	}



	public ChannelProperty getRight() {
		return right;
	}



	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("operator", operator);
		out.field("left", left);
		out.field("right", right);
		out.endObject();

	}

}
