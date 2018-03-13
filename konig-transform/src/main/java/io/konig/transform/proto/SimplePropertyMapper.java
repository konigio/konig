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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.impl.LiteralImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.formula.Direction;
import io.konig.formula.DirectionStep;
import io.konig.formula.Formula;
import io.konig.formula.FormulaVisitor;
import io.konig.formula.HasPathStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.FilteredDataChannel;
import io.konig.transform.rule.TransformBinaryOperator;

public class SimplePropertyMapper implements PropertyMapper {
	
	private static  Logger logger = LoggerFactory.getLogger(SimplePropertyMapper.class);

	private ShapeModelFactory shapeModelFactory;
	private OwlReasoner reasoner;

	public SimplePropertyMapper(OwlReasoner reasoner, ShapeModelFactory shapeModelFactory) {
		this.reasoner = reasoner;
		this.shapeModelFactory = shapeModelFactory;
	}

	@Override
	public void mapProperties(ShapeModel targetShape) throws ShapeTransformException {
		FromItemEnds fromItems = new FromItemEnds();
		ClassModel classModel = targetShape.getClassModel();
		Worker worker = new Worker(fromItems);
		worker.run(classModel);	
		classModel.setFromItem(fromItems.first);
	}
	
	static class FromItemEnds {

		ProtoFromItem first;
		ProtoFromItem last;
	}

	class Worker implements PropertyGroupHandler {
		private Set<PropertyGroup> unmatchedProperties = new HashSet<>();

		private FromItemEnds fromItemEnds;
		private FormulaHandler propertyHandler = new TimeIntervalFormulaHandler(shapeModelFactory.getDataChannelFactory());

		private HasStepVisitor hasStepVisitor;

		public Worker(FromItemEnds fromItemEnds) {
			this.fromItemEnds = fromItemEnds;
		}


		public void run(ClassModel classModel) throws ShapeTransformException {
			collectUnmatchedProperties(classModel);
			
			handleClass(classModel);
			
			if (shapeModelFactory.isFailIfPropertyNotMapped() && !unmatchedProperties.isEmpty()) {
				throw new ShapeTransformException(unmatchedMessage());
				
			}
		}
		
		

		private String unmatchedMessage() {
			StringBuilder msg = new StringBuilder();
			msg.append("Failed to match the following properties:");
			List<String> list = new ArrayList<>();
			for (PropertyGroup group : unmatchedProperties) {
				PropertyModel p = group.getTargetProperty();
				if (p != null) {
					list.add(p.simplePath());
				}
			}
			Collections.sort(list);
			for (String path : list) {
				msg.append("\n   ");
				msg.append(path);
				
			}
			return msg.toString();
		}

		private void handleClass(ClassModel targetClassModel) throws ShapeTransformException {
			if (targetClassModel.getTargetShapeModel()==null) {
				return;
			}
			handleVariables(targetClassModel);
			createPathFromItems(targetClassModel);
			
			LinkedList<ShapeModelMatchCount> queue = collectShapeModelMatchCount(targetClassModel);
			Collections.sort(queue);
			if (logger.isDebugEnabled()) {
				logQueue(targetClassModel, queue);
			}
			
			boolean matched = false;
			
			ShapeModel variableModel=null;
			
			while (!queue.isEmpty() && !unmatchedProperties.isEmpty()) {
				ShapeModelMatchCount sc = queue.pop();
				if (sc.getMatchCount()>0) {
					ShapeModel s = sc.getShapeModel();
					handleShape(s);
					// Update the match counts and sort the queue again
					// so that we pick the next "best" shape to join
					updateMatchCounts(queue);
					Collections.sort(queue);
					matched = true;
				} else if (isVariable(targetClassModel)){
					variableModel = sc.getShapeModel();
				}
			}
			
			if (!matched && variableModel!=null) {
				if (fromItemEnds.first==null) {
					fromItemEnds.first = variableModel;
				} else {
					// TODO: fix me
					throw new ShapeTransformException("Yikes! Don't know how to handle this case.");
				}
			}
			
			if (!unmatchedProperties.isEmpty()) {
				handleNestedResources(targetClassModel);
			}
			
			if (!unmatchedProperties.isEmpty()) {
				applyFormulas(targetClassModel);
			}
			
			
		}

		
		
		private void logQueue(ClassModel targetClassModel, LinkedList<ShapeModelMatchCount> queue) {
		
			logger.debug("handleClass({})", RdfUtil.localName(targetClassModel.getOwlClass()));
		
			StringBuilder builder = new StringBuilder();
			if (queue.isEmpty()) {
				logger.debug("No candidate source shapes found.");
			} else {
				builder.append("handleClass: candidate source shapes...");
				for (ShapeModelMatchCount e : queue) {
					builder.append("\n   ");
					builder.append(RdfUtil.localName(e.getShapeModel().getShape().getId()));
				}
				logger.debug(builder.toString());
			}
			
		}


