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
import java.io.Reader;
import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;

import io.konig.core.Context;
import io.konig.core.KonigException;
import io.konig.core.LocalNameService;
import io.konig.core.Term;
import io.konig.core.Term.Kind;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.rio.turtle.NamespaceMap;
import io.konig.rio.turtle.SeaTurtleParser;


public class FormulaParser {
	
	private static final Set<String> KEYWORD = new HashSet<>();
	static {
		KEYWORD.add("IF");
		KEYWORD.add("COUNT");
		KEYWORD.add("SUM");
		KEYWORD.add("AVG");
		KEYWORD.add("DAY");
		KEYWORD.add("MONTH");
		KEYWORD.add("YEAR");
		KEYWORD.add("CONCAT");
		KEYWORD.add("STRPOS");
		KEYWORD.add("SUBSTR");
		KEYWORD.add("BOUND");
		KEYWORD.add("UNIX_TIME");
		KEYWORD.add("CASE");
		KEYWORD.add("WHEN");
		KEYWORD.add("THEN");
		KEYWORD.add("ELSE");
		KEYWORD.add("END");
		KEYWORD.add("DISTINCT");
		KEYWORD.add("IN");
		KEYWORD.add("NOT");
		KEYWORD.add("AND");
		KEYWORD.add("OR");
		KEYWORD.add("WHERE");
	}
	
	private PropertyOracle propertyOracle;
	private LocalNameService localNameService;
	private NamespaceMap namespaceMap;
	
	public FormulaParser() {
	}
	
	public QuantifiedExpression quantifiedExpression(String formula, URI...terms) throws RDFParseException, IOException {
		
		StringBuilder builder = new StringBuilder();
		for (URI term : terms) {
			builder.append("@term ");
			builder.append(term.getLocalName());
			builder.append(" <");
			builder.append(term.stringValue());
			builder.append(">\n");
		}
		builder.append(formula);
		
		return quantifiedExpression(builder.toString());
	}
	
	public FormulaParser(PropertyOracle propertyOracle) {
		this.propertyOracle = propertyOracle;
	}

	public FormulaParser(PropertyOracle propertyOracle, LocalNameService localNameService) {
		this.propertyOracle = propertyOracle;
		this.localNameService = localNameService;
	}
	public FormulaParser(PropertyOracle propertyOracle, LocalNameService localNameService, NamespaceMap nsMap) {
		this.propertyOracle = propertyOracle;
		this.localNameService = localNameService;
		this.namespaceMap = nsMap;
	}
	
	

	public PropertyOracle getPropertyOracle() {
		return propertyOracle;
	}

	public LocalNameService getLocalNameService() {
		return localNameService;
	}

	public NamespaceMap getNamespaceMap() {
		return namespaceMap;
	}

	public QuantifiedExpression quantifiedExpression(String text)  throws RDFParseException, IOException {
		StringReader reader = new StringReader(text);
		return quantifiedExpression(reader);
	}

	public QuantifiedExpression quantifiedExpression(Reader reader) throws RDFParseException, IOException {
		Worker worker = new Worker(reader);
		worker.setDefaultNamespaceMap(namespaceMap);
		
		try {
			return worker.quantifiedExpression();
		} catch (RDFHandlerException e) {
			throw new KonigException(e);
		}
	}

	public Expression expression(String text)  throws RDFParseException, IOException {
		StringReader reader = new StringReader(text);
		return expression(reader);
	}

	public Expression expression(Reader reader) throws RDFParseException, IOException {
		Worker worker = new Worker(reader);
		
		try {
			return worker.formula();
		} catch (RDFHandlerException e) {
			throw new KonigException(e);
		}
	}
	
	private class Worker extends SeaTurtleParser {
		
		private Worker(Reader reader) {
			initParse(reader, "");
		}

		private Expression formula() throws RDFParseException, IOException, RDFHandlerException {
			
			prologue();
			return expression();
		}

