package io.konig.transform.proto;

import java.text.MessageFormat;
import java.util.HashMap;

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
import java.util.Map;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.DirectionStep;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.InStep;
import io.konig.core.path.Step;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.SH;
import io.konig.formula.Direction;
import io.konig.formula.HasAggregationVisitor;
import io.konig.formula.IriValue;
import io.konig.formula.DirectedStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PathTerm;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.DataChannel;

public class ShapeModelFactory1 {

	private static Logger logger = LoggerFactory.getLogger(ShapeModelFactory1.class);
	private ShapeManager shapeManager;
	private DataChannelFactory dataChannelFactory;
	private PropertyMapper propertyMapper;
	private VariableShapeFactory variableShapeFactory;
	private boolean failIfPropertyNotMapped=true;
	private HasAggregationVisitor aggregationVisitor;
	private OwlReasoner reasoner;
	
	public ShapeModelFactory1(ShapeManager shapeManager, DataChannelFactory dataChannelFactory, OwlReasoner reasoner) {
		if (dataChannelFactory == null) {
			dataChannelFactory = new DefaultDataChannelFactory();
		}
		this.reasoner = reasoner;
		this.shapeManager = shapeManager;
		this.dataChannelFactory = dataChannelFactory;
		propertyMapper = new SimplePropertyMapper(reasoner, this);
	}
	

	

	public ShapeManager getShapeManager() {
		return shapeManager;
	}



	public DataChannelFactory getDataChannelFactory() {
		return dataChannelFactory;
	}

	public boolean isFailIfPropertyNotMapped() {
		return failIfPropertyNotMapped;
	}




	public void setFailIfPropertyNotMapped(boolean failIfPropertyNotMapped) {
		this.failIfPropertyNotMapped = failIfPropertyNotMapped;
	}




	public ShapeModel createShapeModel(Shape targetShape) throws ShapeTransformException {
		return createShapeModel(targetShape, true);
	}


	public ShapeModel createShapeModel(Shape targetShape, boolean mapProperties) throws ShapeTransformException {
		ShapeModel result = doCreateShapeModel(targetShape, null);
		if (mapProperties) {
			propertyMapper.mapProperties(result);
		}
		
		
		return result;
	}

	private ShapeModel doCreateShapeModel(Shape targetShape, PropertyModel accessor) throws ShapeTransformException {
		
		URI targetClass = targetShape.getTargetClass();
	
		ShapeModel shapeModel = new ShapeModel(targetShape);
		shapeModel.setAccessor(accessor);
		ClassModel classModel = new ClassModel(targetClass);
		classModel.setTargetShapeModel(shapeModel);
		
		shapeModel.setClassModel(classModel);
		
		addProperties(shapeModel);
		addGroupBy(shapeModel);
		
		return shapeModel;
	}

	private void addGroupBy(ShapeModel shapeModel) throws ShapeTransformException {
		
		Shape targetShape = shapeModel.getShape();
		for (PropertyConstraint p : targetShape.getProperty()) {
			
			if (requiredProperty(p)) {
				
				if (Konig.dimension.equals(p.getStereotype())) {
					
					PropertyModel item = shapeModel.getPropertyByPredicate(p.getPredicate());
					ShapeModel valueModel = item.getValueModel();
					if (valueModel != null) {
						PropertyModel id = valueModel.getPropertyByPredicate(Konig.id);
						if (id != null) {
							shapeModel.addGroupBy((GroupByItem)id);
						}
					}
				}
				
				QuantifiedExpression formula = p.getFormula();
				
				GroupByItem item =  groupByItem(shapeModel, formula);
				if (item != null) {
					shapeModel.addGroupBy(item);
				}
				
			}
		}
		
		
	}