		private void createPathFromItems(ClassModel targetClassModel) {
//			ShapeModel shapeModel = targetClassModel.getTargetShapeModel();
//			
//			outer: for (PropertyModel p : shapeModel.getProperties()) {
//				if (p instanceof DirectPropertyModel) {
//					DirectPropertyModel direct = (DirectPropertyModel) p;
//					PropertyConstraint pc = direct.getPropertyConstraint();
//					QuantifiedExpression formula = pc.getFormula();
//					if (formula != null) {
//						PrimaryExpression primary = formula.asPrimaryExpression();
//						if (primary instanceof PathExpression) {
//							PathExpression path = (PathExpression) primary;
//							for (PathStep step : path.getStepList()) {
//								if (step instanceof HasPathStep) {
//									
//									ProtoPathFromItem item = new ProtoPathFromItem(shapeModel, path);
//									
//
//									if (fromItemEnds.first == null) {
//										fromItemEnds.first = item;
//									} else {
//										item.setRest(fromItemEnds.last);
//										fromItemEnds.last = item;
//									}
//									
//									
//									if (logger.isDebugEnabled()) {
//										logger.debug("createPathFromItems  path: {}", path.simpleText());
//									}
//									continue outer;
//								}
//							}
//						}
//					}
//				}
//			}
			
		}


		private boolean isVariable(ClassModel targetClassModel) {
		
			return targetClassModel.getTargetShapeModel().getAccessor() instanceof VariablePropertyModel;
		}




		private void handleVariables(ClassModel targetClassModel) throws ShapeTransformException {
			ShapeModel targetShapeModel = targetClassModel.getTargetShapeModel();
			if (targetShapeModel != null) {
				Collection<VariablePropertyModel> varList = targetShapeModel.getVariables();
				if (varList != null) {
					for (VariablePropertyModel var : varList) {
						handleVariable(var);
						
//						ShapeModel varShape = var.getValueModel();
//						matchVariableProperties(targetShapeModel, varShape);
						
						bindVariable(var);
					}
				}
			}
			
		}

		private void handleVariable(VariablePropertyModel var) throws ShapeTransformException {
			
			ClassModel classModel = var.getValueModel().getClassModel();
			URI owlClass = classModel.getOwlClass();
			
			logger.debug("handleVariable(var.path: {}, var.owlClass: {})", var.simplePath(), owlClass.getLocalName());
			
			handleClass(classModel);
			
		}




		private void bindVariable(VariablePropertyModel targetVariable) {
			
			ClassModel classModel = targetVariable.getValueModel().getClassModel();
			Set<ShapeModel> candidateList = classModel.getCandidateSourceShapeModel();
			ProtoFromItemIterator sequence = new ProtoFromItemIterator(fromItemEnds.first);
			while (sequence.hasNext()) {
				ShapeModel shapeModel = sequence.next();
				if (candidateList.contains(shapeModel)) {
					targetVariable.setSourceShape(shapeModel);
					shapeModel.getDataChannel().setVariableName(targetVariable.getPredicate().getLocalName());
					return;
				}
			}
			
			
		}

		private void applyFormulas(ClassModel targetClassModel) throws ShapeTransformException {
			URI owlClass = targetClassModel.getOwlClass();
			
			logger.debug("BEGIN applyFormulas({})", owlClass==null?null:owlClass.getLocalName());
			for (PropertyGroup group : targetClassModel.getPropertyGroups()) {
				if (group.getSourceProperty()==null) {
					PropertyModel targetProperty = group.getTargetProperty();
					if (targetProperty instanceof DirectPropertyModel) {
						DirectPropertyModel direct = (DirectPropertyModel) targetProperty;
						PropertyConstraint constraint = direct.getPropertyConstraint();
						QuantifiedExpression formula = constraint.getFormula();
						if (formula != null) {

							if (propertyHandler.handleFormula(this, targetProperty)) {
								declareMatch(group);
								continue;
							}
							
							DerivedPropertyModel derived = new DerivedPropertyModel(targetProperty.getPredicate(), group, constraint);
							derived.setDeclaringShape(targetClassModel.getTargetShapeModel());
							group.add(derived);
							group.setSourceProperty(derived);
							declareMatch(group);
							
							continue;
						}
						
						
						
						// Formula is not defined on the target property.  See if it is defined on any 
						// of the source shapes.
						URI predicate = targetProperty.getPredicate();
						
						for (ShapeModel sourceShapeModel : targetClassModel.getCandidateSourceShapeModel()) {
							Shape sourceShape = sourceShapeModel.getShape();
							PropertyConstraint p = sourceShape.getDerivedPropertyByPredicate(predicate);
							if (p != null) {
								formula = p.getFormula();
								if (formula != null) {

									if (isFromItem(sourceShapeModel)) {
										
										
										DerivedPropertyModel derived = new DerivedPropertyModel(predicate, group, p);
										derived.setDeclaringShape(sourceShapeModel);
										sourceShapeModel.add(derived);
										group.add(derived);
										group.setSourceProperty(derived);
										declareMatch(group);
										continue;
									} else {
										throw new ShapeTransformException("TODO: Add FromItem for formula");
									}
								}
							}
							
						}
					}
					// Check for an available fixed value
					if (group.getSourceProperty()==null && targetProperty!=null) {
						for (PropertyModel pm : group) {
							if (pm.isSourceProperty() && pm instanceof FixedPropertyModel) {
								group.setSourceProperty(pm);
								declareMatch(group);
							}
						}
					}
				}
				
			}
			
		}