		private QuantifiedExpression quantifiedExpression() throws RDFParseException, IOException, RDFHandlerException {
			
			prologue();
			Expression e = expression();
//			List<Triple> statementList = whereClause();
			List<Triple> statementList = null;
			
			return new QuantifiedExpression(e, statementList);
		}
		
//		private List<Triple> whereClause() throws IOException, RDFParseException {
//			List<Triple> list = null;
//			skipSpace();
//			if (tryWord("WHERE")) {
//				list = new ArrayList<>();
//				Triple triple = null;
//				while ((triple=tryTriple()) != null) {
//					list.add(triple);
//				}
//			}
//			return list;
//		}

//		private Triple tryTriple() throws RDFParseException, IOException {
//			Triple triple = null;
//			PathTerm subject = tryPathTerm();
//			if (subject != null) {
//				PathTerm term = pathTerm();
//				if (!(term instanceof IriValue)) {
//					throw new RDFParseException("Expected curie, iri, or local name");
//				}
//				IriValue predicate = (IriValue) term;
//				PathTerm object = pathTerm();
//				assertNext('.');
//				triple = new Triple(subject, predicate, object);
//				
//			}
//			return triple;
//		}


		private Expression expression() throws RDFParseException, IOException, RDFHandlerException {
			
			Expression e = conditionalOrExpression();
			e.getContext().compile();
			return e;
		}
		
		private BareExpression expr() throws RDFParseException, RDFHandlerException, IOException {
			ConditionalOrExpression e = conditionalOrExpression();
			return new BareExpression(e);
		}

		private ConditionalOrExpression conditionalOrExpression() throws RDFParseException, IOException, RDFHandlerException {
			ConditionalOrExpression or = new ConditionalOrExpression();
			or.setContext(getContext());
			ConditionalAndExpression and = conditionalAndExpression();
			or.add(and);
			while ((and=tryConditionalAndExpression()) != null) {
				or.add(and);
			}
			return or;
		}

		private ConditionalAndExpression tryConditionalAndExpression() throws IOException, RDFParseException, RDFHandlerException {
			if (tryWord("WHERE")) {
				unread("WHERE");
				return null;
			}
			ConditionalAndExpression and = null;
			ValueLogical value = tryValueLogical();
			if (value != null) {
				and = new ConditionalAndExpression();
				and.add(value);
				skipSpace();
				while (tryWord("&&") || isWord("AND")) {
					value = valueLogical();
					and.add(value);
				}
			}
			return and;
		}
		
		private boolean isString(String text) throws IOException {
			StringBuilder buffer = buffer();
			for (int i=0; i<text.length(); ) {
				int k = read();
				if (k<0) {
					return false;
				}
				buffer.appendCodePoint(k);
				int c = text.codePointAt(i);
				if (c != k) {
					unread(buffer.toString());
					return false;
				}
				i += Character.charCount(k);
			}
			
			unread(buffer.toString());
			return true;
		}

		private boolean isWord(String word) throws IOException {
			
			if (tryWord(word)) {
				int c = peek();
				if (!Character.isLetterOrDigit(c) && c!='_') {
					return true;
				}
				unread(word);
			}
			return false;
		}

		private ValueLogical valueLogical() throws RDFParseException, IOException, RDFHandlerException {
			ValueLogical value = tryValueLogical();
			if (value == null) {
				fail("Expected ValueLogical");
			}
			return value;
		}

		private ValueLogical tryValueLogical() throws IOException, RDFParseException, RDFHandlerException {
			return tryRelationalExpression();
		}

		private RelationalExpression tryRelationalExpression() throws IOException, RDFParseException, RDFHandlerException {
			RelationalExpression e = null;
			NumericExpression left = tryNumericExpression();
			
			if (left != null) {
				e = 
					(e = tryListRelationalExpression(left))   !=null ? e :
					(e = tryConditionalExpression(left))      !=null ? e :
					(e = tryBinaryRelationalExpression(left)) !=null ? e :
					null;
			}
			
			return e;
			
		}

		private RelationalExpression tryConditionalExpression(NumericExpression condition) throws IOException, RDFParseException, RDFHandlerException {
			skipSpace();
			int c = read();
			if (c == '?') {
				skipSpace();
				NumericExpression whenTrue = numericExpression();
				skipSpace();
				read(':');
				skipSpace();
				NumericExpression whenFalse = numericExpression();
				return new ConditionalExpression(condition, whenTrue, whenFalse);
			}
			unread(c);
			return null;
		}

