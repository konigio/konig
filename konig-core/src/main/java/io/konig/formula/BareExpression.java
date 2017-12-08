package io.konig.formula;

/*
 * #%L
 * Konig Core
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

public class BareExpression extends ConditionalOrExpression {

	public BareExpression(ConditionalOrExpression e) {
		super(e);
	}
	
	@Override
	public void print(PrettyPrintWriter out) {
		printOrList(out);
	}
	
	public static BareExpression wrap(PrimaryExpression primary) {

		UnaryExpression unary = new UnaryExpression(primary);
		MultiplicativeExpression mult = new MultiplicativeExpression(unary);
		NumericExpression numeric = new GeneralAdditiveExpression(mult);
		ValueLogical valueLogical = new BinaryRelationalExpression(null, numeric, null);
		
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(valueLogical);
		
		ConditionalOrExpression or = new ConditionalOrExpression();
		or.add(and);
		
		return new BareExpression(or);
	}
}