		private boolean isFromItem(ShapeModel sourceShapeModel) {
			if (fromItemEnds.first != null) {
				ProtoFromItemIterator sequence = new ProtoFromItemIterator(fromItemEnds.first);
				while (sequence.hasNext()) {
					ShapeModel shapeModel = sequence.next();
					if (shapeModel == sourceShapeModel) {
						return true;
					}
				}
			}
			return false;
		}

		private void handleNestedResources(ClassModel targetClassModel) throws ShapeTransformException {
			for (PropertyGroup group : targetClassModel.getPropertyGroups()) {
				ClassModel nested = group.getValueClassModel();
				
				if (nested != null && nested.hasUnmatchedProperty()) {
					handleClass(nested);
				} else {
					inverseFunctionalReference(group);
				}
			}
		}

		private boolean inverseFunctionalReference(PropertyGroup group) throws ShapeTransformException {
			PropertyModel targetProperty = group.getTargetProperty();
			if (group.getSourceProperty()==null && targetProperty instanceof BasicPropertyModel && reasoner!= null) {
				BasicPropertyModel basicTargetProperty = (BasicPropertyModel) targetProperty;
				PropertyConstraint pc = basicTargetProperty.getPropertyConstraint();
				if (pc!=null && pc.getValueClass() instanceof URI) {
				
					URI valueClass = (URI)pc.getValueClass();
					for (PropertyModel p : group) {
						if (p.isSourceProperty() && p instanceof StepPropertyModel) {
							StepPropertyModel step = (StepPropertyModel) p;
							StepPropertyModel nextStep = step.getNextStep();
							
						
							if (nextStep!=null && nextStep.getNextStep()==null) {
								URI inverseFunctionalPredicate = nextStep.getPredicate();
								if (reasoner.isInverseFunctionalProperty(inverseFunctionalPredicate)) {
									List<Shape> shapeList = shapeModelFactory.getShapeManager().getShapesByTargetClass(valueClass);
									
									for (Shape shape : shapeList) {
										if (shape.getPropertyConstraint(inverseFunctionalPredicate) != null) {
											
											DataChannel channel = shapeModelFactory.getDataChannelFactory().createDataChannel(shape);
											if (channel != null) {
												ShapeModel rightShape = shapeModelFactory.createShapeModel(shape, false);
												rightShape.setDataChannel(channel);
												
												PropertyModel idProperty = rightShape.getPropertyByPredicate(Konig.id);
												if (idProperty != null) {
													PropertyModel rightProperty = 
															rightShape.getPropertyByPredicate(inverseFunctionalPredicate);

													ShapeModel leftShape = nextStep.getDeclaringShape();
													URI leftPredicate = nextStep.getPropertyConstraint().getPredicate();
													if (leftPredicate != null) {
														PropertyModel leftProperty = leftShape.getPropertyByPredicate(leftPredicate);
														
														ProtoBinaryBooleanExpression condition = new ProtoBinaryBooleanExpression(
																TransformBinaryOperator.EQUAL, leftProperty, rightProperty);

														group.setSourceProperty(idProperty);
														declareMatch(group);
														if (
															(fromItemEnds.first==null) ||
															(fromItemEnds.first==fromItemEnds.last && fromItemEnds.first==leftShape)
														) {
															fromItemEnds.first = fromItemEnds.last = new ProtoJoinExpression(leftShape, rightShape, condition);
														} else if (fromItemEnds.first==fromItemEnds.last && fromItemEnds.first==rightShape) {
															fromItemEnds.first = fromItemEnds.last = new ProtoJoinExpression(rightShape, leftShape, condition);
														} else {
															
															throw new ShapeTransformException("Don't know how to build join. TODO: fix me!");
														} 
													}
													
													
												}
											}
										}
									}
									
								}
							}
						}
					}
				}
			}
			return false;
			
		}