		private ListRelationalExpression tryListRelationalExpression(NumericExpression left) throws IOException, RDFParseException, RDFHandlerException {
			ListRelationalExpression list = null;
			skipSpace();

			ContainmentOperator operator = null;
			if (tryWord("IN")) {
				assertWhitespace();
				operator = ContainmentOperator.IN;
			} else if (tryWord("NOT")) {
				assertWhitespace();
				if (!tryWord("IN")) {
					fail("Expected 'IN'");
				}
				operator = ContainmentOperator.NOT_IN;
			}
			
			if (operator != null) {
				ExpressionList right = expressionList();
				list = new ListRelationalExpression(operator, left, right);
			}
			return list;
		}

		private ExpressionList expressionList() throws IOException, RDFParseException, RDFHandlerException {
			ExpressionList list = new ExpressionList();
			skipSpace();
			assertNext('(');
			skipSpace();
			
			Expression e = expression();
			list.add(e);
			
			for (;;) {
				skipSpace();
				int c = read();
				if (c == ',') {
					e = expression();
					list.add(e);
				} else if (c == ')') {
					break;
				} else {
					fail("Expected ',' or ')'");
				}
			}
			
			return list;
		}

		private BinaryRelationalExpression tryBinaryRelationalExpression(NumericExpression left) throws IOException, RDFParseException, RDFHandlerException {

			BinaryRelationalExpression binary = null;
			if (left != null) {
				BinaryOperator operator = null;
				NumericExpression right = null;
				int c = read();
				switch (c) {
				case '=' :
					operator = BinaryOperator.EQUALS;
					break;
					
				case '<' :
					operator = tryWord("=") ? BinaryOperator.LESS_THAN_OR_EQUAL : BinaryOperator.LESS_THAN;
					break;
					
				case '>' :
					operator = tryWord("=") ? BinaryOperator.GREATER_THAN_OR_EQUAL : BinaryOperator.GREATER_THAN;
					break;
					
				case '!' :
					operator = tryWord("=") ? BinaryOperator.NOT_EQUAL : null;
					break;
					
				}
				if (operator == null) {
					unread(c);
				} else {
					right = numericExpression();
				}
				
				binary = new BinaryRelationalExpression(operator, left, right);
			}
			return binary;
		}

		private NumericExpression numericExpression() throws RDFParseException, IOException, RDFHandlerException {
			NumericExpression numeric = tryNumericExpression();
			if (numeric == null) {
				fail("Expected a NumericExpression");
			}
			return numeric;
		}

		private NumericExpression tryNumericExpression() throws IOException, RDFParseException, RDFHandlerException {
			return tryAdditiveExpression();
		}

		private AdditiveExpression tryAdditiveExpression() throws IOException, RDFParseException, RDFHandlerException {
			GeneralAdditiveExpression expr = null;
			MultiplicativeExpression left = tryMultiplicativeExpression();
			if (left != null) {
				expr = new GeneralAdditiveExpression(left);
		
				for (;;) {
					skipSpace();

					int c = read();
					AdditiveOperator operator = null;
					switch (c) {
					case '+' :
						operator = AdditiveOperator.PLUS;
						break;
						
					case '-' :
						operator = AdditiveOperator.MINUS;
						break;
					}
					
					if (operator == null) {
						unread(c);
						break;
					}
					skipSpace();
					MultiplicativeExpression right = multiplicativeExpression();
					Addend addend = new Addend(operator, right);
					expr.add(addend);
				}
				
			}
			return expr;
		}

		private MultiplicativeExpression multiplicativeExpression() throws RDFParseException, IOException, RDFHandlerException {
			MultiplicativeExpression mult = tryMultiplicativeExpression();
			if (mult == null) {
				fail("Expected MultiplicativeExpression");
			}
			return mult;
		}

