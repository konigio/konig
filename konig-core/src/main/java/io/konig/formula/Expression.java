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


import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.io.PrettyPrintWriter;

public class Expression extends AbstractFormula {

	protected List<ConditionalAndExpression> orList;
	protected Context context;
	
	protected Expression(String text) {
		FormulaParser parser = new FormulaParser();
		try {
			Expression self = parser.expression(text);
			orList = self.getOrList();
			context = self.getContext();
			
		} catch (RDFParseException | IOException e) {
			throw new KonigException("Failed to parse Expression: " + text, e);
		}
	}
	
	public Expression() {
		 orList = new ArrayList<>();
	}
	
	protected void doClone(Context context, List<ConditionalAndExpression> orList) {
		this.context = context.deepClone();
		this.orList = new ArrayList<>();
		for (ConditionalAndExpression and : orList) {
			this.orList.add(and.deepClone());
		}
	}
	
	protected Expression(Expression e) {
		context = e.context;
		orList = e.orList;
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public void add(ConditionalAndExpression expr) {
		orList.add(expr);
	}

	public List<ConditionalAndExpression> getOrList() {
		return orList;
	}

	@Override
	public void print(PrettyPrintWriter out) {
		Context lastWrittenContext = out.getLastWrittenContext();
		if (context != null && !out.isSuppressContext() && context!=lastWrittenContext) {
			out.setLastWrittenContext(context);
			
			List<Term> termList = context.asList();
			int remainder = printNamespaces(out, termList);
			if (remainder>0) {
				printContext(out, termList);
			}
			
		}
		printOrList(out);
		out.setLastWrittenContext(lastWrittenContext);
		
	}
	

	public String getText() {
		StringWriter buffer = new StringWriter();
		PrettyPrintWriter out = new PrettyPrintWriter(buffer);
		printOrList(out);
		out.close();
		return buffer.toString();
	}
	
	protected void printOrList(PrettyPrintWriter out) {

		String operator = "";
		for (ConditionalAndExpression e : orList) {
			out.print(operator);
			e.print(out);
			operator = " || ";
		}
	}
	


	private void printContext(PrettyPrintWriter out, List<Term> termList) {
		
		out.print("@context {");
		out.pushIndent();
		
		String comma = "";
		for (Term term : termList) {
			if (term.getKind() != Kind.NAMESPACE ||
				term.getContainer()!=null ||
				term.getLanguage()!=null ||
				term.getType()!=null
			) {
				out.println(comma);
				term.print(out);
				comma = ",";
			}
		}
		out.println();
		out.popIndent();
		out.println('}');
		
	}

	private int printNamespaces(PrettyPrintWriter out, List<Term> termList) {
		int count = 0;
		for (Term term : termList) {
			if (term.getKind() == Kind.NAMESPACE && 
				term.getContainer()==null &&
				term.getLanguage()==null &&
				term.getType()==null
			) {
				
				out.print("@prefix ");
				out.print(term.getKey());
				out.print(": <");
				out.print(term.getExpandedIdValue());
				out.println("> .");
			} else {
				count++;
			}
		}
		return count;
	}

	public Value toValue() {
		String text = toString();
		return new LiteralImpl(text);
	}

	@Override
	public void dispatch(FormulaVisitor visitor) {
		visitor.enter(this);
		for (ConditionalAndExpression and : orList) {
			and.dispatch(visitor);
		}
		doDispatch(visitor);
		visitor.exit(this);
	}

	protected void doDispatch(FormulaVisitor visitor) {
		// Derived classes should override.
	}
	
	public PathExpression pathExpression() {
		PrimaryExpression primary = asPrimaryExpression();
		return primary instanceof PathExpression ? (PathExpression) primary : null;
	}
	
	/**
	 * Get the PrimaryExpression wrapped by this Expression.
	 * @return The PrimaryExpression wrapped by this Expression, or null if there is no single, unadorned 
	 * PrimaryExpression wrapped by this Expression.
	 */
	public PrimaryExpression asPrimaryExpression() {
		List<ConditionalAndExpression> orList = getOrList();
		if (orList.size()==1) {
			ConditionalAndExpression and = orList.get(0);
			List<ValueLogical> andList = and.getAndList();
			if (andList.size()==1) {
				ValueLogical value = andList.get(0);
				if (value instanceof BinaryRelationalExpression) {
					BinaryRelationalExpression binary = (BinaryRelationalExpression) value;
					if (binary.getRight() == null) {
						NumericExpression left = binary.getLeft();
						if (left instanceof GeneralAdditiveExpression) {
							GeneralAdditiveExpression additive = (GeneralAdditiveExpression) left;
							if (additive.getAddendList()==null || additive.getAddendList().isEmpty()) {
								MultiplicativeExpression mult = additive.getLeft();
								if (mult.getMultiplierList()==null || mult.getMultiplierList().isEmpty()) {
									UnaryExpression unary = mult.getLeft();
									if (unary.getOperator()==null) {
										return unary.getPrimary();
									}
								}
							}
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public Expression deepClone() {
		Expression clone = new Expression();
		clone.doClone(context, orList);
		return clone;
	}

}