		/**
		 * Recompute the matchCount values.
		 */
		private void updateMatchCounts(LinkedList<ShapeModelMatchCount> queue) {
			for (ShapeModelMatchCount sc : queue) {
				sc.updateMatchCount();
			}
			
		}

		private void handleShape(ShapeModel sourceShapeModel) throws ShapeTransformException {
			
			matchProperties(sourceShapeModel);
			buildFromExpression(sourceShapeModel);
			buildNestedProperties(sourceShapeModel);
			
		}


		

		private void buildFromExpression(ShapeModel newShapeModel) throws ShapeTransformException {
			
			beginBuildFromExpression(newShapeModel);
			
			if (abortFromExpression(newShapeModel)) {
				return;
			}
			
			if (fromItemEnds.first == null) {
				fromItemEnds.first = fromItemEnds.last = newShapeModel;
			} else {

				ProtoBooleanExpression condition = null;
				
				ClassModel newClassModel = newShapeModel.getClassModel();
				
				DataChannel newChannel = newShapeModel.getDataChannel();
				
				ProtoFromItemIterator sequence = new ProtoFromItemIterator(fromItemEnds.first);
				while (sequence.hasNext() && condition==null) {
					ShapeModel priorShapeModel = sequence.next();
					
					if (priorShapeModel.getDataChannel() == newChannel && newChannel!=null) {
						return;
					}
				
					
					ClassModel priorClassModel = priorShapeModel.getClassModel();
					
					// There are three important use cases to consider.
					//
					// CASE 1. priorShape and newShape describe the same entity.
					//
					// CASE 2. The entity described by newShape is a child of the entity
					//         described by priorShape.
					//
					// CASE 3. The entity described by newShape is accessed indirectly
					//         from the entity described by priorShape
					//
					// CASE 4. The entity described by priorShape is from a variable, and
					//         the entity described by newShape is referenced via that variable.
					
					if (priorClassModel == newClassModel) {
						
						// Try CASE 1
						condition = sameEntityJoinCondition(priorShapeModel, newShapeModel);
						
					} else  {
						
						// Try CASE 2
						condition = childEntityJoinCondition(priorShapeModel, newShapeModel);
						
						if (condition == null) {
							// Try CASE 3
							condition = peerJoinCondition(priorShapeModel, newShapeModel);
							
						}
						
					}
					
				}
				

				if (condition==null) {
					throw new ShapeTransformException("No join condition was found for shape " + newShapeModel.getShape().getId());
				}
				
				createFromItem(newShapeModel, condition);
				
			}
		}




		private boolean abortFromExpression(ShapeModel sourceShapeModel) {
			ClassModel classModel = sourceShapeModel.getClassModel();
			ShapeModel targetShapeModel = classModel.getTargetShapeModel();
			PropertyModel accessor = targetShapeModel.getAccessor();
			if (accessor instanceof DirectPropertyModel) {
				DirectPropertyModel direct = (DirectPropertyModel) accessor;
				PropertyConstraint p = direct.getPropertyConstraint();
				if (p.getFormula() != null) {
					return true;
				}
			}
			return false;
		}


		private void beginBuildFromExpression(ShapeModel newShapeModel) {
			
			if (logger.isDebugEnabled()) {
				String shapeId = RdfUtil.localName(newShapeModel.getShape().getId());
				String accessorId = newShapeModel.getClassModel().getTargetShapeModel().accessorPath();
				
				logger.debug("BEGIN buildFromExpression(newShapeModel.shape: {}, targetShapeModel.accessor: {})", shapeId, accessorId);
			}
			
		}


