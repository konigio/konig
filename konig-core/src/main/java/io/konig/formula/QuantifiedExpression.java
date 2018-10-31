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


import java.util.List;

import io.konig.core.KonigException;
import io.konig.core.io.PrettyPrintWriter;

public class QuantifiedExpression extends Expression {
	private List<Triple> statementList;

	public static final QuantifiedExpression fromString(String text)  {
		try {
			FormulaParser parser = new FormulaParser();
			return parser.quantifiedExpression(text);
		} catch (Throwable oops) {
			throw new KonigException("Failed to parse expression: " + text, oops);
		}
	}

	public QuantifiedExpression() {
		
	}

	public String toSimpleString() {
		String[] text = toString().split("\\n");
		return text[text.length-1];
	}
	public static QuantifiedExpression wrap(ValueLogical valueLogical) {
		
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(valueLogical);
		
		ConditionalOrExpression or = new ConditionalOrExpression();
		or.add(and);
		
		return new QuantifiedExpression(or, null);
		
	}
	
	public static QuantifiedExpression wrap(NumericExpression numeric) {
		ValueLogical valueLogical = new BinaryRelationalExpression(null, numeric, null);
		
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(valueLogical);
		
		ConditionalOrExpression or = new ConditionalOrExpression();
		or.add(and);
		
		return new QuantifiedExpression(or, null);
		
	}
		
	public static QuantifiedExpression wrap(PrimaryExpression primary) {
		UnaryExpression unary = new UnaryExpression(primary);
		MultiplicativeExpression mult = new MultiplicativeExpression(unary);
		NumericExpression numeric = new GeneralAdditiveExpression(mult);
		ValueLogical valueLogical = new BinaryRelationalExpression(null, numeric, null);
		
		ConditionalAndExpression and = new ConditionalAndExpression();
		and.add(valueLogical);
		
		ConditionalOrExpression or = new ConditionalOrExpression();
		or.add(and);
		
		return new QuantifiedExpression(or, null);
	}
	
	public QuantifiedExpression(Expression formula, List<Triple> statementList) {
		super(formula);
		this.statementList = statementList;
	}
	
	public QuantifiedExpression(String text) {
		this(fromString(text));
	}
	

	public QuantifiedExpression(QuantifiedExpression clone) {
		super(clone);
		statementList = clone.statementList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		
		super.print(out);
		if (statementList!=null && !statementList.isEmpty()) {
			
			out.println();
			out.indent();
			out.println("WHERE");
			out.pushIndent();
			
			for (Triple s : statementList) {
				out.indent();
				out.println(s);
			}
			out.popIndent();
		}
	}




	protected void doDispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		if (statementList != null) {
			for (Triple s : statementList) {
				s.dispatch(visitor);
			}
		}
		visitor.exit(this);
	}
}