		private MultiplicativeExpression tryMultiplicativeExpression() throws IOException, RDFParseException, RDFHandlerException {
			MultiplicativeExpression mult = null;
			UnaryExpression left = tryUnaryExpression();
			if (left != null) {
				mult = new MultiplicativeExpression(left);
				for (;;) {
					MultiplicativeOperator operator = null;
					skipSpace();
					int c = read();
					switch(c) {
					case '*' :
						operator = MultiplicativeOperator.MULTIPLY;
						break;
						
					case '/' :
						operator = MultiplicativeOperator.DIVIDE;
						break;
						
					}
					
					if (operator == null) {
						unread(c);
						break;
					}
					
					UnaryExpression right = unaryExpression();
					mult.add(new Factor(operator, right));
					
				}
			}
			return mult;
		}

		private UnaryExpression unaryExpression() throws RDFParseException, IOException, RDFHandlerException {
			UnaryExpression unary = tryUnaryExpression();
			if (unary == null) {
				fail("Expected UnaryExpression");
			}
			return unary;
		}

		private UnaryExpression tryUnaryExpression() throws IOException, RDFParseException, RDFHandlerException {
			skipSpace();
			int c = read();
			UnaryOperator operator = null;
			if (c == '!') {
				int d = peek();
				if (d == '=') {
					return null;
				}
				operator = UnaryOperator.NOT;
			} else {
				unread(c);
			}
			PrimaryExpression primary = tryPrimaryExpression();
			if (operator != null && primary==null) {
				fail("Expected PrimaryExpression");
			}
			return primary==null ? null : new UnaryExpression(operator, primary);
		}

		private PrimaryExpression tryPrimaryExpression() throws IOException, RDFParseException, RDFHandlerException {
			PrimaryExpression primary = null;
			
			primary = 
				(primary=tryBrackettedExpression()) != null ? primary :
				(primary=tryBuiltInCall()) != null ? primary :
				(primary=tryCase()) != null ? primary :
				(primary=tryLiteralFormula()) != null ? primary :
				(primary=tryPath()) != null ? primary :
				(primary=tryIri()) != null ? primary :
				(primary=tryBNode()) != null ? primary :
				null;
			
			
			
			return primary;
		}

		
		private PrimaryExpression tryBNode() throws IOException, RDFParseException, RDFHandlerException {
			
			skipSpace();
			int c = peek();
			
			if (c == '[') {
				List<PredicateObjectList> constraints = predicateObjectList();
				return new BNodePrimaryExpression(constraints);
			}
			
			return null;
		}

		/**
		 * Return one of fully-qualified IRI, CURIE, or localName registered with the LocalNameService.
		 * @throws IOException 
		 * @throws RDFParseException 
		 * @throws RDFHandlerException 
		 */
		private PrimaryExpression tryIri() throws IOException, RDFParseException, RDFHandlerException {
			
			
			
			PrimaryExpression result = null;
			
			return
				(result=tryFullIri()) != null ? result :
				(result=tryCurieOrLocalName()) != null ? result :
				null;
		
		}