		private ProtoBooleanExpression peerJoinCondition(ShapeModel priorShapeModel, ShapeModel newShapeModel) throws ShapeTransformException {
			ClassModel newParentClassModel = newShapeModel.getClassModel().getParent();
			ClassModel priorParentClassModel = priorShapeModel.getClassModel().getParent();
			if (newParentClassModel == priorParentClassModel && newParentClassModel!=null) {
				
				ClassModel parentClassModel = newParentClassModel;
				
				
				ShapeModel newTargetShapeModel = newShapeModel.getClassModel().getTargetShapeModel();
				ShapeModel priorTargetShapeModel = priorShapeModel.getClassModel().getTargetShapeModel();
				
				
				PropertyModel newAccessor = newTargetShapeModel.getAccessor();
				PropertyModel priorAccessor = priorTargetShapeModel.getAccessor();
				
				Shape parentShape = parentClassModel.getTargetShapeModel().getShape();
				
				for (PropertyConstraint p : parentShape.getProperty()) {
					PropertyPath path = p.getPath();
					if (path instanceof SequencePath) {
						SequencePath sequence = (SequencePath)path;
						PropertyPath first = sequence.get(0);
						if (first instanceof PredicatePath) {
							PredicatePath predicatePath = (PredicatePath) first;
							URI predicate = predicatePath.getPredicate();
							
							if (predicate.equals(newAccessor.getPredicate())) {
								
								
								QuantifiedExpression formula = p.getFormula();
								if (formula != null) {
									
									PrimaryExpression primary = formula.asPrimaryExpression();
									if (primary instanceof PathExpression) {
										PathExpression otherPath = (PathExpression) primary;
										List<PathStep> stepList = otherPath.getStepList();
										PathStep otherFirst = stepList.get(0);
										if (otherFirst instanceof DirectionStep) {
											DirectionStep dir = (DirectionStep) otherFirst;
											if (dir.getDirection() == Direction.OUT) {
												URI otherPredicate = dir.getTerm().getIri();
												if (otherPredicate.equals(priorAccessor.getPredicate())) {

													PropertyModel aTail = lastProperty(newShapeModel, sequence);
													PropertyModel bTail = lastProperty(priorShapeModel, stepList);
													
													return new ProtoBinaryBooleanExpression(TransformBinaryOperator.EQUAL, aTail, bTail);
												}
											}
										}
									}
									
								}
							}
							
						} else {
							throw new ShapeTransformException("Unsupported PropertyPath: " + path.toString());
						}
					}
				}

			}
			return null;
		}

		private PropertyModel lastProperty(ShapeModel shapeModel, List<PathStep> stepList) throws ShapeTransformException {
			PropertyModel property = null;
			for (int i=1; i<stepList.size(); i++) {
				PathStep step = stepList.get(i);
				if (step instanceof DirectionStep) {
					DirectionStep dir = (DirectionStep) step;
					if (dir.getDirection() == Direction.OUT) {
						URI predicate = dir.getTerm().getIri();
						property = shapeModel.getPropertyByPredicate(predicate);
						if (property == null) {
							throw new ShapeTransformException("Property not found: " + predicate);
						}
						shapeModel = property.getValueModel();
					} else {
						throw new ShapeTransformException("IN direction not supported yet");
					}
				}
			}
			return property;
		}

		private PropertyModel lastProperty(ShapeModel shapeModel, SequencePath sequence) throws ShapeTransformException {
			PropertyModel property = null;
			
			for (int i=1; i<sequence.size(); i++) {
				PropertyPath p = sequence.get(i);
				if (p instanceof PredicatePath) {
					PredicatePath predicatePath = (PredicatePath) p;
					URI predicate = predicatePath.getPredicate();
					property = shapeModel.getPropertyByPredicate(predicate);
					if (property == null) {
						throw new ShapeTransformException("Property not found: " + predicate);
					}
					shapeModel = property.getValueModel();
					
					
				} else {
					throw new ShapeTransformException("Unsupported PropertyPath: " + sequence.toString());
				}
			}
			return property;
		}

		private void buildNestedProperties(ShapeModel s) throws ShapeTransformException {

			for (PropertyModel p : s.getProperties()) {
				PropertyGroup group = p.getGroup();
				if (group.getSourceProperty() == p) {
					ShapeModel valueModel = p.getValueModel();
					if (valueModel != null) {
						matchProperties(valueModel);
					}
				}
			}
			
		}

		private void createFromItem(ShapeModel newShapeModel, ProtoBooleanExpression condition) {
			
			if (fromItemEnds.first instanceof ShapeModel) {
				fromItemEnds.first = fromItemEnds.last = new ProtoJoinExpression((ShapeModel)fromItemEnds.first, newShapeModel, condition);
			} else {
				ProtoJoinExpression join = (ProtoJoinExpression) fromItemEnds.last;
				ShapeModel priorShapeModel = join.getRightShapeModel();
				fromItemEnds.last = new ProtoJoinExpression(priorShapeModel, newShapeModel, condition);
				join.setRight(fromItemEnds.last);
			}
			
		}



		private ProtoBooleanExpression childEntityJoinCondition(ShapeModel priorShapeModel, ShapeModel newShapeModel) {
			ProtoBinaryBooleanExpression condition = null;
			ClassModel priorClassModel = priorShapeModel.getClassModel();
			ClassModel newClassModel = newShapeModel.getClassModel();
			if (newClassModel.getParent() == priorClassModel) {
				ShapeModel newTargetShapeModel = newClassModel.getTargetShapeModel();
				PropertyModel targetAccessor = newTargetShapeModel.getAccessor();
				if (targetAccessor != null) {
					PropertyGroup accessorGroup = targetAccessor.getGroup();
					 condition = joinByAccessor(accessorGroup, priorShapeModel, newShapeModel);
				}
			}
			return condition;
		}
		
