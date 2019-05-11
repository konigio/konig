package io.konig.core.showl;

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


import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.expression.ShowlExpressionBuilder;
import io.konig.core.vocab.Konig;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShowlNodeShapeBuilder {
	
	private static final Logger logger = LoggerFactory.getLogger(ShowlNodeShapeBuilder.class);


	private ShowlSchemaService schemaService;
	private ShowlNodeShapeService nodeService;
	private boolean recursive=true;
	
	public ShowlNodeShapeBuilder(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService) {
		this.schemaService = schemaService;
		this.nodeService = nodeService;
	}

	public boolean isRecursive() {
		return recursive;
	}

	public void setRecursive(boolean recursive) {
		this.recursive = recursive;
	}

	public ShowlNodeShape buildNodeShape(ShowlPropertyShape accessor, Shape shape) throws ShowlProcessingException {
		if (logger.isTraceEnabled()) {
			if (accessor == null) {
				logger.trace("buildNodeShape({})", RdfUtil.localName(shape.getId()));
			} else {
				logger.trace("buildNodeShape(accessor={}, shape={}",  accessor.getPath(), RdfUtil.localName(shape.getId()));
			}
		}
		ShowlClass owlClass = targetOwlClass(accessor, shape);
		ShowlNodeShape node = new ShowlNodeShape(accessor, shape, owlClass);
		addProperties(node);
		return node;
	}


	private void addProperties(ShowlNodeShape node) {
		if (logger.isTraceEnabled()) {
			logger.trace("addProperties({})", node.getPath());
		}
		
		addIdProperty(node);
		
		for (PropertyConstraint c : node.getShape().getProperty()) {
			addDirectProperty(node, c.getPredicate(), c);
		}
		
		for (PropertyConstraint c : node.getShape().getDerivedProperty()) {
			addDerivedProperty(node, c);
		}
		
		
		
	}


	private void addIdProperty(ShowlNodeShape declaringShape) {

		if (declaringShape.getShape().getIriTemplate() != null) {

			ShowlProperty property = schemaService.produceProperty(Konig.id);
			ShowlOutwardPropertyShape out = new ShowlOutwardPropertyShape(declaringShape, property);
			out.setFormula(ShowlFunctionExpression.fromIriTemplate(
					schemaService, nodeService, out, declaringShape.getShape().getIriTemplate()));
			
			declaringShape.addDerivedProperty(out);
		} else if (declaringShape.getShape().getNodeKind() == NodeKind.IRI) {
			addDirectProperty(declaringShape, Konig.id, null);
		}
		
	}


	private void addDerivedProperty(ShowlNodeShape node, PropertyConstraint c) {
		URI predicate = c.getPredicate();
		if (predicate != null) {
			ShowlProperty property = schemaService.produceProperty(predicate);
			ShowlDerivedPropertyShape p = new ShowlOutwardPropertyShape(node, property, c);
			property.addPropertyShape(p);
			node.addDerivedProperty(p);

			
			if (logger.isTraceEnabled()) {
				logger.trace("addDerivedProperty: {}", p.getPath());
			}
			
			processFormula(p);
			
			if (recursive && c.getShape() != null) {
				buildNodeShape(p, c.getShape());
			}
			
		}
		
	}


	private void addDirectProperty(ShowlNodeShape node, URI predicate, PropertyConstraint c) {
		
		if (predicate != null) {
			ShowlProperty property = schemaService.produceProperty(predicate);
			ShowlDirectPropertyShape direct = new ShowlDirectPropertyShape(node, property, c);
			property.addPropertyShape(direct);
			node.addProperty(direct);
			
			if (logger.isTraceEnabled()) {
				logger.trace("addDirectProperty: {}", direct.getPath());
			}
			
			processFormula(direct);
						
			if (recursive && c!=null && c.getShape()!=null) {
				buildNodeShape(direct, c.getShape());
			}
		}
		
	}


	private void processFormula(ShowlPropertyShape p) {
		PropertyConstraint c = p.getPropertyConstraint();
		if (c != null && c.getFormula() != null) {
			QuantifiedExpression formula = c.getFormula();
			ShowlExpressionBuilder builder = new ShowlExpressionBuilder(schemaService, nodeService);
			ShowlExpression ex = builder.expression(p, formula);
			p.setFormula(ex);
			
			if (ex instanceof ShowlPropertyExpression) {
				ShowlPropertyShape synonym = ((ShowlPropertyExpression) ex).getSourceProperty();
				synonym.addExpression(propertyExpression(p));
			}
		}
		
	}


	private ShowlExpression propertyExpression(ShowlPropertyShape p) {
		if (p instanceof ShowlDirectPropertyShape) {
			return new ShowlDirectPropertyExpression((ShowlDirectPropertyShape)p);
		}
		return new ShowlDerivedPropertyExpression((ShowlDerivedPropertyShape)p);
	}

	private ShowlClass targetOwlClass(ShowlPropertyShape accessor, Shape shape) {
		if (accessor != null) {
			PropertyConstraint p = accessor.getPropertyConstraint();
			if (p != null) {
				if (p.getValueClass() instanceof URI) {
					return schemaService.produceShowlClass(RdfUtil.uri(p.getValueClass()));
				}
				if (p.getShape() != null && p.getShape().getTargetClass() != null) {
					return schemaService.produceShowlClass(p.getShape().getTargetClass());
				}
			}
		}
		URI targetClass = shape.getTargetClass();
		if (targetClass == null) {
			targetClass = Konig.Undefined;
		}
		return schemaService.produceShowlClass(targetClass);
	}
	

}
