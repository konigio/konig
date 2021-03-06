package io.konig.core.showl;

import java.util.ArrayList;
import java.util.List;

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


import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Context;
import io.konig.core.OwlReasoner;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
import io.konig.core.util.IriTemplate;
import io.konig.core.util.ValueFormat.Element;
import io.konig.core.util.ValueFormat.ElementType;
import io.konig.core.vocab.SH;
import io.konig.formula.Formula;
import io.konig.formula.FormulaUtil;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.FunctionExpression;
import io.konig.formula.PathTerm;

public class ShowlFunctionExpression implements ShowlExpression {

	private ShowlPropertyShape declaringProperty;
	private FunctionExpression function;
	private List<ShowlExpression> arguments;

	public ShowlFunctionExpression(ShowlPropertyShape declaringProperty, FunctionExpression function) {
		this(declaringProperty, function, new ArrayList<>());
	}
	
	
	
	public ShowlFunctionExpression(ShowlPropertyShape declaringProperty, FunctionExpression function,
			List<ShowlExpression> arguments) {
		this.declaringProperty = declaringProperty;
		this.function = function;
		this.arguments = arguments;
	}



	public static ShowlExpression fromIriTemplate(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService, ShowlPropertyShape declaringProperty, IriTemplate template) {
		
		List<? extends Element> list = template.toList();
		
		if (list.size() == 1) {
			Element e = list.get(0);
			if (e.getType() == ElementType.VARIABLE) {
				Context c = template.getContext();
				URI predicate = new URIImpl(c.expandIRI(e.getText()));
				ShowlPropertyShape arg = declaringProperty.getDeclaringShape().findOut(predicate);
				if (arg != null) {
					arg = arg.maybeDirect();
					return ShowlUtil.propertyExpression(arg);
				}
			}
		}
		
		ShowlExpressionBuilder builder = new ShowlExpressionBuilder(schemaService, nodeService);
		return builder.functionExpression(declaringProperty,  FunctionExpression.fromIriTemplate(template));
	}
	
	public void addArgument(ShowlExpression arg) {
		arguments.add(arg);
	}

	@Override
	public String displayValue() {
		return function.toSimpleString();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) {
		
		MyFormulaVisitor visitor = new MyFormulaVisitor(sourceNodeShape, set);
		function.dispatch(visitor);
		
		for (ShowlExpression arg : arguments) {
			arg.addDeclaredProperties(sourceNodeShape, set);
		}

	}
	
	public ShowlNodeShape getDeclaringShape() {
		return declaringProperty.getDeclaringShape();
	}
	
	

	public ShowlPropertyShape getDeclaringProperty() {
		return declaringProperty;
	}

	public FunctionExpression getFunction() {
		return function;
	}

	public List<ShowlExpression> getArguments() {
		return arguments;
	}


	public String toString() {
		return FormulaUtil.simpleString(function);
	}

	static class MyFormulaVisitor implements FormulaVisitor {

		private ShowlNodeShape sourceNodeShape;
		private Set<ShowlPropertyShape> set;
		
		public MyFormulaVisitor(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) {
			this.sourceNodeShape = sourceNodeShape;
			this.set = set;
		}

		@Override
		public void enter(Formula formula) {
			if (formula instanceof PathTerm) {
				PathTerm term = (PathTerm) formula;
				URI predicate = term.getIri();
				
				ShowlPropertyShape p = sourceNodeShape.getProperty(predicate);
				if (p != null) {
					set.add(p);
				} else {
					for (ShowlDerivedPropertyShape derived : sourceNodeShape.getDerivedProperty(predicate)) {
						if (derived.getHasValueDeprecated().isEmpty()) {
							p = derived.getSynonym();
							if (p instanceof ShowlDirectPropertyShape) {
								set.add(p);
								return;
							} else {
								p = derived;
							}
						}
					}
					if (p != null) {
						set.add(p);
					} 
				}
			}
		}

		@Override
		public void exit(Formula formula) {
			
			
		}
		
	}

	@Override
	public void addProperties(Set<ShowlPropertyShape> set) {
		for (ShowlExpression e : arguments) {
			e.addProperties(set);
		}
		
	}

	@Override
	public URI valueType(OwlReasoner reasoner) {
		URI type = function.getModel().getReturnType().getRdfType();
		if (type == SH.IRI) {
			return XMLSchema.ANYURI;
		}
		return type;
	}

	@Override
	public ShowlFunctionExpression transform() {
		return new ShowlFunctionExpression(declaringProperty, function, ShowlUtil.transform(arguments));
	}

}