	private GroupByItem groupByItem(ShapeModel shapeModel, QuantifiedExpression formula) throws ShapeTransformException {
		
		
		if (!hasAggregation(formula)) {
			return null;
		}
		
		PrimaryExpression primary = formula.asPrimaryExpression();
		if (primary instanceof PathExpression) {
			PathExpression path = (PathExpression) primary;
			
			PropertyModel result = null;
			
			boolean firstStep=true;
			for (PathStep step : path.getStepList()) {
				if (step instanceof DirectedStep) {
					DirectedStep dirStep = (DirectedStep) step;
					if (dirStep.getDirection() == Direction.OUT) {
						PathTerm term = dirStep.getTerm();
						if (term instanceof IriValue) {
							IriValue iriValue = (IriValue) term;
							URI predicate = iriValue.getIri();
							
							result = shapeModel.getPropertyByPredicate(predicate);
							if (result != null) {
								
								PropertyGroup group = result.getGroup();
								result = group.getTargetProperty();
								
								if (firstStep) {
									if (result instanceof BasicPropertyModel) {
										BasicPropertyModel basic = (BasicPropertyModel) result;
										PropertyConstraint pc = basic.getPropertyConstraint();
										if (pc.getMaxCount()!=null) {
											return null;
										}
									}
									firstStep = false;
								}
								shapeModel = result.getValueModel();
								
							} else {
								
								String msg = MessageFormat.format("In shape<{0}>, property <{1}> not found in path: {2}", 
										RdfUtil.localName(shapeModel.getShape().getId()),
										term.toString(), path.toString());
								throw new ShapeTransformException(msg);
							}
						} else {
							return null;
						}
					}
				} 
				
			}
			return result;
			
		}
		return null;
	}




	private boolean hasAggregation(QuantifiedExpression formula) {
		if (formula != null) {
			if (aggregationVisitor == null) {
				aggregationVisitor = new HasAggregationVisitor();
			}
			aggregationVisitor.reset();
			
			formula.dispatch(aggregationVisitor);
			
			return aggregationVisitor.visitedAggregation();
		}
		return false;
	}




	private boolean requiredProperty(PropertyConstraint p) {

		Integer maxCount = p.getMaxCount();
		if (maxCount != null && maxCount==1) {
			Integer minCount = p.getMinCount();
			return minCount!=null && minCount==1;
		}
		return false;
	}




	private void addProperties(ShapeModel targetShapeModel) throws ShapeTransformException {
				
		Shape targetShape = targetShapeModel.getShape();
		ClassModel classModel = targetShapeModel.getClassModel();
		

		addTargetId(targetShapeModel);
		
		
		for (PropertyConstraint p : targetShape.getProperty()) {
			if (p.getPath() instanceof PredicatePath) {
				PredicatePath path = (PredicatePath) p.getPath();
				URI predicate = path.getPredicate();
				PropertyGroup group = classModel.produceOutGroup(predicate);
				
				DirectPropertyModel propertyModel = new DirectPropertyModel(predicate, group, p);
				propertyModel.setDeclaringShape(targetShapeModel);
				group.setTargetProperty(propertyModel);
				group.add(propertyModel);
				
				targetShapeModel.add(propertyModel);
				
				if (logger.isDebugEnabled()) {
					logger.debug("addProperties: Adding {} from {} to group[{}] in ClassModel[{}] ",
						predicate.getLocalName(),
						RdfUtil.localName(targetShape.getId()),
						group.hashCode(),
						classModel.hashCode()
					);
				}
				
				Shape valueShape = p.getShape();
				if (valueShape != null) {
					ShapeModel valueShapeModel = doCreateShapeModel(valueShape, propertyModel);
					group.setValueClassModel(valueShapeModel.getClassModel());
					propertyModel.setValueModel(valueShapeModel);
				}
				handleEquivalentPath(propertyModel);
			}
		}

		addVariables(targetShapeModel);
		
	}

	
		
		
	private void addTargetId(ShapeModel targetShapeModel) {

		Shape targetShape = targetShapeModel.getShape();
		
		if (targetShape.getNodeKind()==NodeKind.IRI || targetShape.getIriTemplate()!=null || targetShape.getIriFormula()!=null) {
			ClassModel classModel = targetShapeModel.getClassModel();
			PropertyGroup group = classModel.produceOutGroup(Konig.id);
			IdPropertyModel propertyModel = new IdPropertyModel(group);
			propertyModel.setDeclaringShape(targetShapeModel);
			targetShapeModel.add(propertyModel);
			group.add(propertyModel);
			group.setTargetProperty(propertyModel);
			targetShapeModel.add(propertyModel);
		}


	}