		private PathTerm tryCurieOrLocalName() throws IOException, RDFParseException {
			
			int c = next();
			if (Character.isLetter(c)) {
				
				StringBuilder buffer = buffer();
				do {
					buffer.appendCodePoint(c);
					c = read();
				} while (Character.isLetterOrDigit(c) || c=='_');
				String predicate = buffer.toString();
				if (c == ':') {
					String prefix = predicate;
					c = read();
					if (Character.isLetterOrDigit(c)) {
						buffer = buffer();
						do {
							buffer.appendCodePoint(c);
							c = read();
						} while (Character.isLetterOrDigit(c) || c=='_');
						String localName = buffer.toString();
						unread(c);
						Context context = getContext();
						boolean ok = context.getTerm(prefix)!= null;
						if (!ok && namespaceMap!=null) {
							String namespace = namespaceMap.get(prefix);
							if (namespace != null) {
								Term term = context.addTerm(prefix, namespace);
								term.setKind(Kind.NAMESPACE);
								ok = true;
							}
						}
						if (!ok) {
							throw new RDFParseException("Namespace not defined for prefix: " + prefix);
						}
						
						return new CurieValue(getContext(), prefix, localName);
					} else {
						fail("Expected a letter after ':' in CURIE");
					}
				} else {
					unread(c);
					if (KEYWORD.contains(predicate.toUpperCase())) {
						unread(predicate);
						return null;
					}
					
					String localName = predicate;
					Context context = getContext();
					
					Term term = context.getTerm(localName);
					
					if (term == null) {
						if (localNameService != null) {
							Set<URI> iriOptions = localNameService.lookupLocalName(localName);
							
							if (iriOptions.size()==1) {
								URI id = iriOptions.iterator().next();
								term = new Term(localName, id.stringValue(), Kind.ANY);
								context.add(term);
							} else if (iriOptions.isEmpty()) {
								String msg = MessageFormat.format("Local name not found: {0}", localName);
								fail(msg);
							} else {
								StringBuilder builder = new StringBuilder();
								builder.append("Local name \"");
								builder.append(localName);
								builder.append("\" is ambiguous.  Could be one of");
								for (URI iri : iriOptions) {
									builder.append("\n   ");
									builder.append(iri.stringValue());
								}
								fail(builder.toString());
							}
						}
					}
					
					
					return new LocalNameTerm(context, predicate);
				}
				
			} else {
				unread(c);
			}
			return null;
		}

		private PrimaryExpression tryFullIri() throws RDFParseException, IOException, RDFHandlerException {
			if (
				isString("<http://") ||
				isString("<https://") ||
				isString("<urn:") ||
				isString("<file:")
			) {				
				String iriText = iriRef();
				if (iriText.indexOf('{')>=0) {
					Context context = getContext();
					IriTemplate template = new IriTemplate(context, iriText);
					
					if (namespaceMap != null && localNameService!=null) {
						for (Element e : template.toList()) {
							if (e.getType() == ElementType.VARIABLE) {
								String text = e.getText();
								int colon = text.indexOf(':');
								if (colon > 0) {
									String prefix = text.substring(0, colon);
									String namespaceURI = namespaceMap.get(prefix);
									if (namespaceURI == null) {
										throw new RDFHandlerException("namespace prefix not defined: " + prefix);
									}
									context.add(new Term(prefix, namespaceURI, Kind.NAMESPACE));
									String localName = text.substring(colon+1);
									context.addTerm(localName, text);
								} else {
									
									Set<URI> set = localNameService.lookupLocalName(text);
									if (set.size() > 1) {
										StringBuilder builder = new StringBuilder();
										builder.append("Local name '");
										builder.append(text);
										builder.append("' is ambiguous.  Possible values include: ");
										for (URI uri : set) {
											builder.append("\n  ");
											builder.append(uri.stringValue());
										}
										throw new RDFHandlerException(builder.toString());
									} else if (set.isEmpty()) {
										throw new RDFHandlerException("Local name not known: " + text);
									}
									
									URI uri = set.iterator().next();
									context.addTerm(text, uri.stringValue());
								}
							}
						}
					}
					return new IriTemplateExpression(template);
				}
				return new FullyQualifiedIri(new URIImpl(iriText));
			}
			return null;
		}

		private IfFunction tryIfFunction() throws IOException, RDFParseException, RDFHandlerException {
			skipSpace();
			if (tryCaseInsensitiveWord("IF") != null){
				skipSpace();
				int c = read();
				if (c != '(') {
					unread(c);
				} else {
					skipSpace();
					Expression condition = expr();
					read(',');
					Expression whenTrue = expr();
					read(',');
					Expression whenFalse = expr();
					assertNext(')');
					
					return new IfFunction(condition, whenTrue, whenFalse);
				}
			}
			
			return null;
		}

