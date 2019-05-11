package io.konig.formula;

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


import java.util.ArrayList;

public class BasicExpression extends Expression {

	public BasicExpression(String text) {
		super(text);
	}

	public BasicExpression() {
	}

	public BasicExpression(Expression e) {
		super(e);
	}

	@Override
	public BasicExpression clone() {
		BasicExpression other = new BasicExpression();
		other.context = context;
		if (orList != null) {
			other.orList = new ArrayList<>();
			for (ConditionalAndExpression e : orList) {
				other.orList.add(e.clone());
			}
		}
		return other;
	}

}