		private ProtoBinaryBooleanExpression joinByAccessor(PropertyGroup accessorGroup, ShapeModel parentShapeModel,
				ShapeModel childShapeModel) {
			

			
			PropertyModel parentProperty=null;
			PropertyModel childProperty=null;
			outer: for (PropertyModel p : accessorGroup) {
				if (p.getDeclaringShape() == parentShapeModel) {
					
					if (p instanceof DirectPropertyModel && p.getValueModel()==null) {
						// p is a direct IRI reference to the child entity.
						// If the child has an id property, then we can join on it.
						
						childProperty = childShapeModel.getPropertyByPredicate(Konig.id);
						parentProperty = p;
						break;
						
					}
					
					if (p instanceof StepPropertyModel) {
						StepPropertyModel firstStep = (StepPropertyModel) p;
						StepPropertyModel secondStep = firstStep.getNextStep();
						if (secondStep!=null && secondStep.getNextStep()==null) {
												
							
							PropertyGroup secondStepGroup = secondStep.getGroup();
							for (PropertyModel c : secondStepGroup) {
								
								if (c.getDeclaringShape() == childShapeModel) {
								
									if (c instanceof DirectPropertyModel) {
										parentProperty = secondStep.getDeclaringProperty();
										childProperty = c;
										break outer;
									} else if (c instanceof StepPropertyModel) {
										StepPropertyModel childStep = (StepPropertyModel) c;
										if (childStep.getNextStep()==null) {
											parentProperty = secondStep.getDeclaringProperty();
											childProperty = childStep.getDeclaringProperty();
											break outer;
										}
									}
								}
							}
						}
					}
				}
			}
			
			
			
			return parentProperty!=null && childProperty!=null ? 
				new ProtoBinaryBooleanExpression(TransformBinaryOperator.EQUAL, parentProperty, childProperty) : null;
		}

		private ProtoBooleanExpression sameEntityJoinCondition(ShapeModel priorShapeModel, ShapeModel newShapeModel) {
			
			PropertyModel priorProperty = priorShapeModel.getPropertyByPredicate(Konig.id);
			PropertyModel newProperty = newShapeModel.getPropertyByPredicate(Konig.id);
			
			if (priorProperty!=null && newProperty!=null) {
				return new ProtoBinaryBooleanExpression(TransformBinaryOperator.EQUAL, newProperty, priorProperty);
			}
			return null;
		}


		private void matchProperties(ShapeModel sourceShapeModel) throws ShapeTransformException {
			
			addFilteredChannel(sourceShapeModel);
			matchDirectProperties(sourceShapeModel);
			matchStepProperties(sourceShapeModel);
		}


		private void matchStepProperties(ShapeModel sourceShapeModel) throws ShapeTransformException {
			List<StepPropertyModel> stepProperties = sourceShapeModel.getStepProperties();
			for (StepPropertyModel step : stepProperties) {
				if (step.getNextStep()==null) {
					PropertyModel declaringProperty = step.getDeclaringProperty();
					PropertyGroup group = declaringProperty.getGroup();
					if (group.getTargetProperty()!=null && group.getSourceProperty()==null) {
						group.setSourceProperty(step);
					} else {
						ClassModel classModel = group.getParentClassModel();
						URI predicate = step.getPredicate();
						PropertyGroup peer = classModel.getPropertyGroupByPredicate(predicate);
						
						if (peer != null && peer.getTargetProperty()!=null && peer.getSourceProperty()==null) {
							peer.setSourceProperty(step);
							declareMatch(peer);
						}
					}
				} else if (step.getNextStep().getNextStep()==null) {
					PropertyGroup group = step.getGroup();
					if (group.getTargetProperty()!=null && group.getSourceProperty()==null) {
						URI inverseFunctionalPredicate = step.getNextStep().getPredicate();
						if (reasoner.isInverseFunctionalProperty(inverseFunctionalPredicate)) {
							StepPropertyModel nextStep = step.getNextStep();
							
							PropertyModel targetProperty = group.getTargetProperty();
							
							
							if (targetProperty instanceof BasicPropertyModel) {
								BasicPropertyModel basic = (BasicPropertyModel) targetProperty;
								PropertyConstraint pc = basic.getPropertyConstraint();
								
								
								URI valueClass = null;
								if (pc.getValueClass() instanceof URI) {
									valueClass = (URI) pc.getValueClass();
								} else if (pc.getShape() != null) {
									Shape nestedShape = pc.getShape();
									valueClass = nestedShape.getTargetClass();
								}
								
								if (valueClass == null) {
									continue;
								}
								
								List<Shape> shapeList = shapeModelFactory.getShapeManager().getShapesByTargetClass((URI)valueClass);
								
								for (Shape shape : shapeList) {
									if (shape.getPropertyConstraint(inverseFunctionalPredicate) != null) {
										
										DataChannel channel = shapeModelFactory.getDataChannelFactory().createDataChannel(shape);
										if (channel != null) {
											ShapeModel rightShape = shapeModelFactory.createShapeModel(shape, false);
											rightShape.setDataChannel(channel);
											
											PropertyModel idProperty = rightShape.getPropertyByPredicate(Konig.id);
											if (idProperty != null) {
												PropertyModel rightProperty = 
														rightShape.getPropertyByPredicate(inverseFunctionalPredicate);

												ShapeModel leftShape = nextStep.getDeclaringShape();
												URI leftPredicate = nextStep.getPropertyConstraint().getPredicate();
												if (leftPredicate != null) {
													PropertyModel leftProperty = leftShape.getPropertyByPredicate(leftPredicate);
													
													ProtoBinaryBooleanExpression condition = new ProtoBinaryBooleanExpression(
															TransformBinaryOperator.EQUAL, leftProperty, rightProperty);

													group.setSourceProperty(idProperty);
													declareMatch(group);
													if (
														(fromItemEnds.first==null) ||
														(fromItemEnds.first==fromItemEnds.last && fromItemEnds.first==leftShape)
													) {
														fromItemEnds.first = fromItemEnds.last = new ProtoJoinExpression(leftShape, rightShape, condition);
													} else if (fromItemEnds.first==fromItemEnds.last && fromItemEnds.first==rightShape) {
														fromItemEnds.first = fromItemEnds.last = new ProtoJoinExpression(rightShape, leftShape, condition);
													} else {
														
														throw new ShapeTransformException("Don't know how to build join. TODO: fix me!");
													} 
												}
											}
										}
									}
								}
							}
						}
						
					}
				}
			}
			
		}



