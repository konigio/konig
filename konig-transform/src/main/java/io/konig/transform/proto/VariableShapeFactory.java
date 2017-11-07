package io.konig.transform.proto;

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


import java.util.LinkedList;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;

import io.konig.core.UidGenerator;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.VariableTerm;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;

/**
 * A factory that produces a shape containing all properties accessed from a given variable.
 * @author Greg McFall
 *
 */
public class VariableShapeFactory {

	public VariableShapeFactory() {
		
	}

	public Shape createShape(Shape declaringShape, PropertyConstraint variable) throws ShapeTransformException {
		Shape result = new Shape(new BNodeImpl(UidGenerator.INSTANCE.next()));
		if (variable.getValueClass() instanceof URI) {
			URI targetClass = (URI) variable.getValueClass();
			result.setTargetClass(targetClass);

			Visitor visitor = new Visitor(variable, result);
			
			dispatch(declaringShape.getProperty(), visitor);
			
		} else {
			throw new ShapeTransformException("Expected valueClass to be a URI");
		}
		
		
		
		return result;
	}
	
	
	private void dispatch(List<PropertyConstraint> list, Visitor visitor) {
		if (list != null) {
			for (PropertyConstraint p : list) {
				Formula formula = p.getFormula();
				if (formula != null) {
					formula.dispatch(visitor);
				}
			}
		}
		
	}


	
	static class Visitor implements FormulaVisitor {

		private PropertyConstraint variable;
		private Shape variableShape;
		

		public Visitor(PropertyConstraint variable, Shape variableShape) {
			this.variable = variable;
			this.variableShape = variableShape;
		}

		@Override
		public void enter(Formula formula) {
			if (formula instanceof PathExpression) {
				PathExpression path = (PathExpression)formula;
				Shape sink = null;
				PropertyConstraint accessor = null;
				String varName = variable.getPredicate().getLocalName();
				for (PathStep step : path.getStepList()) {
					if (step instanceof DirectionStep) {
						DirectionStep dirStep = (DirectionStep) step;
						PathTerm term = dirStep.getTerm();
						if (term instanceof VariableTerm) {
							VariableTerm varTerm = (VariableTerm) term;
							if (varTerm.getVarName().equals(varName)) {
								sink = variableShape;
							} else {
								return;
							}
						} else {
							if (accessor != null) {
								sink = accessor.getShape();
								if (sink == null) {
									sink = new Shape();
									accessor.setShape(sink);
								}
							}
							
							if (sink == null) {
								return;
							}
							URI predicate = term.getIri();
							PropertyConstraint p = sink.getPropertyConstraint(predicate);
							if (p == null) {
								p = new PropertyConstraint(predicate);
								sink.add(p);
								accessor = p;
							}
						}
					}
					
				}
			}
		}

		@Override
		public void exit(Formula formula) {
			
			
		}
		
	}
}