	private void handleEquivalentPath(DirectPropertyModel propertyModel) throws ShapeTransformException {

		StepPropertyModel stepModel = null;
		if (propertyModel instanceof DirectPropertyModel) {
			DirectPropertyModel directPropertyModel = (DirectPropertyModel) propertyModel;
			PropertyConstraint p = directPropertyModel.getPropertyConstraint();
			Path path = p.getEquivalentPath();
			
			
			if (path != null) {
				
				logger.debug("handleEquivalentPath({})", propertyModel.simplePath());
				
				// TODO: abort if the property is a source property and the declaring shape is not the top shape.
				
				
				ShapeModel declaringShape = propertyModel.getDeclaringShape();

				
				
				ClassModel classModel = declaringShape.getClassModel();
				List<Step> stepList = path.asList();
				int last = stepList.size()-1;
				PropertyGroup group = null;
				StepPropertyModel priorStep = null;
				for (int index=0; index<=last; index++) {
					Step step = stepList.get(index);
					if (step instanceof DirectionStep) {
						if (group != null) {
							classModel = group.produceValueClassModel(null);
						}
						DirectionStep dirStep = (DirectionStep) step;
						Direction direction = dirStep.getDirection();
						URI predicate = dirStep.getPredicate();
						group = classModel.produceGroup(direction, predicate);
						logger.debug("handleEquivalentPath: adding {}{} to group[{}] in ClassModel[{}]", 
								direction.getSymbol(), 
								predicate.getLocalName(),
								group.hashCode(),
								classModel.hashCode());
					
						stepModel = new StepPropertyModel(predicate, direction, group, directPropertyModel, index);
						stepModel.setDeclaringShape(declaringShape);
						
						// TODO: eliminate list of step properties
						declaringShape.addStepProperty(stepModel);
						group.add(stepModel);
						
						if (priorStep != null) {
							priorStep.setNextStep(stepModel);
						}
						priorStep = stepModel;
						
					} else if (step instanceof HasStep) {
						HasStep hasStep = (HasStep) step;
						priorStep.setFilter(hasStep.getPairList());
						List<PredicateValuePair> pairList = hasStep.getPairList();
						classModel = group.getValueClassModel();
						if (classModel != null) {
							for (PredicateValuePair pair : pairList) {
								URI predicate = pair.getPredicate();
								Value value = pair.getValue();
								if (value instanceof Literal) {
									PropertyGroup childGroup = classModel.produceOutGroup(predicate);
									if ( childGroup.getTargetProperty()!=null ) {
										logger.debug("handleEquivalentPath: Adding fixed property {}={} in group #{}", 
												childGroup.getTargetProperty().simplePath(), value.stringValue(), childGroup.hashCode());
										
										FixedPropertyModel fixed = new FixedPropertyModel(predicate, childGroup, value);
										childGroup.add(fixed);
										
										if (logger.isDebugEnabled()) {

											logger.debug("handleEquivalentPath: adding {} to group[{}] in ClassModel[{}]", 
													predicate.getLocalName(),
													childGroup.hashCode(),
													classModel.hashCode());
										}
									}
								}
							}
						}
						
					} else {
						throw new ShapeTransformException("Step type not supported: " + step.getClass().getSimpleName());
					}
				}
			} 
		}
		
		propertyModel.setPathTail(stepModel);
	}




	public void addSourceShapes(ShapeModel targetShapeModel) throws ShapeTransformException {
		
		if (logger.isDebugEnabled()) {
			logger.debug("addSourceShapes(targetShapeModel: {})", RdfUtil.localName(targetShapeModel.getShape().getId()));
		}
		ClassModel classModel = targetShapeModel.getClassModel();

		URI targetClass = targetShapeModel.getShape().getTargetClass();
		if (targetClass != null) {
			List<Shape> shapeList = shapeManager.getShapesByTargetClass(targetClass);
			
			for (Shape shape : shapeList) {
				if (!topTarget(shape, classModel)) {
					DataChannel channel = dataChannelFactory.createDataChannel(shape);
					if (channel != null) {
						addSourceShape(classModel, classModel, shape, channel);
					}
				}
				
			}
		}
		
		addShapesForInverseProperties(targetShapeModel);
		
		
	}




