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
	
	public Expression(String text) {
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
		
		if (context != null) {

			List<Term> termList = context.asList();
			int remainder = printNamespaces(out, termList);
			if (remainder>0) {
				printContext(out, termList);
			}
			
		}
		printOrList(out);
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
}