		private void addFilteredChannel(ShapeModel sourceShapeModel) throws ShapeTransformException {
			ShapeModel targetShapeModel = sourceShapeModel.getClassModel().getTargetShapeModel();
			PropertyModel accessor = targetShapeModel.getAccessor();
			if (accessor instanceof BasicPropertyModel) {
				BasicPropertyModel basic = (BasicPropertyModel) accessor;
				PropertyConstraint p = basic.getPropertyConstraint();
				QuantifiedExpression q = p.getFormula();
				
				if (q !=null && containsHasStep(q)) {

					if (logger.isDebugEnabled()) {
						String formula = q.toSimpleString();
						String path = targetShapeModel==null ? null : targetShapeModel.accessorPath();
						String localName = RdfUtil.localName(sourceShapeModel.getShape().getId());
						logger.debug("addFilteredChannel({} : {}, formula: {}", localName, path, formula);
						
					}
					
					DataChannel rawChannel = sourceShapeModel.getDataChannel();
					FilteredDataChannel filtered = new FilteredDataChannel(sourceShapeModel.getShape(), rawChannel, q);
					sourceShapeModel.setDataChannel(filtered);
					
					
					appendDataChannel(sourceShapeModel);
				}
				
			}
			
			
			
			
		}


		private void appendDataChannel(ShapeModel shapeModel) {
		
			if (logger.isDebugEnabled()) {
				logger.debug("appendDataChannel(shape.localName: {})", shapeModel.simpleName());
			}
			
			if (fromItemEnds.first == null) {
				fromItemEnds.first = fromItemEnds.last = shapeModel;
			} else if (fromItemEnds.last instanceof ProtoJoinExpression){

				ProtoJoinExpression join = (ProtoJoinExpression) fromItemEnds.last;
				ShapeModel priorShapeModel = join.getRightShapeModel();
				fromItemEnds.last = new ProtoJoinExpression(priorShapeModel, shapeModel, null);
				join.setRight(fromItemEnds.last);
			} else if (fromItemEnds.first==fromItemEnds.last && fromItemEnds.last instanceof ShapeModel) {
				ShapeModel first = (ShapeModel) fromItemEnds.first;
				ProtoJoinExpression join = new ProtoJoinExpression(first, shapeModel, null);
				fromItemEnds.first = fromItemEnds.last = join;
			}
			
		}


		private boolean containsHasStep(QuantifiedExpression q) {
		
			HasStepVisitor visitor = hasStepVisitor();
			q.dispatch(visitor);
			
			return visitor.foundHasStep();
		}


		private HasStepVisitor hasStepVisitor() {
			if (hasStepVisitor == null) {
				hasStepVisitor = new HasStepVisitor();
			} else {
				hasStepVisitor.reset();
			}
			return hasStepVisitor;
		}


