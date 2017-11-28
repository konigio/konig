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

import java.util.List;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.UidGenerator;
import io.konig.core.impl.RdfUtil;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.HasPathStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PredicateObjectList;
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
	private static Logger logger = LoggerFactory.getLogger(VariableShapeFactory.class);
	
	private static final String VARIABLE_SHAPE_BASE_URI = "urn:konig:variable/";

	public VariableShapeFactory() {
		
	}

	public Shape createShape(Shape declaringShape, PropertyConstraint variable) throws ShapeTransformException {
		
		logger.debug("BEGIN createShape({}, {})", RdfUtil.localName(declaringShape.getId()), RdfUtil.localName(variable.getPredicate()));
		
		URI targetClass = targetClass(variable);
		
		Shape result = new Shape(shapeId(targetClass));
		if (targetClass != null) {
			result.setTargetClass(targetClass);

			Visitor visitor = new Visitor(variable, result);
			
			dispatch(declaringShape.getProperty(), visitor);
			
		} else {
			throw new ShapeTransformException("Expected valueClass to be a URI");
		}
		
		
		if (logger.isDebugEnabled()) {
			endCreateShape(result);
		}
		return result;
	}
	
	
	private void endCreateShape(Shape result) {
		String summary = result.summaryText();
		logger.debug("END createShape ==> {}", summary);
		
	}

	private URI targetClass(PropertyConstraint variable) {
		Resource valueClass = variable.getValueClass();
		if (valueClass == null) {

			Shape valueShape = variable.getShape();
			if (valueShape!=null) {
				valueClass = valueShape.getId();
			}
		}
		return valueClass instanceof URI ? (URI) valueClass : null;
	}

	private Resource shapeId(URI valueClass) {
		StringBuilder builder = new StringBuilder();
		builder.append(VARIABLE_SHAPE_BASE_URI);
		
		if (valueClass != null) {
			URI valueClassId = (URI) valueClass;
			builder.append(valueClassId.getLocalName());
			builder.append('/');
		}
		
		builder.append(UidGenerator.INSTANCE.next());
		return new URIImpl(builder.toString());
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
				StringBuilder pathText = new StringBuilder(variableShape.getId().stringValue());
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
							}
							accessor = p;
						}
					} else if (step instanceof HasPathStep) {
						HasPathStep hasStep = (HasPathStep) step;
						Shape nestedShape = nestedShape(pathText, accessor);
						for (PredicateObjectList pol : hasStep.getConstraints()) {
							URI predicate = pol.getVerb().getIri();
							PropertyConstraint p = sink.getPropertyConstraint(predicate);
							if (p == null) {
								p = new PropertyConstraint(predicate);
								nestedShape.add(p);
							}
						}
					}
					
				}
			}
		}

		private Shape nestedShape(StringBuilder pathText, PropertyConstraint accessor) {

			pathText.append('/');
			pathText.append(accessor.getPredicate().getLocalName());
			URIImpl shapeId = new URIImpl(pathText.toString());
			Shape shape = new Shape(shapeId);
			accessor.setShape(shape);
			return shape;
		}

		@Override
		public void exit(Formula formula) {
			
			
		}
		
	}
}
