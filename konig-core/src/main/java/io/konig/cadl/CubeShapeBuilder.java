package io.konig.cadl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashSet;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.showl.ShowlClass;
import io.konig.core.showl.ShowlManager;
import io.konig.core.showl.ShowlProperty;
import io.konig.core.showl.ShowlPropertyShape;
import io.konig.core.showl.ShowlTraverser;
import io.konig.datasource.DataSource;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.FunctionExpression;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.formula.VariableTerm;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class CubeShapeBuilder {
	
	private static Logger logger = LoggerFactory.getLogger(CubeShapeBuilder.class);

	private ShapeManager shapeManager;
	private OwlReasoner reasoner;
	private String shapeNamespace;
	
	public CubeShapeBuilder(OwlReasoner reasoner, ShapeManager shapeManager, String shapeNamespace) {
		this.reasoner = reasoner;
		this.shapeManager = shapeManager;
		this.shapeNamespace = shapeNamespace;
	}

	public Shape buildShape(Cube cube) throws CubeShapeException {
		Worker worker = new Worker();
		return worker.buildShape(cube);
	}
	
	private class Worker {
		
		private ShowlTraverser traverser;
		private PropertyConstraint sourceVariable;
		private String sourceVarName;
		
		public Shape buildShape(Cube cube) throws CubeShapeException {
			if (logger.isDebugEnabled()) {
				logger.debug("buildShape({})", cube.getId());
			}
			
			ShowlManager showlManager = new ShowlManager(shapeManager, reasoner);
			traverser = new ShowlTraverser(showlManager);
			
			
			URI shapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape");
			Shape shape = produceShape(shapeId);
			shape.setNodeShapeCube(cube);
			
			addVariable(shape, cube, cube.getSource());
			
			List<PathStep> location = new ArrayList<>();
			
			for (Dimension dim : cube.getDimension()) {
				location.clear();
				rewriteFormula(location, dim.getFormula());
				
				for (Level level : dim.getLevel()) {
					addLevel(location, shape, cube, dim, level);
				}
			}
			
			addDataSources(shape, cube);
			
			return shape;
		}
		
		private void addDataSources(Shape shape, Cube cube) {
			
			for (DataSource ds : cube.getStorage()) {
				shape.addShapeDataSource(ds);
			}
			
		}

		private void addVariable(Shape shape, Cube cube, Variable source) {
			
			PropertyConstraint p = shape.getVariableById(source.getId());
			if (p == null) {
				URI predicate = CubeUtil.predicate(cube, source.getId());
				p = new PropertyConstraint(predicate);
				shape.addVariable(p);

				if (logger.isTraceEnabled()) {
					logger.trace("addVariable: {}", p.getPredicate().stringValue());
				}
			}
			
			p.setValueClass(source.getValueType());
			
			sourceVariable = p;
			sourceVarName = "?" + p.getPredicate().getLocalName();
			
			shape.setTargetClass(targetClass(cube));
			

			if (logger.isTraceEnabled()) {
				logger.trace("addVariable: On {}, targetClass = {}", RdfUtil.localName(shape.getId()), RdfUtil.localName(shape.getTargetClass()));
			}
			
		}
		
		private URI targetClass(Cube cube) {
			return RdfUtil.uri(sourceVariable.getValueClass());
		}


		private void addLevel(List<PathStep> location, Shape shape, Cube cube, Dimension dim, Level level) throws CubeShapeException {
			if (logger.isTraceEnabled()) {
				logger.trace("addLevel(shape: {}, level: {})", RdfUtil.localName(shape.getId()), RdfUtil.localName(level.getId()));
			}
			URI predicate = CubeUtil.predicate(cube, level.getId());
			PropertyConstraint p = shape.getPropertyConstraint(predicate);
			if (p == null) {
				p = new PropertyConstraint(predicate);
				shape.add(p);
			}
			p.setMinCount(0);
			p.setMaxCount(1);
			
			
			
			setLevelFormula(location, dim, level, p);
			
			if (!level.getAttribute().isEmpty()) {
				setLevelValueClass(location, dim, level, p);
				
				URI levelShapeId = uri(shapeNamespace + cube.getId().getLocalName() + "RawShape/level/" + level.getId().getLocalName() + "Shape");
				Shape levelShape = produceShape(levelShapeId);
				p.setShape(levelShape);
				addAttributes(levelShape, cube, level, p, location);
				
			} else {
				setDatatype(p, p.getFormula(), level.getId());
			}
			
		}


		private void setLevelValueClass(List<PathStep> location, Dimension dim, Level level, PropertyConstraint p) throws CubeShapeException {
			
			QuantifiedExpression formula = p.getFormula();
			Set<URI> valueClassCandidates = traverser.valueClass(RdfUtil.uri(sourceVariable.getValueClass()), formula);
			
			if (valueClassCandidates.size()==1) {
				p.setValueClass(valueClassCandidates.iterator().next());
				if (logger.isTraceEnabled()) {
					logger.trace("setLevelValueClass: level={}, valueClass={}", level.getId().getLocalName(), RdfUtil.localName(p.getValueClass()));
				}
			} else {
				throw new CubeShapeException("Failed to determine valueClass for level " + level.getId().stringValue());
			}
			
			
		}

		private void addAttributes(Shape shape, Cube cube, Level level,  PropertyConstraint levelConstraint, List<PathStep> location) throws CubeShapeException {
			
			if (logger.isTraceEnabled()) {
				logger.debug("addAttributes(shape: {}, level: {})", RdfUtil.localName(shape.getId()), level.getId().getLocalName());
			}
			
			URI datatype=null;
			for (Attribute attr : level.getAttribute()) {
				
				if (attr.getId().getLocalName().equals("id")) {
					shape.setNodeKind(NodeKind.IRI);
					continue;
				}
				
				URI predicate = null;
				
				if (attr.getFormula()==null) {
					// The attribute does not declare a formula.
					// This implies that there must be  a sh:Nodeshape with the same target class that has 
					// a property with the same local name as the attribute.
					//
					// We now lookup that property and create a formulat that references it.
					URI levelClass = RdfUtil.uri(levelConstraint.getValueClass());
					if (levelClass == null) {
						throw new CubeShapeException("Target class not defined for level: " + level.getId().stringValue());
					}
					
					String localName = attr.getId().getLocalName();
					ShowlPropertyShape property = traverser.findPropertyByLocalName(levelClass, localName);
					if (property == null) {
						throw new CubeShapeException("Property not found for attribute: " + attr.getId().stringValue());
					}
					
					predicate = property.getPredicate();
					datatype = datatype(property);
					
					if (logger.isTraceEnabled()) {
						logger.trace("addAttributes: At level {}, added direct attribute {}", level.getId().getLocalName(), predicate.stringValue());
					}
					
				} else {
					predicate = CubeUtil.predicate(cube, attr.getId());
				}
				PropertyConstraint p = shape.getPropertyConstraint(predicate);
				if (p == null) {
					p = new PropertyConstraint(predicate);
					shape.add(p);
				}
				
				QuantifiedExpression formula = rewriteFormula(location, attr.getFormula());
				
				
				if (logger.isTraceEnabled() && formula!=null) {
					logger.trace("addAttributes: level={}, attribute={}, formula={}", 
							level.getId().getLocalName(), 
							attr.getId().getLocalName(),
							formula.toSimpleString());
				}
				p.setFormula(formula);
				p.setMinCount(0);
				p.setMaxCount(1);
				if (datatype != null) {
					p.setDatatype(datatype);
				}
				if (p.getDatatype() == null) {
					setDatatype(p, formula, attr.getId());
				}
				
			}
			
		}
		
		private URI datatype(ShowlPropertyShape property) {
			PropertyConstraint constraint = property.getPropertyConstraint();
			if (constraint != null && constraint.getDatatype()!=null) {
				return constraint.getDatatype();
			}
			ShowlProperty p = property.getProperty();
			if (p!=null) {
				if (p.getRange() != null) {
					return p.getRange().getId();
				}
				ShowlClass owlClass = p.inferRange(traverser.getManager().getShowlFactory());
				if (owlClass != null) {
					return owlClass.getId();
				}
			}
			return null;
		}

		/**
		 * Rewrite the formula to eliminate references to the variable.
		 * <p>
		 * For example, suppose we have the following structure:
		 * </p>
		 * <pre>
		 *   
		 *   VARIABLE ?opportunity
		 *   DIMENSION AccountDim       <==> ?opportunity.customerAccount
		 *     LEVEL industry           <==> ?opportunity.customerAccount.industry
		 *       ATTRIBUTE industryCode <==> ?opportunity.customerAccount.industry.identifier
		 * </pre>
		 * At level 'industry' we rewrite the formula as '$.customerAccount.industry'
		 * At attribute 'industryCode' we rewrite the formula as '$.identifier' since it has the same base as the parent level.
		 * <p>
		 * If the given formula does not start with a variable, we return the formula as-is.
		 * If the given location is empty, we remove the leading variable step, and populate the location with the current path.
		 * If the given location is non-empty and the given formula starts with a variable, we remove the leading path.
		 *  
		 * </p>
		 * @param location
		 * @param formula
		 * @return
		 */
		private QuantifiedExpression rewriteFormula(List<PathStep> location, QuantifiedExpression formula) {
			
			if (formula != null) {
				
				String oldFormulaText = null;
				if (logger.isTraceEnabled()) {
					oldFormulaText = formula.toSimpleString();
				}
				formula = formula.clone();
				
				// Collect the list of PathExpression instances within the formula
				final List<PathExpression> pathList = new ArrayList<>();
				formula.dispatch(new FormulaVisitor() {

					@Override
					public void enter(Formula formula) {
						if (formula instanceof PathExpression) {
							pathList.add((PathExpression) formula);
						}
						
					}

					@Override
					public void exit(Formula formula) {
						
					}
					
				});
				
				
				// For each PathExpression that we discovered, apply the rewrite rules
				
				for (PathExpression path : pathList) {
					if (startsWithVariable(path)) {
						List<PathStep> stepList = path.getStepList();
						
						if (location.isEmpty()) {
							stepList.remove(0);
							location.addAll(stepList);
							
						} else {
							// TODO: Verify that each element of 'stepList' is equivalent to the 
							//       corresponding element of 'location'
							//       For now, we just assume the equivalence.
							
							for (int i=0; i<location.size(); i++) {
								stepList.remove(0);
							}
						}
					}
					
				}
				if (logger.isTraceEnabled()) {
					logger.trace("rewriteFormula: {} ===> {}", oldFormulaText, formula.toSimpleString());
				}
			}
			
			return formula;
		}

		private boolean startsWithVariable(PathExpression path) {
			List<PathStep> stepList = path.getStepList();
			if (!stepList.isEmpty()) {
				PathStep step = stepList.get(0);
				if (step instanceof DirectionStep) {
					DirectionStep dirStep = (DirectionStep) step;
					PathTerm term = dirStep.getTerm();
					return term instanceof VariableTerm;
				}
			}
			return false;
		}

		

		private void setDatatype(PropertyConstraint p, QuantifiedExpression formula, URI elementId) throws CubeShapeException {
			
			if (formula == null) {
				throw new CubeShapeException(
						MessageFormat.format("Formula must be defined for <{0}>", elementId.stringValue())
				);
			}
			
			PrimaryExpression primary = formula.asPrimaryExpression();
			if (primary instanceof FunctionExpression) {
				FunctionExpression func = (FunctionExpression) primary;
				URI datatype = func.getModel().getReturnType().getRdfType();
				p.setDatatype(datatype);
				if (logger.isTraceEnabled()) {
					logger.trace("setDatatype: type={} at {}", datatype.getLocalName(), elementId.stringValue());
				}
				return;
				
			}
			
			Set<ShowlProperty> propertySet =
					traverser.traverse(sourceVariable.getPredicate(), RdfUtil.uri(sourceVariable.getValueClass()), formula);
			
			if (propertySet.size()==1) {
				setDatatype(propertySet.iterator().next(), p, elementId, formula);
				
			} else if (propertySet.isEmpty()) {
				throw new CubeShapeException(
					MessageFormat.format("Datatype not found for <{0}> mapped by {1}", elementId.stringValue(), formula.toSimpleString())
				);
			} else {
				// propertySet contains multiple properties
				
				Set<URI> datatypeSet = new HashSet<>();
				for (ShowlProperty property : propertySet) {
					datatypeSet.addAll(property.rangeIncludes(reasoner));
				}
				
				if (datatypeSet.size() == 1) {
					URI datatype = datatypeSet.iterator().next();
					if (reasoner.isDatatype(datatype)) {
						p.setDatatype(datatype);
						if (logger.isTraceEnabled()) {
							logger.trace("setDatatype: type={} at {}", datatype.getLocalName(), elementId.stringValue());
						}
					} else {

						throw new CubeShapeException(
								MessageFormat.format(
									"<{0}> must have a Datatype value, but the formula {1} implies <{2}>", 
									elementId.stringValue(), 
									formula.toSimpleString(), 
									datatype.stringValue())
						);
					}
				} else if (datatypeSet.isEmpty()) {

					throw new CubeShapeException(
							MessageFormat.format(
									"Datatype for <{0}> is not known.  The datatype must be specified explicitly.", 
									elementId.stringValue()));
				} else {

					StringBuilder msg = new StringBuilder();
					msg.append("Datatype is ambiguous for <");
					msg.append(elementId.stringValue());
					msg.append(">.  The datatype must be specified explicitly.");
					throw new CubeShapeException(msg.toString());
				}
			}
			
			
		}

		

		private void setDatatype(ShowlProperty property, PropertyConstraint p, URI element, QuantifiedExpression formula) throws CubeShapeException {
			
			ShowlClass range = property.getRange();
			URI datatype = null;
			if (range == null) {
				Set<URI> set = property.rangeIncludes(reasoner);
				if (set.size()==1) {
					datatype = set.iterator().next();
				} else if (set.size()>1) {
					StringBuilder msg = new StringBuilder();
					msg.append("Datatype is ambiguous for <");
					msg.append(element.stringValue());
					msg.append(">.  The datatype must be specified explicitly.");
					throw new CubeShapeException(msg.toString());
				}
			} else {
				datatype = range.getId();
			}
			
			if (datatype == null) {
				throw new CubeShapeException(
						MessageFormat.format(
								"Datatype for <{0}> is not known.  The datatype must be specified explicitly.", 
								element.stringValue()));
			}
			
			if (!reasoner.isDatatype(datatype)) {
				throw new CubeShapeException(
						MessageFormat.format(
							"<{0}> must have a Datatype value, but the formula {1} implies <{2}>", 
							element.stringValue(), 
							formula.toSimpleString(), 
							datatype.stringValue())
				);
			}
			
			p.setDatatype(datatype);
			
			
		}

		private Shape produceShape(URI shapeId) {
			Shape shape = shapeManager.getShapeById(shapeId);
			if (shape == null) {
				shape = new Shape(shapeId);
				shapeManager.addShape(shape);
			}
			return shape;
		}

		private void setLevelFormula(List<PathStep> location, Dimension dim, Level level, PropertyConstraint p) {
			
			QuantifiedExpression formula = rewriteFormula(location, level.getFormula());
			p.setFormula(formula);
			if (formula!=null && logger.isTraceEnabled()) {
				logger.trace("At Dimension {}, Level {}... formula = {}", 
						dim.getId().getLocalName(), level.getId().getLocalName(), formula.toSimpleString());
			}
			
		}


		private URI uri(String value) {
			return new URIImpl(value);
		}
	}
	
	
	

	

}