		private BuiltInCall tryBuiltInCall() throws IOException, RDFParseException, RDFHandlerException {
			BuiltInCall call = 
			
			(call=tryIfFunction()) !=null ? call :
			(call=trySetFunction(FunctionModel.SUM)) != null ? call :
			(call=trySetFunction(FunctionModel.AVG)) != null ? call :
			(call=trySetFunction(FunctionModel.COUNT)) != null ? call :
			(call=tryGenericFunction(FunctionModel.DAY)) != null ? call :
			(call=tryGenericFunction(FunctionModel.MONTH)) != null ? call :
			(call=tryGenericFunction(FunctionModel.YEAR)) != null ? call :
			(call=tryGenericFunction(FunctionModel.CONCAT)) != null ? call :
			(call=tryGenericFunction(FunctionModel.SUBSTR)) != null ? call :
			(call=tryGenericFunction(FunctionModel.STRPOS)) != null ? call :
			(call=tryGenericFunction(FunctionModel.UNIX_TIME)) != null ? call :
			(call=tryGenericFunction(FunctionModel.IRI)) != null ? call :
			(call=tryGenericFunction(FunctionModel.STRIP_SPACES)) != null ? call :
			(call=tryGenericFunction(FunctionModel.INT)) != null ? call :
			(call=tryBoundFunction()) != null ? call :
				
			null;
			
			return call;
		}

		
		



		private SetFunctionExpression trySetFunction(FunctionModel model) throws IOException, RDFParseException, RDFHandlerException {
			skipSpace();
			String functionName = model.getName();
			String name = tryCaseInsensitiveWord(functionName);
			if (name != null) {
				int c = next();
				if (c != '(') {
					unread(c);
					unread(name);
				} else {
					skipSpace();
					boolean distinct = tryCaseInsensitiveWord("DISTINCT") != null;
					if (distinct) {
						skipSpace();
					}
					List<Expression> argList = argList();
					assertNext(')');
					return new SetFunctionExpression(model, distinct, argList);
				}
				
			}
			
			return null;
		}

		private BoundFunction tryBoundFunction() throws IOException, RDFParseException, RDFHandlerException {
			
			if (tryWord("BOUND")) {
				int c = next();
				if (c != '(') {
					unread(c);
					unread("BOUND");
				} else {
					PathExpression arg = tryPath();
					
					if (arg == null) {
						fail("Expected variable or path as argument to BOUND function");
					}
					assertNext(')');
					
					return new BoundFunction(arg);
						
				}
			}
			return null;
		}

		
		private BuiltInCall tryGenericFunction(FunctionModel model) throws IOException, RDFParseException, RDFHandlerException {
			skipSpace();
			String functionName = model.getName();
			String name = tryCaseInsensitiveWord(functionName);
			if (name != null) {
				int c = next();
				if (c != '(') {
					unread(c);
					unread(name);
				} else {
					skipSpace();
					List<Expression> argList = argList();
					assertNext(')');
					return new FunctionExpression(model, argList);
				}
				
			}
			
			return null;
		}

		private CaseStatement tryCase() throws IOException, RDFParseException, RDFHandlerException {
			skipSpace();
			String name = tryCaseInsensitiveWord("CASE");
			if (name != null) {
				
				Expression caseCondition = null;
				skipSpace();
				String when = tryCaseInsensitiveWord("WHEN");
				if (when == null) {
					caseCondition = expression();
				} else {
					unread(when);
				}
				
			
				List<WhenThenClause> whenThenList = whenThenList();
				Expression elseClause = null;
				if (tryCaseInsensitiveWord("ELSE") != null) {
					elseClause = expression();
				}
				
				skipSpace();
				assertIgnoreCase("END");
				
				return new CaseStatement(caseCondition, whenThenList, elseClause);
			}
			
			return null;
		}

		private List<WhenThenClause> whenThenList() throws IOException, RDFParseException, RDFHandlerException {
			
			List<WhenThenClause> list = new ArrayList<>();
			
			for (;;) {
			
				skipSpace();
				if (tryCaseInsensitiveWord("WHEN") != null) {
					skipSpace();
					Expression when = expression();
					skipSpace();
					assertIgnoreCase("THEN");
					Expression then = expression();
					list.add(new WhenThenClause(when, then));
				} else {
					break;
				}
			}
			
			if (list.isEmpty()) {
				throw new RDFParseException("CASE statement is missing WHEN...THEN clause");
			}
			
			return list;
			
		}

