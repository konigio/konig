package io.konig.transform.rule;

import io.konig.core.io.AbstractPrettyPrintable;
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


public class JoinRule extends AbstractPrettyPrintable implements FromItem {
	private FromItem left;
	private FromItem right;
	private BooleanExpression condition;
	
	public JoinRule(FromItem left, FromItem right, BooleanExpression condition) {
		this.left = left;
		this.right = right;
		this.condition = condition;
	}

	public FromItem getLeft() {
		return left;
	}
	
	public DataChannel getLeftChannel() {
		if (left instanceof DataChannel) {
			return (DataChannel) left;
		}
		return null;
	}
	
	public DataChannel getRightChannel() {
		if (right instanceof DataChannel) {
			return (DataChannel) right;
		}
		return null;
	}

	public FromItem getRight() {
		return right;
	}

	public BooleanExpression getCondition() {
		return condition;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		out.beginObject(this);
		out.field("left", left);
		out.field("condition", condition);
		out.field("right", right);
		
		out.endObject();
		
	}

	@Override
	public DataChannel primaryChannel() {
		return left instanceof DataChannel ? (DataChannel) left : null;
	}
	
	
}
