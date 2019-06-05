package io.konig.core.showl.expression;

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


import java.text.MessageFormat;
import java.util.List;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Vertex;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlDerivedPropertyExpression;
import io.konig.core.showl.ShowlDerivedPropertyShape;
import io.konig.core.showl.ShowlDirectPropertyExpression;
import io.konig.core.showl.ShowlDirectPropertyShape;
import io.konig.core.showl.ShowlEnumIndivdiualReference;
import io.konig.core.showl.ShowlExpression;
import io.konig.core.showl.ShowlFilterExpression;
import io.konig.core.showl.ShowlFunctionExpression;
import io.konig.core.showl.ShowlIdRefPropertyShape;
import io.konig.core.showl.ShowlInwardPropertyShape;
import io.konig.core.showl.ShowlIriReferenceExpression;
import io.konig.core.showl.ShowlNodeShape;
import io.konig.core.showl.ShowlNodeShapeService;
import io.konig.core.showl.ShowlOutwardPropertyShape;
import io.konig.core.showl.ShowlProcessingException;
import io.konig.core.showl.ShowlProperty;
import io.konig.core.showl.ShowlPropertyExpression;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlSchemaService;
import io.konig.core.showl.ShowlUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.BareExpression;
import io.konig.formula.ConditionalOrExpression;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Expression;
import io.konig.formula.Formula;
import io.konig.formula.FormulaUtil;
import io.konig.formula.FunctionExpression;
import io.konig.formula.HasPathStep;
import io.konig.formula.IriTemplateExpression;
import io.konig.formula.IriValue;
import io.konig.formula.LiteralFormula;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PredicateObjectList;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.VariableTerm;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShowlExpressionBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowlExpressionBuilder.class);

	private ShowlSchemaService schemaService;
	private ShowlNodeShapeService nodeService;

	public ShowlExpressionBuilder(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService) {
		this.schemaService = schemaService;
		this.nodeService = nodeService;
	}


	public ShowlExpression expression(ShowlPropertyShape p, Formula formula) throws ShowlProcessingException {
		if (formula instanceof FunctionExpression) {
			return functionExpression(p, (FunctionExpression) formula);
		} 
		if (formula instanceof QuantifiedExpression) {
			return quantifiedExpression(p, (QuantifiedExpression) formula);
		} 
		if (formula instanceof ConditionalOrExpression) {
			return conditionalOr(p, (ConditionalOrExpression)formula);
		}
		if (formula instanceof PathExpression) {
			return path(p, (PathExpression) formula);
		}
		if (formula instanceof BareExpression) {
			return bare(p, (BareExpression)formula);
		}
		
		if (formula instanceof IriValue) {
			return iriValue(p, (IriValue)formula);
		}
		
		if (formula instanceof LiteralFormula) {
			return literal((LiteralFormula)formula);
		}
		if (formula instanceof IriTemplateExpression) {
			return iriTemplate(p, (IriTemplateExpression) formula);
		}
		fail("At {0}, failed to process expression: {1}", p.getPath(), FormulaUtil.simpleString(formula));
		
		return null;
	}


	private ShowlExpression iriTemplate(ShowlPropertyShape p, IriTemplateExpression formula) {
		return ShowlFunctionExpression.fromIriTemplate(schemaService, nodeService, p, formula.getTemplate());
	}


	private ShowlExpression iriValue(ShowlPropertyShape p, IriValue formula) {

		URI iri = formula.getIri();
		
		// Get the parent NodeShape within which to search for the property.
		// We really ought to rethink this whole method.  It's not clear that the
		// following approach to finding the parent node is correct in all cases.
		
		ShowlNodeShape parent = p.getValueShape()==null ? p.getDeclaringShape() : p.getValueShape();
		
		ShowlPropertyShape out = parent.findOut(iri);
		if (out != null) {
			return propertyExpression(out);
		}
		
		Vertex v = schemaService.getOwlReasoner().getGraph().getVertex(iri);
		if (v != null) {
			// For now, we assume that if the individual is in the graph, then it is a member of an enumeration.
			// We should probably confirm that the OWL Class of the individual is a subclass of schema:Enumeration
			
			return new ShowlEnumIndivdiualReference(iri);
		}
		
		ShowlIriReferenceExpression result = new ShowlIriReferenceExpression(iri, p);
		
		return result;
	}

	private ShowlPropertyExpression asPropertyExpression(ShowlPropertyShape p) {
		if (p instanceof ShowlDirectPropertyShape) {
			return new ShowlDirectPropertyExpression((ShowlDirectPropertyShape) p);
		}
		
		return new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape) p);
	}

	private ShowlExpression propertyExpression(ShowlPropertyShape out) {
		
		out = out.maybeDirect();
		if (out instanceof ShowlDirectPropertyShape) {
			return new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)out);
		}
		
		return new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape) out);
	}


	private ShowlExpression literal(LiteralFormula formula) {
		
		return new ShowlLiteralExpression(formula.getLiteral());
	}


	private ShowlExpression conditionalOr(ShowlPropertyShape p, ConditionalOrExpression formula) {

		PrimaryExpression primary = formula.asPrimaryExpression();
		if (primary == null) {
			fail("At {0}, failed to process conditional or expression {1}", p.getPath(), FormulaUtil.simpleString(formula));
		} 
		
		return expression(p, primary);
	}


	private ShowlExpression bare(ShowlPropertyShape p, BareExpression formula) {

		PrimaryExpression primary = formula.asPrimaryExpression();
		if (primary == null) {
			fail("At {0}, failed to process bare expression {1}", p.getPath(), FormulaUtil.simpleString(formula));
		} 
		
		return expression(p, primary);
	}

	private ShowlExpression path(ShowlPropertyShape p, PathExpression formula) {
		return path(p, null, formula);
	}

	private ShowlExpression path(ShowlPropertyShape p, ShowlPropertyShape prior, PathExpression formula) {
		
		List<PathStep> stepList = formula.getStepList();
		String shapeIdValue = p.getDeclaringShape().getId().stringValue();
		
		
		for (int i=0; i<stepList.size(); i++) {
			PathStep step = stepList.get(i);
			if (step instanceof DirectionStep) {
				DirectionStep dirStep = (DirectionStep) step;
				URI predicate = dirStep.getTerm().getIri();
				
				ShowlProperty property = schemaService.produceProperty(predicate);
				shapeIdValue += dirStep.getDirection().getSymbol() + predicate.getLocalName();
				
				ShowlNodeShape parentNode = parentNode(shapeIdValue, dirStep, property, p, prior);
				switch (dirStep.getDirection()) {
				case OUT :
					prior = outwardProperty(parentNode, property, prior, p);
					break;
					
				case IN:
					prior = inwardProperty(parentNode, property);
					break;
				}
			} else {
				HasPathStep hasStep = (HasPathStep) step;
				
				if (prior == null) {
					fail("At {0}, top-level filter not supported: {1}", p.getPath(), FormulaUtil.simpleString(formula));
				}
				ShowlNodeShape valueShape = prior.getValueShape();
				if (valueShape == null) {
					ShowlProperty property = schemaService.produceProperty(prior.getPredicate());
					ShowlClass owlClass = schemaService.inferRange(property);
					valueShape = createNodeShape(prior, shapeIdValue, owlClass, p);
					prior.setValueShape(valueShape);
				}
				buildHasStep(prior, hasStep);
			}
		}
		
		return asPropertyExpression(prior);
	}

	private void buildHasStep(ShowlPropertyShape prior, HasPathStep step) throws ShowlProcessingException {
		

		for (PredicateObjectList pol : step.getConstraints()) {
			PathExpression path = pol.getPath();
			
			ShowlExpression showlPath = path(prior, prior, path);
			
			ShowlPropertyShape field = ShowlUtil.asPropertyShape(showlPath);
			
			if (field == null) {
				fail("Cannot convert expression to PropertyShape {}", showlPath.displayValue());
			}
			
			
			for (Expression e : pol.getObjectList().getExpressions()) {
				
				ShowlExpression expression = expression(prior, e);
				field.addHasValue(new ShowlFilterExpression(expression));
				
				if (logger.isTraceEnabled()) {
					logger.trace("buildHasStep: {} = {}", field.getPath(), expression.displayValue());
				}
				
			}
		}
		
	}


	private ShowlDerivedPropertyShape inwardProperty(ShowlNodeShape parentNode, ShowlProperty property) {
		ShowlInwardPropertyShape prior = parentNode.getInwardProperty(property.getPredicate());
		if (prior != null) {
			return prior;
		}
		ShowlInwardPropertyShape p = new ShowlInwardPropertyShape(parentNode, property);
		parentNode.addInwardProperty(p);
		return p;
	}


	private ShowlDerivedPropertyShape outwardProperty(ShowlNodeShape parentNode, ShowlProperty property,
			ShowlPropertyShape prior, ShowlPropertyShape declaringProperty) {
		
		
		ShowlDerivedPropertyShape existing = parentNode.getDerivedProperty(property.getPredicate()).unfiltered();
				parentNode.findProperty(property.getPredicate());
		if (existing != null) {
			return existing;
		}

		PropertyConstraint c = null;
		if (prior==null) {
			c = new PropertyConstraint(property.getPredicate());
			c.setNodeKind(declaringProperty.getNodeKind());
		}
		ShowlOutwardPropertyShape p = new ShowlOutwardPropertyShape(parentNode, property, c);
		property.addPropertyShape(p);
		parentNode.addDerivedProperty(p);
		
		return p;
	}


	private ShowlNodeShape createNodeShape(ShowlPropertyShape accessor, String shapeIdValue,
			ShowlClass owlClass, ShowlPropertyShape idRef ) {
		ShowlNodeShape value = accessor.getValueShape();
		if (value != null) {
			return value;
		}
		URI shapeId = new URIImpl(shapeIdValue);
		Shape shape = new Shape(shapeId);
		
		NodeKind kind = accessor.getNodeKind();
		if (kind == null) {
			ShowlNodeShape nestedShape = accessor.getValueShape();
			if (nestedShape != null) {
				kind = nestedShape.getNodeKind();
			}
		}
		shape.setNodeKind(kind);

		ShowlNodeShape node = nodeService.createShowlNodeShape(accessor, shape, owlClass);

		if (kind == NodeKind.IRI) {
			ShowlProperty konigId = schemaService.produceProperty(Konig.id);
			ShowlIdRefPropertyShape p = new ShowlIdRefPropertyShape(node, konigId, idRef);
			node.addDerivedProperty(p);
		}
		return node;
	}
	


	private ShowlNodeShape parentNode(
		String shapeIdValue,
		DirectionStep dirStep, 
		ShowlProperty property, 
		ShowlPropertyShape p, 
		ShowlPropertyShape prior
	) {
		if (dirStep.getTerm() instanceof VariableTerm) {
			return varRoot(dirStep.getTerm().getIri(), p);
		}
		if (prior == null) {
			return p.getDeclaringShape();
		}
		
		ShowlClass owlClass = schemaService.inferDomain(property);
		ShowlProperty priorProperty = prior.getProperty();
		ShowlClass prevClass = dirStep.getDirection() == Direction.OUT ?
			schemaService.inferRange(priorProperty) :
			schemaService.inferDomain(priorProperty);
		
		owlClass = schemaService.mostSpecificClass(owlClass, prevClass);
		
		// Hmmmm.  Are we sure that p=idref always?
		
		return createNodeShape(prior, shapeIdValue, owlClass, p);
	}
	


	private ShowlNodeShape varRoot(URI predicate, ShowlPropertyShape propertyShape) {
		
		for (ShowlPropertyShape p=propertyShape; p!=null;) {
			ShowlNodeShape node = p.getDeclaringShape();
			Shape shape = node.getShape();
			if (shape == null) {
				fail("Declaring Shape is null at {0}", p.getPath());
			}
			
			if (shape.getVariableById(predicate) != null) {
				return node;
			}
			
			p = node.getAccessor();
		}
		fail("Root node for ?{0} not found in formula of {1}", predicate.getLocalName(), propertyShape.getPath());
		return null;
	}


	private void fail(String pattern, Object...arg) {
		String msg = MessageFormat.format(pattern, arg);
		throw new ShowlProcessingException(msg);
	}

	private ShowlExpression quantifiedExpression(ShowlPropertyShape p, QuantifiedExpression formula) {
		
		PrimaryExpression primary = formula.asPrimaryExpression();
		if (primary == null) {
			fail("At {0}, failed to process quantified expression {1}", p.getPath(), FormulaUtil.simpleString(formula));
		} 
		
		return expression(p, primary);
	}

	public ShowlFunctionExpression functionExpression(ShowlPropertyShape p, FunctionExpression formula) {
		ShowlFunctionExpression func = new ShowlFunctionExpression(p, formula);
		for (Expression arg : formula.getArgList()) {
			func.getArguments().add(expression(p, arg));
		}
		return func;
	}



}
