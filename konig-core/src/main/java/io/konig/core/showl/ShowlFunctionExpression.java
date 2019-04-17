package io.konig.core.showl;

import java.util.Set;

import org.openrdf.model.URI;

import io.konig.core.util.IriTemplate;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.FunctionExpression;
import io.konig.formula.PathTerm;

public class ShowlFunctionExpression implements ShowlExpression {

	private ShowlNodeShape declaringShape;
	private FunctionExpression function;

	public ShowlFunctionExpression(ShowlNodeShape declaringShape, FunctionExpression function) {
		this.declaringShape = declaringShape;
		this.function = function;
	}
	
	public static ShowlFunctionExpression fromIriTemplate(ShowlNodeShape declaringShape, IriTemplate template) {
		return new ShowlFunctionExpression(declaringShape, FunctionExpression.fromIriTemplate(template));
	}

	@Override
	public ShowlNodeShape rootNode() {
		return declaringShape.getRoot();
	}

	@Override
	public String displayValue() {
		return function.toSimpleString();
	}

	@Override
	public void addDeclaredProperties(ShowlNodeShape sourceNodeShape, Set<ShowlPropertyShape> set) {
		
		MyFormulaVisitor visitor = new MyFormulaVisitor(sourceNodeShape, set);
		function.dispatch(visitor);

	}
	
	public ShowlNodeShape getDeclaringShape() {
		return declaringShape;
	}

	public FunctionExpression getFunction() {
		return function;
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
						if (derived.getHasValue().isEmpty()) {
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

}