		private void matchDirectProperties(ShapeModel sourceShapeModel) throws ShapeTransformException {
			String path = null;
			
			Collection<PropertyModel> list = sourceShapeModel.getProperties();
			for (PropertyModel p : list) {
				PropertyGroup group = p.getGroup();
				
				if (group.getSourceProperty() == null) {

					matchProperty(p);
					
				} 
//				else if (logger.isDebugEnabled()) {
//						logger.debug("matchDirectProperties: SKIP {}", p.simplePath());
//				}
				
			}

			if (logger.isDebugEnabled()) {
				logger.debug("END matchDirectProperties({} : {})", RdfUtil.localName(sourceShapeModel.getShape().getId()), path);
			}
			
		}


		private void matchProperty(PropertyModel p) throws ShapeTransformException {

			if (logger.isDebugEnabled()) {
				logger.debug("matchProperty({}, class: {})", p.simplePath(), p.getClass().getSimpleName());
			}
		
			if (p instanceof DirectPropertyModel) {
				
				DirectPropertyModel direct = (DirectPropertyModel) p;
				
				StepPropertyModel step = direct.getStepPropertyModel();
				if (step != null && isTopShape(direct.getDeclaringShape())) {
					logger.debug("replacing property with step: {}", step.getPredicate().getLocalName());
					p = step;
				}
				
				PropertyGroup group = p.getGroup();
				
				if (group.getTargetProperty()!=null && group.getSourceProperty()==null) {
					group.setSourceProperty(p);
					declareMatch(group);
					
					ShapeModel valueModel = p.getValueModel();
					if (valueModel != null) {
						matchProperties(valueModel);
					}
				}
				
				
			} else if (p instanceof IdPropertyModel) {

				PropertyGroup group = p.getGroup();
				group.setSourceProperty(p);
				declareMatch(group);
				
			} 
		}
		
		private boolean isTopShape(ShapeModel declaringShape) {
			while (declaringShape.getAccessor()!=null) {
				declaringShape = declaringShape.getAccessor().getDeclaringShape();
			}
			ClassModel classModel = declaringShape.getClassModel();
			
			return classModel.getParent()==null;
		}
		
		public void declareMatch(PropertyGroup group) {
			
			if (logger.isDebugEnabled()) {
				if (group.getTargetProperty()==null) {
					logger.debug("declareMatch(group.targetProperty: null)");
				} else {
					String sourceProperty = "null";
					PropertyModel sp = group.getSourceProperty();
					if (sp!=null) {
						if (sp instanceof StepPropertyModel) {
							StepPropertyModel step = (StepPropertyModel) sp;
							sourceProperty = step.getDeclaringProperty().getPredicate().getLocalName();
						} else {
							sourceProperty = sp.getPredicate().getLocalName();
						}
					}
					logger.debug("declareMatch({} => {})", sourceProperty, group.getTargetProperty().simplePath());
				}
			}
			unmatchedProperties.remove(group);
		
		}

		private LinkedList<ShapeModelMatchCount> collectShapeModelMatchCount(ClassModel classModel) throws ShapeTransformException {

			LinkedList<ShapeModelMatchCount> list = new LinkedList<>();
			Set<ShapeModel> set = classModel.getCandidateSourceShapeModel();
			if (set == null) {
				set = buildCandidateSources(classModel);
			}
			
			for (ShapeModel s : set) {
				ShapeModelMatchCount sc = new ShapeModelMatchCount(s);
				list.add(sc);
			}
			
			return list;
		}

		private Set<ShapeModel> buildCandidateSources(ClassModel classModel) throws ShapeTransformException {
			Set<ShapeModel> set = new HashSet<>();
			classModel.setCandidateSourceShapeModel(set);
			
			shapeModelFactory.addSourceShapes(classModel.getTargetShapeModel());
			
			
			return set;
		}

		/**
		 * Add properties from a given ClassModel to the set of unmatched properties, recursively.
		 */
		private void collectUnmatchedProperties(ClassModel classModel) {
			logger.debug("collectUnmatchedProperties({})", RdfUtil.localName(classModel.getOwlClass()));
			for (PropertyGroup p : classModel.getPropertyGroups()) {
				
				if (p.getTargetProperty()!=null && Konig.modified.equals(p.getTargetProperty().getPredicate())) {
					p.setSourceProperty(new FixedPropertyModel(Konig.modified, p, new LiteralImpl("{modified}")));
				} else if (p.getValueClassModel() != null) {
					collectUnmatchedProperties(p.getValueClassModel());
				} else if (p.getTargetProperty() instanceof DirectPropertyModel){
					unmatchedProperties.add(p);
				}
			}
			
		}
	}
	
	

	private static class HasStepVisitor implements FormulaVisitor {
		private boolean foundHasStep = false;
		
		public void reset() {
			foundHasStep = false;
		}

		@Override
		public void enter(Formula formula) {
			
			if (formula instanceof HasPathStep) {
				foundHasStep = true;
			}
			
		}
		
		public boolean foundHasStep() {
			return foundHasStep;
		}

		@Override
		public void exit(Formula formula) {
		}
		
	}
}