	private void addShapesForInverseProperties(ShapeModel targetShapeModel) throws ShapeTransformException {
			
		for (PropertyModel p : targetShapeModel.getProperties()) {
			if (p instanceof DirectPropertyModel) {
				DirectPropertyModel direct = (DirectPropertyModel) p;
				StepPropertyModel pathHead = direct.getPathHead();
				if (pathHead != null && pathHead.getDirection()==Direction.IN) {
					PropertyGroup headGroup = pathHead.getGroup();
					ClassModel classModel = headGroup.getParentClassModel();
					

					StepPropertyModel next = pathHead.getNextStep();
					
					ClassModel sourceClassModel = null;
					if (next == null) {
					 // Do nothing.
					} else if (next.getDirection() == Direction.OUT) {
						sourceClassModel = next.getGroup().getParentClassModel();
					} else {
						throw new ShapeTransformException("Two consecutive inverse attributes not supported");
					}
					
					
					URI inverseProperty = pathHead.getPredicate();

					// Find property constraints that have the inverse property, and get the shapes
					// containing those property constraints.
					
					List<Vertex> sourceShapeVertices = reasoner.getGraph()
							.v(inverseProperty).in(SH.path).in(SH.property).toVertexList();
					
					for (Vertex v : sourceShapeVertices) {
						if (v.getId() instanceof URI) {
							URI sourceShapeId = (URI) v.getId();
							Shape sourceShape = shapeManager.getShapeById(sourceShapeId);
							if (sourceShape == null) {
								logger.warn("Shape not found: {}", sourceShapeId.getLocalName());
							} else if (isInverseCandidate(pathHead, sourceShape)) {
							
								DataChannel channel = dataChannelFactory.createDataChannel(sourceShape);
								if (channel != null) {
									
									if (logger.isDebugEnabled()) {
										logger.debug("addShapesForInverseProperties: For ^{} on {}, add source {}", 
												inverseProperty.getLocalName(), 
												RdfUtil.localName(targetShapeModel.getShape().getId()),
												RdfUtil.localName(sourceShape.getId()));
									}
									addSourceShape(classModel, sourceClassModel, sourceShape, channel);
								}
							}
						}
					}
					if (logger.isDebugEnabled()) {
						// Confirm that the next property is in the ClassModel
						
						if (next != null && next.getDirection()==Direction.OUT) {
							String shapeName = RdfUtil.localName(pathHead.getDeclaringShape().getShape().getId());
							PropertyGroup g = classModel.getOutGroupByPredicate(next.getPredicate());
							if (g == null) {
								logger.debug("addShapesForInverseProperty: GROUP NOT FOUND for {}! in Shape {}", 
										next.getPredicate().getLocalName(),
										shapeName);
							} else {
								logger.debug("addShapesForInverseProperties: group[{}] in ClassModel[{}] contains '{}'", 
										g.hashCode(),
										classModel.hashCode(),
										next.getPredicate().getLocalName(), 
										shapeName);
							}
						}
					}
					
				}
			}
		}
		
	}


	private boolean isInverseCandidate(StepPropertyModel pathHead, Shape sourceShape) {
		StepPropertyModel next = pathHead.getNextStep();
		if (next == null || next.getDirection()==Direction.IN) {
			return true;
		}
		PropertyConstraint p = sourceShape.getPropertyConstraint(next.getPredicate());
		
		return p != null;
	}




	private void addVariables(ShapeModel targetShapeModel) throws ShapeTransformException {
		Shape targetShape = targetShapeModel.getShape();
		List<PropertyConstraint> varList = targetShape.getVariable();
		if (varList != null) {
			VariableShapeFactory shapeFactory = getVariableShapeFactory();
			for (PropertyConstraint var : varList) {
				Resource varTypeId = var.getValueClass();
				if (varTypeId instanceof URI) {
					Shape varShape = shapeFactory.createShape(targetShape, var);
					ShapeModel varShapeModel = doCreateShapeModel(varShape, null);

					URI predicate = var.getPredicate();
					PropertyGroup group = targetShapeModel.getClassModel().produceOutGroup(predicate);
					VariablePropertyModel varModel = new VariablePropertyModel(predicate, group, var);
					varShapeModel.setAccessor(varModel);
					varModel.setDeclaringShape(targetShapeModel);
					group.add(varModel);
					if (logger.isDebugEnabled()) {
						logger.debug("addVariables: Adding variable {} from {} to group[{}] in ClassModel[{}]",
								predicate.getLocalName(),
								RdfUtil.localName(targetShape.getId()),
								group.hashCode(),
								group.getParentClassModel().hashCode());
					}
					varModel.setValueModel(varShapeModel);
					targetShapeModel.add(varModel);
					
				} else {
					throw new ShapeTransformException("addVariables: Variable type is not defined: " + var.getPredicate());
				}
			}
		}
		
	}