		private List<Expression> argList() throws IOException, RDFParseException, RDFHandlerException {
			List<Expression> result = new ArrayList<>();
			int c=0;
			do {
				Expression arg = expr();
				result.add(arg);
				c = next();
			} while (c==',');
			unread(c);
			return result;
		}

		private PathExpression tryPath() throws RDFParseException, IOException, RDFHandlerException {
			
			int next = next();
			
			if (next != '?' && next!='$') {
				unread(next);
				return null;
			}
			
			PathExpression path = new PathExpression();
			
			if (next == '?') {
				unread(next);
				VariableTerm var = variable();
				path.add(new DirectionStep(Direction.OUT, var));
			}
			
			next = peek();
			
			while (next=='^' || next=='.' || next=='[') {
				switch(next) {
				case '.' :
					path.add(outStep());
					break;
					
				case '^' :
					path.add(inStep());
					break;
					
				case '[' :
					path.add(hasStep());
				}
				skipSpace();
				next = peek();
			}
			
			return path;
		}

		private PathStep hasStep() throws RDFParseException, RDFHandlerException, IOException {

			List<PredicateObjectList> constraints = predicateObjectList();
			return new HasPathStep(constraints);
		}

		private PathStep inStep() throws RDFParseException, IOException {
			assertNext('^');
			return new DirectionStep(Direction.IN, pathTerm());
		}

		private PathStep outStep() throws RDFParseException, IOException {
			assertNext('.');
			return new DirectionStep(Direction.OUT, pathTerm());
			
		}

		


		private List<PredicateObjectList> predicateObjectList() throws IOException, RDFParseException, RDFHandlerException {
			assertNext('[');
			List<PredicateObjectList> list = new ArrayList<>();
			for (;;) {
				
				PathExpression path = tryPath();
				if (path == null) {
					IriValue predicate = iriValue();
					
					path = new PathExpression();
					path.add(new DirectionStep(Direction.OUT, (PathTerm) predicate));
				}
				ObjectList objectList = objectList();
				list.add(new PredicateObjectList(path, objectList));
				int next = next();
				if (next == ';') {
					skipSpace();
					next = peek();
					if (next == ']') {
						break;
					}
				} else if (next==']') {
					break;
				} else {
					throw new RDFParseException("Invalid predicateObjectList");
				}
			}
			return list;
		}

		private ObjectList objectList() throws RDFParseException, RDFHandlerException, IOException {
			List<Expression> list = new ArrayList<>();
			
			for (;;) {
				Expression e = expression();
				list.add(e);
				
				int next = next();
				if (next != ',') {
					unread(next);
					break;
				}
			}
			
			return new ObjectList(list);
		}

		private IriValue iriValue() throws RDFParseException, IOException {
			IriValue result = null;
			PathTerm term = tryPathTerm();
			if (term instanceof IriValue) {
				result = (IriValue) term;
			} else {
				fail("Expected IRI value");
			}
			return result;
		}