	private boolean topTarget(Shape shape, ClassModel classModel) {
		ShapeModel targetShapeModel = classModel.getTargetShapeModel();
		return (shape == targetShapeModel.getShape()) && targetShapeModel.getAccessor()==null;
	}

	private ShapeModel addSourceShape(ClassModel targetClassModel, ClassModel sourceClassModel, Shape sourceShape, DataChannel channel) throws ShapeTransformException {
		if (logger.isDebugEnabled()) {
			String sourceShapeName = RdfUtil.localName(sourceShape.getId());
			logger.debug("addSourceShape: Adding {} as candidate source for ClassModel[{}]", sourceShapeName, targetClassModel.hashCode());
		}
		ShapeModel sourceShapeModel = new ShapeModel(sourceShape);
		sourceShapeModel.setDataChannel(channel);
		targetClassModel.addCandidateSourceShapeModel(sourceShapeModel);
		sourceShapeModel.setClassModel(targetClassModel);
		
		addSourceId(sourceShapeModel);
		
		for (PropertyConstraint propertyConstraint : sourceShape.getProperty()) {
			
			if (propertyConstraint.getPath() instanceof PredicatePath) {
				PredicatePath path = (PredicatePath) propertyConstraint.getPath();
				URI predicate = path.getPredicate();
				PropertyGroup group = sourceClassModel.produceOutGroup(predicate);
				
				DirectPropertyModel propertyModel = new DirectPropertyModel(predicate, group, propertyConstraint);
				propertyModel.setDeclaringShape(sourceShapeModel);
				sourceShapeModel.add(propertyModel);
				propertyModel.setGroup(group);
				group.add(propertyModel);
				
				if (logger.isDebugEnabled()) {
					logger.debug("addSourceShape: Adding {} from {} to group[{}] in ClassModel[{}]",
							predicate.getLocalName(),
							RdfUtil.localName(sourceShape.getId()),
							group.hashCode(),
							sourceClassModel.hashCode()
							);
				}
				
				
				handleEquivalentPath(propertyModel);
				
				Shape valueShape = propertyConstraint.getShape();
				if (valueShape != null) {
					URI valueClass = propertyConstraint.getValueClass() instanceof URI ? (URI) propertyConstraint.getValueClass() : null;
					ClassModel valueClassModel = group.produceValueClassModel(valueClass);
					ShapeModel valueShapeModel = addSourceShape(valueClassModel, valueClassModel, valueShape, channel);
					propertyModel.setValueModel(valueShapeModel);
					valueShapeModel.setAccessor(propertyModel);
				}
			}
		}
		
		return sourceShapeModel;
		
	}
	
	private VariableShapeFactory getVariableShapeFactory() {
		if (variableShapeFactory == null) {
			variableShapeFactory = new VariableShapeFactory();
		}
		return variableShapeFactory;
	}

	private void addSourceId(ShapeModel sourceShapeModel) {
		

		Shape sourceShape = sourceShapeModel.getShape();
		
		if (sourceShape.getNodeKind()==NodeKind.IRI || sourceShape.getIriTemplate()!=null || sourceShape.getIriFormula()!=null) {
			ClassModel classModel = sourceShapeModel.getClassModel();
			PropertyGroup group = classModel.produceOutGroup(Konig.id);
			IdPropertyModel propertyModel = new IdPropertyModel(group);
			propertyModel.setDeclaringShape(sourceShapeModel);
			group.add(propertyModel);
			sourceShapeModel.add(propertyModel);
		}
		
		
	}
}