		private PathTerm pathTerm() throws RDFParseException, IOException {
			
			PathTerm predicate = tryPathTerm();
			if (predicate == null) {
				fail("Expected a predicate");
			}
			return predicate;
		}

		
		private PathTerm tryPathTerm() throws RDFParseException, IOException {
			String predicate = null;
			skipSpace();
			int c = read();
			
			if (c == '<') {
				String value = iriRef(c);
				return new FullyQualifiedIri(new URIImpl(value));
			}
			
			
			
			if (Character.isLetter(c)) {

				StringBuilder buffer = buffer();
				
				do {
					buffer.appendCodePoint(c);
					c = read();
				} while (Character.isLetter(c) || Character.isDigit(c) || c=='_');
				
				predicate = buffer.toString();
				
				if (c == ':') {
					
					c = read();
					if (Character.isLetter(c)) {
						buffer = buffer();
						do {
							buffer.appendCodePoint(c);
							c = read();
						} while (Character.isLetter(c) || Character.isDigit(c) || c=='_');
						String prefix = predicate;
						String localName = buffer.toString();
						unread(c);
						Context context = getContext();
						boolean ok = context.getTerm(prefix)!= null;
						if (!ok && namespaceMap!=null) {
							String namespace = namespaceMap.get(prefix);
							if (namespace != null) {
								Term term = context.addTerm(prefix, namespace);
								term.setKind(Kind.NAMESPACE);
								ok = true;
							}
						}
						if (!ok) {
							throw new RDFParseException("Namespace not defined for prefix: " + prefix);
						}
						
						return new CurieValue(getContext(), prefix, localName);
					} else {
						fail("Expected a letter");
					}
				}
				
			} 

			
			unread(c);
			if ("IN".equalsIgnoreCase(predicate)) {
				unread(predicate);
				predicate = null;
			} else if ("NOT".equalsIgnoreCase(predicate)) {
				unread(predicate);
				predicate = null;
			} else if ("THEN".equalsIgnoreCase(predicate)) {
				unread(predicate);
				predicate = null;
			} else if ("WHEN".equalsIgnoreCase(predicate)) {
				unread(predicate);
				predicate = null;
			} else if ("ELSE".equalsIgnoreCase(predicate)) {
				unread(predicate);
				predicate = null;
			} else if ("END".equalsIgnoreCase(predicate)) {
				unread(predicate);
				predicate = null;
			}

			if (predicate == null) {
				return null;
			}

			String localName = predicate;
			Context context = getContext();
			
			Term term = context.getTerm(localName);
			
			if (term == null) {
				if (localNameService != null) {
					Set<URI> iriOptions = localNameService.lookupLocalName(localName);
					
					if (iriOptions.size()==1) {
						URI id = iriOptions.iterator().next();
						term = new Term(localName, id.stringValue(), Kind.ANY);
						context.add(term);
					} else if (iriOptions.isEmpty()) {
						String msg = MessageFormat.format("Local name not found: {0}", localName);
						throw new RDFParseException(msg);
					} else {
						StringBuilder builder = new StringBuilder();
						builder.append("Local name \"");
						builder.append(localName);
						builder.append("\" is ambiguous.  Could be one of");
						for (URI iri : iriOptions) {
							builder.append("\n   ");
							builder.append(iri.stringValue());
						}
						throw new RDFParseException(builder.toString());
					}
				}
			}
			
			
			return new LocalNameTerm(context, predicate);
		}
		

		private VariableTerm variable() throws RDFParseException, IOException {
			assertNext('?');
			buffer = buffer();
			int c = read();
			do {
				buffer.appendCodePoint(c);
				c = read();
			} while (Character.isLetter(c) || Character.isDigit(c) || c=='_');
			unread(c);
			String varName = buffer.toString();

			Context context = getContext();
			String termName = varName;
			Term term = context.getTerm(termName);
			if (term != null) {
				String iri = context.expandIRI(termName);
				return new VariableTerm(varName, new URIImpl(iri));
			}
			if (localNameService != null) {
				Set<URI> set = localNameService.lookupLocalName(termName);
				if (set.size() == 1) {
					URI varId = set.iterator().next();
					context.addTerm(termName, varId.stringValue());
					return new VariableTerm(varName, varId);
				}
				
			}
			VariableTerm result = new VariableTerm(varName);
			context.addTerm(varName, result.getIri().stringValue());
			return result;
		}

		private LiteralFormula tryLiteralFormula() throws RDFParseException, RDFHandlerException, IOException {
			Literal literal = tryLiteral();
			return literal == null ? null : new LiteralFormula(literal);
		}
		private BrackettedExpression tryBrackettedExpression() throws IOException, RDFParseException, RDFHandlerException {
			BrackettedExpression bracket = null;
			skipSpace();
			int c = read();
			if (c == '(') {
				skipSpace();
				Expression e = expression();
				skipSpace();
				assertNext(')');
				bracket = new BrackettedExpression(e);
			} else {
				unread(c);
			}
			return bracket;
		}


		private ConditionalAndExpression conditionalAndExpression() throws RDFParseException, IOException, RDFHandlerException {
			ConditionalAndExpression and = tryConditionalAndExpression();
			if (and == null) {
				fail("Expected ConditionalAndExpression" );
			}
			return and;
		}
	}
}
