package io.konig.transform.proto;

/*
 * #%L
 * Konig Transform
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import static io.konig.core.impl.RdfUtil.localName;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.path.DirectionStep;
import io.konig.core.path.HasStep;
import io.konig.core.path.HasStep.PredicateValuePair;
import io.konig.core.path.Step;
import io.konig.core.vocab.Konig;
import io.konig.formula.DirectedStep;
import io.konig.formula.Direction;
import io.konig.formula.HasPathStep;
import io.konig.formula.PathExpression;
import io.konig.formula.PathStep;
import io.konig.formula.PrimaryExpression;
import io.konig.formula.QuantifiedExpression;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyManager;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;
import io.konig.shacl.ShapePropertyPair;
import io.konig.transform.ShapeTransformException;
import io.konig.transform.rule.DataChannel;
import io.konig.transform.rule.TransformBinaryOperator;

public class ShapeModelFactory {
	private static Logger logger = LoggerFactory.getLogger(ShapeModelFactory.class);
	

	private ShapeManager shapeManager;
	private PropertyManager propertyManager;
	private DataChannelFactory dataChannelFactory;
	private OwlReasoner reasoner;
	
	private int maxInlineVocabularySize=30;
	
	
	
	public ShapeModelFactory(
			ShapeManager shapeManager, 
			PropertyManager propertyManager, 
			DataChannelFactory dataChannelFactory,
			OwlReasoner reasoner
	) {
		this.shapeManager = shapeManager;
		this.propertyManager = propertyManager;
		this.dataChannelFactory = dataChannelFactory;
		this.reasoner = reasoner;
	}
	
	
	public int getMaxInlineVocabularySize() {
		return maxInlineVocabularySize;
	}


	public void setMaxInlineVocabularySize(int maxInlineVocabularySize) {
		this.maxInlineVocabularySize = maxInlineVocabularySize;
	}


	public ShapeModel createShapeModel(Shape shape) throws ShapeTransformException {
		Worker worker = new Worker();
		
		return worker.execute(shape);
	}
	
	private class Worker {
		
		private ShapeModel root;

		private MatchMaker matchMaker = new MatchMaker();
		
		public ShapeModel execute(Shape shape) throws ShapeTransformException {
			ShapeModel result = createShapeModel(shape);
			mapProperties(result);
			buildJoinConditions(result);
			return result;
		}

		private void buildJoinConditions(ShapeModel result) throws ShapeTransformException {
			
			List<SourceShapeInfo> list = result.getClassModel().getCommittedSources();
			Collections.sort(list);
			
			for (int i=1; i<list.size(); i++) {
				
				SourceShapeInfo info = list.get(i);
				join(result, info, i);
			}
			
		}

		private void join(ShapeModel targetShapeModel, SourceShapeInfo info, int index) throws ShapeTransformException {

			List<SourceShapeInfo> list = targetShapeModel.getClassModel().getCommittedSources();
			for (int i=0; i<index; i++) {
				SourceShapeInfo prior = list.get(i);
				if (joinPrior(prior, info)) {
					return;
				}
			}
			String shapeId = RdfUtil.localName(info.getSourceShape().getShape().getId());
			throw new ShapeTransformException("Failed to find join condition for " + shapeId);
		}

		private boolean joinPrior(SourceShapeInfo prior, SourceShapeInfo info) {
			
			ClassModel priorClassModel = prior.getSourceShape().getClassModel();
			ClassModel infoClassModel = info.getSourceShape().getClassModel();
			
			if (priorClassModel == infoClassModel) {
				// TODO: Join shapes that describe the same entity.
			} else {
				// Join shapes that describe different entities.
				PropertyModel targetAccessor = info.getTargetAccessor();
				if (targetAccessor instanceof StepPropertyModel) {
					StepPropertyModel targetStep = (StepPropertyModel) targetAccessor;
					
					if (targetStep.getStepIndex()==0 && targetStep.getDirection()==Direction.IN) {
						// For now we only handle the special case where targetAccessor is the first element 
						// within a path, and has an inward direction.
						
						// TODO: generalize this to handle the other possibilities.
						
						ClassModel targetClassModel = targetStep.getDeclaringShape().getClassModel();
						if (priorClassModel == targetClassModel) {
							
							URI predicate = targetStep.getPredicate();
							ShapeModel priorShape = prior.getSourceShape();
							ShapeModel infoShape = info.getSourceShape();
							
							PropertyConstraint p = infoShape.getShape().getPropertyConstraint(predicate);
							
							if (p != null) {
								
								PropertyModel right = priorShape.getPropertyByPredicate(Konig.id);
								PropertyModel left = producePropertyModel(infoShape, p);
								ProtoBinaryBooleanExpression condition = new ProtoBinaryBooleanExpression(
										TransformBinaryOperator.EQUAL, left, right);
								
								info.setJoinCondition(condition);
								
								if (logger.isDebugEnabled()) {
									logger.debug("joinPrior: Join {} with {} ON {}={}",
											RdfUtil.localName(infoShape.getShape().getId()),
											RdfUtil.localName(priorShape.getShape().getId()),
											left.getPredicate().getLocalName(),
											right.getPredicate().getLocalName());
								}
								
								return true;
							}
							
						}
					}
				}
			}
			
			return false;
		}

		private PropertyModel producePropertyModel(ShapeModel shapeModel, PropertyConstraint p) {
			URI predicate = p.getPredicate();
			PropertyModel result = shapeModel.getPropertyByPredicate(predicate);
			if (result == null) {
				ClassModel classModel = shapeModel.getClassModel();
				PropertyGroup group = classModel.produceGroup(Direction.OUT, predicate);
				result = new DirectPropertyModel(predicate, group, p);
				fullAttachProperty(result, shapeModel, group);
			}
			return result;
		}

		public ShapeModel createShapeModel(Shape shape) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("createShapeModel: {}", localName(shape.getId()));
			}
			ShapeModel targetShapeModel = targetShapeModel(shape, shape.getTargetClass());
			if (root == null) {
				root = targetShapeModel;
			}
			
			findCandidateSourceShapes(targetShapeModel);
			
			return targetShapeModel;
		}

		private void mapProperties(ShapeModel targetShapeModel) throws ShapeTransformException {
			
			ClassModel classModel = targetShapeModel.getClassModel();
			
			List<SourceShapeInfo> infoList = new ArrayList<>(classModel.getCandidateSources());
			
			while (!infoList.isEmpty()) {
				sortSourceShapeInfo(infoList);
				// The sorting process also filters out candidate shapes that have no matches.
				// So we need to check, once again, that the list is not empty.
				if (!infoList.isEmpty()) {
					SourceShapeInfo info = infoList.remove(0);
					if (info.getMatchCount()>0) {
						if (logger.isDebugEnabled()) {
							logger.debug("mapProperties: Mapping {} to {}",
									RdfUtil.localName(info.getSourceShape().getShape().getId()),
									RdfUtil.localName(targetShapeModel.getShape().getId())
									);
						}
						
						info.dispatch(matchMaker);
					}
				}
			}
			
			resolveIriReferences(targetShapeModel);
			
			
		}

		


		private void resolveIriReferences(ShapeModel targetShapeModel) throws ShapeTransformException {
			for (SourceShapeInfo info : targetShapeModel.getClassModel().getCommittedSources()) {
				for (DirectPropertyModel targetDirect : info.getMatchedTargetProperties()) {
					PropertyConstraint p = targetDirect.getPropertyConstraint();
					if (p.getValueClass() instanceof URI && p.getShape()==null) {
						PropertyModel sourceProperty = targetDirect.getGroup().getSourceProperty();
						if (sourceProperty instanceof StepPropertyModel) {
							StepPropertyModel sourceStep = (StepPropertyModel) sourceProperty;
							resolveIriReference(sourceStep, targetDirect);
						}
					}
				}
			}
			
		}

		private void resolveIriReference(StepPropertyModel sourceStep, DirectPropertyModel targetDirect) throws ShapeTransformException {
			
			URI valueClass = (URI) targetDirect.getPropertyConstraint().getValueClass();
			
			List<Vertex> individuals = reasoner.getGraph().v(valueClass).in(RDF.TYPE).toVertexList();
			if (!individuals.isEmpty() && individuals.size()<=maxInlineVocabularySize) {
				IriResolutionStrategy strategy = new LookupPredefinedNamedIndividual(individuals);
				sourceStep.setIriResolutionStrategy(strategy);
				if (logger.isDebugEnabled()) {
					logger.debug("resolveIriReference: Using LookupPredefinedNamedIndividual strategy for {}.{}",
							RdfUtil.localName(targetDirect.getDeclaringShape().getShape().getId()),
							targetDirect.getPredicate().getLocalName());
				}
			} else {
				String msg = MessageFormat.format("No strategy for resolving IRI for {0}.{1}", 
						RdfUtil.localName(targetDirect.getDeclaringShape().getShape().getId()),
						targetDirect.getPredicate().getLocalName());
				
				throw new ShapeTransformException(msg);
			}
			
		}

		private void sortSourceShapeInfo(List<SourceShapeInfo> infoList) throws ShapeTransformException {
			computeMatchCounts(infoList);
			Collections.sort(infoList);
			Iterator<SourceShapeInfo> sequence = infoList.iterator();
			while (sequence.hasNext()) {
				SourceShapeInfo info = sequence.next();
				if (info.getMatchCount() == 0) {
					if (logger.isDebugEnabled()) {
						logger.debug("sortSourceShapeInfo: remove {}", RdfUtil.localName(info.getSourceShape().getShape().getId()));
					}
					sequence.remove();
				}
			}
			
		}

		private void computeMatchCounts(List<SourceShapeInfo> infoList) throws ShapeTransformException {
			for (SourceShapeInfo info : infoList) {
				info.computeMatchCount();
			}
			
		}

		private void findCandidateSourceShapes(ShapeModel targetShapeModel) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("findCandidateSourceShapes: for {}", RdfUtil.localName(targetShapeModel.getShape().getId()));
			}
			scanSimpleDirectProperties(targetShapeModel);
			scanPathExpressions(targetShapeModel);
			
			ClassModel classModel = targetShapeModel.getClassModel();
			if (classModel.getCandidateSources()==null) {
				String msg = MessageFormat.format("No candidate sources found for {0} in ClassModel[{1}]", 
						RdfUtil.localName(targetShapeModel.getShape().getId()), classModel.hashCode());
				
				throw new ShapeTransformException(msg);
			}
			
		}

		/**
		 * Iterate over the simple direct properties (i.e. direct properties without a formula)
		 * and find candidate source shapes.
		 */
		private void scanSimpleDirectProperties(ShapeModel targetShapeModel) throws ShapeTransformException {
			URI targetClass = targetShapeModel.getShape().getTargetClass();
			
			List<Shape> candidateList = shapeManager.getShapesByTargetClass(targetClass);
			
			Resource targetShapeId = targetShapeModel.getShape().getId();
			ClassModel classModel = targetShapeModel.getClassModel();
			for (Shape sourceShape : candidateList) {
				if (targetShapeId.equals(sourceShape.getId())) {
					continue;
				}
				SourceShapeInfo info = produceSourceShapeInfo(targetShapeModel, sourceShape);
				if (info != null) {
					classModel.add(info);
				}
			}
		}
		


		private SourceShapeInfo produceSourceShapeInfo(ShapeModel targetShapeModel, Shape sourceShape) throws ShapeTransformException {
			SourceShapeInfo info = null;
			ShapeModel sourceShapeModel = null;
			for (PropertyConstraint p : sourceShape.getProperty()) {
				URI predicate = p.getPredicate();
				if (predicate != null) {
					PropertyModel targetProperty = targetShapeModel.getPropertyByPredicate(predicate);
					if (targetProperty != null) {
						if (sourceShapeModel == null) {
							sourceShapeModel = new ShapeModel(sourceShape);
						}
						producePropertyModel(targetProperty, sourceShapeModel);
					} else if (p.getFormula() != null){
						sourceShapeModel = scanSourcePath(targetShapeModel, sourceShapeModel, sourceShape, p);
						
					}
				}
			}
			if (sourceShapeModel != null) {
				info = new SourceShapeInfo(sourceShapeModel);
				addIdProperty(sourceShapeModel);
			}
			if (logger.isDebugEnabled()) {
				String message = info == null ?
						"produceSourceShapeInfo: Ignoring {}.  No matching properties found." :
						"produceSourceShapeInfo: Created SourceShapeInfo for {}";
				logger.debug(message, RdfUtil.localName(sourceShape.getId()));
			}
			return info;
		}


		private ShapeModel scanSourcePath(
			ShapeModel targetShapeModel, 
			ShapeModel sourceShapeModel, 
			Shape sourceShape, 
			PropertyConstraint p
		) throws ShapeTransformException {
			
			ShapeModel sourceShapeModelParam = sourceShapeModel;
			ClassModel classModel = targetShapeModel.getClassModel();
			
			if (logger.isDebugEnabled()) {
				logger.debug("scanSourcePath: scanning {} from {} seeking match for {} in ClassModel({})",
						p.getPredicate().getLocalName(), 
						RdfUtil.localName(sourceShape.getId()),
						RdfUtil.localName(targetShapeModel.getShape().getId()),
						classModel.hashCode());
			}
			
			Path path = p.getEquivalentPath();
			
			
			if (path != null) {
				
				
				DirectPropertyModel directSourceProperty = null;
				StepPropertyModel priorStep = null;
				
				List<Step> stepList = path.asList();
				for (int index=0; index<stepList.size(); index++) {
					Step step = stepList.get(index);
					if (step instanceof HasStep) {
						HasStep hasStep = (HasStep) step;
						List<PredicateValuePair> pairList = hasStep.getPairList();
						
						priorStep.setFilter(pairList);
						if (classModel != null) {
							for (PredicateValuePair pair : pairList) {
								URI predicate = pair.getPredicate();
								Value value = pair.getValue();
								if (value instanceof Literal) {
									PropertyGroup childGroup = classModel.produceOutGroup(predicate);
									if (childGroup.getTargetProperty()!=null) {
										if (logger.isDebugEnabled()) {
											logger.debug("scanSourcePath: Adding fixed property {}={} to group({}) in ClassModel({})",
													childGroup.getTargetProperty().simplePath(), 
													value.stringValue(), 
													childGroup.hashCode(),
													childGroup.getParentClassModel().hashCode());
										}
										FixedPropertyModel fixed = new FixedPropertyModel(predicate, childGroup, value);
										attachProperty(fixed, sourceShapeModel, childGroup);
										priorStep.addFilterProperty(fixed);
									}
								}
							}
						}
						
					} else	if (step instanceof DirectionStep) {
						DirectionStep direction = (DirectionStep) step;
						
						
						if (direction.getDirection() == Direction.OUT) {

							if (classModel == null) {
								unwind(priorStep);
								return sourceShapeModelParam;
							}
							if (directSourceProperty == null) {

								URI predicate = p.getPredicate();
								PropertyGroup group = classModel.produceGroup(Direction.OUT, predicate);
								
								if (sourceShapeModel == null) {
									sourceShapeModel = new ShapeModel(sourceShape);
									sourceShapeModel.setClassModel(classModel);
								}
								directSourceProperty = new DirectPropertyModel(predicate, group, p);
								fullAttachProperty(directSourceProperty, sourceShapeModel, group);
							}
							
							URI stepPredicate = direction.getPredicate();
							PropertyGroup stepGroup = classModel.produceGroup(Direction.OUT, stepPredicate);
							
							StepPropertyModel outStep = new StepPropertyModel(stepPredicate, Direction.OUT, stepGroup, directSourceProperty, index);
							attachProperty(outStep, sourceShapeModel, stepGroup);
							
							directSourceProperty.setPathTail(outStep);
							
							if (priorStep != null) {
								priorStep.setNextStep(outStep);
							}


							if (logger.isDebugEnabled()) {
								logger.debug("scanSourcePath: Added .{} at step[{}] from {}.{} in group({}) of ClassModel[{}]", 
										stepPredicate.getLocalName(), 
										index, 
										RdfUtil.localName(sourceShape.getId()), 
										directSourceProperty.getPredicate().getLocalName(),
										stepGroup.hashCode(),
										stepGroup.getParentClassModel().hashCode());
							}
							
							priorStep = outStep;
							URI valueClass = valueClass(direction);
							classModel = stepGroup.produceValueClassModel(valueClass);
							
							
						} else {
							throw new ShapeTransformException("TODO: support IN direction");
						}
					}
				}
			}
			
			return sourceShapeModel;
		}

		

		private URI valueClass(DirectionStep step) {
			Set<URI> set=null;
			if (step.getDirection()==Direction.OUT) {
				set = reasoner.rangeIncludes(step.getPredicate());
			} else {
				set = reasoner.domainIncludes(step.getPredicate());
			}
			return set==null || set.isEmpty() ? null : set.iterator().next();
		}

		private void unwind(StepPropertyModel step) {
			if (step != null) {
				DirectPropertyModel direct = step.getDeclaringProperty();
				direct.getGroup().remove(direct);
				while (step != null) {
					PropertyGroup group = step.getGroup();
					group.remove(step);
					if (group.isEmpty()) {
						group.getParentClassModel().removeGroup(step.getDirection(), step.getPredicate());
					}
					step = step.getPreviousStep();
				}
			}
		}

		private void scanPathExpressions(ShapeModel targetShapeModel) throws ShapeTransformException {

			if (logger.isDebugEnabled()) {
				logger.debug("scanPathExpressions: Scanning {}", RdfUtil.localName(targetShapeModel.getShape().getId()));
			}
			URI targetClass = targetShapeModel.getShape().getTargetClass();
			if (targetClass == null) {
				throw new ShapeTransformException("targetClass must be defined for " + 
						RdfUtil.localName(targetShapeModel.getShape().getId()));
			}
			
			for (PropertyModel p : targetShapeModel.getProperties()) {
				if (p instanceof DirectPropertyModel) {
					DirectPropertyModel direct = (DirectPropertyModel) p;
					if (direct.getPathTail() !=null) {
						scanPath(direct);
					}
				}
			}
			
		}


		private void scanPath(DirectPropertyModel direct) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("scanPath: Scanning {} in {}", 
						direct.getPredicate().getLocalName(), 
						RdfUtil.localName(direct.getDeclaringShape().getShape().getId()));
			}
			StepPropertyModel step = direct.getPathHead();
			
			while (step != null) {
				switch (step.getDirection()) {
				case IN:
					scanInStep(step); 
					break;
				case OUT:
					scanOutStep(step);
					break;
				}
				step = step.getNextStep();
			}
			
		}

		private void scanOutStep(StepPropertyModel step) throws ShapeTransformException {
			
			List<SourceShapeInfo> set = shapeModelsForOutStep(step);
			for (SourceShapeInfo info : set) {
				ShapeModel sourceShapeModel = info.getSourceShape();
				producePropertyModel(step, sourceShapeModel);
			}
		}

		

		

		private PropertyModel producePropertyModel(PropertyModel targetProperty, ShapeModel sourceShapeModel) {
			
			URI predicate = targetProperty.getPredicate();
			PropertyModel sourceProperty = sourceShapeModel.getPropertyByPredicate(predicate);
			if (sourceProperty == null) {
				PropertyConstraint p = sourceShapeModel.getShape().getPropertyConstraint(predicate);
				if (p != null) {
					PropertyGroup group = targetProperty.getGroup();
					sourceProperty = new DirectPropertyModel(predicate, group, p);
					fullAttachProperty(sourceProperty, sourceShapeModel, group);
					if (logger.isDebugEnabled()) {
						logger.debug("Added '{}' from {} to group[{}] in ClassModel[{}]",
							predicate.getLocalName(), 
							RdfUtil.localName(sourceShapeModel.getShape().getId()),
							group.hashCode(),
							group.getParentClassModel().hashCode());
					}
				}
			}
			
			
			return sourceProperty;
		}

		private List<SourceShapeInfo> shapeModelsForOutStep(StepPropertyModel step) throws ShapeTransformException {
			if (step.getPreviousStep() != null) {
				List<SourceShapeInfo> result = step.getPreviousStep().getValueShapeInfo();
				if (result == null) {
					String message = MessageFormat.format(
							"ShapeModels not found for {0}{1}", 
							directionSymbol(step), step.getPredicate().getLocalName());
					throw new ShapeTransformException(message);
				}
				return result;
			} else {
				throw new ShapeTransformException("TODO: handle initial step");
			}
		}

		private void scanInStep(StepPropertyModel step) throws ShapeTransformException {
			
			if (step.getPreviousStep() != null){
				// TODO: Handle steps after the first step.
				throw new ShapeTransformException("Inverse path elements are currently supported only at the beginning of a path");
			}
			
			ShapeModel targetShapeModel = step.getDeclaringShape();
			DirectPropertyModel direct = step.getDeclaringProperty();
			
			URI targetClass = targetShapeModel.getShape().getTargetClass();
			
			URI predicate = step.getPredicate();
			if (logger.isDebugEnabled()) {
				logger.debug("scanPathExpressions: Inspecting '{}' for {}#{}",
						predicate.getLocalName(), 
						RdfUtil.localName(targetShapeModel.getShape().getId()), 
						direct.getPredicate().getLocalName());
			}
			Set<ShapePropertyPair> set = propertyManager.propertyConstraintsByPathOrFormula(step.getPredicate());
			
			if (set == null) {
				if (logger.isDebugEnabled()) {
					logger.debug(
							"scanPathExpressions: No source shapes found for ^{}",
							predicate.getLocalName());
				}
			} else {
			
				for (ShapePropertyPair pair : set) {
					
					PropertyConstraint sourceProperty = pair.getProperty();
					URI valueType = valueClass(sourceProperty);
					if (valueType != null && reasoner.isSubClassOf(valueType, targetClass)) {
						addInverseSource(targetShapeModel, direct, pair);
						
					} else {
						if (logger.isDebugEnabled() && sourceProperty!=direct.getPropertyConstraint()) {
							logger.debug("scanPathExpressions: Value class not defined for '{}' referenced from '{}' in '{}'", 
									RdfUtil.localName(sourceProperty.getPredicate()), 
									direct.getPredicate().getLocalName(),
									RdfUtil.localName(targetShapeModel.getShape().getId()));
						}
					}
				}
			}
		}

		private void addInverseSource(ShapeModel targetShapeModel, DirectPropertyModel targetProperty, ShapePropertyPair pair) throws ShapeTransformException {
			
			
			StepPropertyModel head = targetProperty.getPathHead();
			
			if (head == null) {
				throw new ShapeTransformException("Path head not found");
			}
			ClassModel targetClassModel = targetShapeModel.getClassModel();
			
			ShapeModel sourceShapeModel = new ShapeModel(pair.getShape());
			ClassModel sourceClassModel = head.getValueClassModel();
			sourceShapeModel.setClassModel(sourceClassModel);
			
			
			if (logger.isDebugEnabled()) {
				logger.debug("addInverseSource: Add {} to ClassModel[{}]", 
						RdfUtil.localName(sourceShapeModel.getShape().getId()),
						sourceClassModel.hashCode());
			}
			
			SourceShapeInfo info = new SourceShapeInfo(sourceShapeModel);
			info.setTargetAccessor(head);
			targetClassModel.addCandidateSourceShapeModel(info);

			head.addValueShapeInfo(info);
		}

		private URI valueClass(PropertyConstraint sourceProperty) {
			Resource type = sourceProperty.getValueClass();
			if (type instanceof URI) {
				return (URI) type;
			} else {
				Shape shape = sourceProperty.getShape();
				if (shape != null) {
					return shape.getTargetClass();
				}
			}
			return null;
		}

		private ShapeModel targetShapeModel(Shape shape, URI targetClass) throws ShapeTransformException {
			if (targetClass == null) {
				throw new ShapeTransformException(
					"targetClass must be defined for Shape " + localName(shape.getId()));
			}
			ClassModel classModel = new ClassModel(shape.getTargetClass());
			if (logger.isDebugEnabled()) {
				logger.debug("targetShapeModel created ShapeModel for {} in ClassModel[{}]]", 
						localName(shape.getId()), 
						classModel.hashCode());
			}
			ShapeModel targetShapeModel = new ShapeModel(shape);
			targetShapeModel.setClassModel(classModel);
			classModel.setTargetShapeModel(targetShapeModel);
			addIdProperty(targetShapeModel);
			addTargetDirectProperties(targetShapeModel);
			
			return targetShapeModel;
		}

		private void addIdProperty(ShapeModel shapeModel) {
			Shape targetShape = shapeModel.getShape();
			
			if (targetShape.getNodeKind()==NodeKind.IRI || targetShape.getIriTemplate()!=null || targetShape.getIriFormula()!=null) {
				ClassModel classModel = shapeModel.getClassModel();
				PropertyGroup group = classModel.produceOutGroup(Konig.id);
				IdPropertyModel propertyModel = new IdPropertyModel(group);
				fullAttachProperty(propertyModel, shapeModel, group);
				if (logger.isDebugEnabled()) {
					logger.debug("addIdProperty: Added 'id' property to {}", RdfUtil.localName(targetShape.getId()));
				}
			}
		}

		private void addTargetDirectProperties(ShapeModel shapeModel) throws ShapeTransformException {
			if (logger.isDebugEnabled()) {
				logger.debug("addTargetDirectProperties for {}", localName(shapeModel.getShape().getId()));
			}
			
			
			ClassModel classModel = shapeModel.getClassModel();
			
			for (PropertyConstraint p : shapeModel.getShape().getProperty()) {
				QuantifiedExpression formula = p.getFormula();
			
				URI predicate=p.getPredicate();
				if (predicate == null) {
					continue;
				}
					
				PropertyGroup group = classModel.produceGroup(Direction.OUT, predicate);
				
				if (logger.isDebugEnabled()) {
					logger.debug("addTargetDirectProperties: Adding {} from {} to group[{}] in ClassModel[{}]",
							predicate.getLocalName(),
							localName(shapeModel.getShape().getId()),
							group.hashCode(),
							classModel.hashCode());
				}
				DirectPropertyModel direct = directPropertyModel(shapeModel, predicate, group, p);
				if (formula != null) {
					PrimaryExpression primary = formula.asPrimaryExpression();
					if (primary instanceof PathExpression) {
						attachPathSteps(direct, (PathExpression) primary);
					} else {
						throw new ShapeTransformException("TODO: support generic formula in target property");
					}
				} 
			}
			
		}

		

		private void attachPathSteps(DirectPropertyModel direct, PathExpression path) throws ShapeTransformException {
			
			ShapeModel shapeModel = direct.getDeclaringShape();
			StepPropertyModel priorStep = null;
			int index = -1;
			ClassModel classModel = direct.getDeclaringShape().getClassModel();
			List<PathStep> stepList = path.getStepList();
			for (PathStep step : stepList) {
				index++;
				if (step instanceof HasPathStep) {
					
					if (priorStep == null) {
						throw new ShapeTransformException("Leading filter not supported");
					}
					
					HasStep hasStep = (HasStep) step;
					priorStep.setFilter(hasStep.getPairList());
					
					// TODO:
					// In shapeModelFactory1, we inject FixedPropertyModel elements within the group.
					// Should we do that here?
					
				} else if (step instanceof DirectedStep) {
					DirectedStep directStep = (DirectedStep) step;
					URI predicate = directStep.getTerm().getIri();
					PropertyGroup group = classModel.produceGroup(directStep.getDirection(), predicate);
					
					StepPropertyModel stepModel = new StepPropertyModel(predicate, directStep.getDirection(), group, direct, index);
					if (priorStep != null) {
						priorStep.setNextStep(stepModel);
					}
					if (logger.isDebugEnabled()) {
						logger.debug("attachPathSteps: Add step {}{} to group[{}] in ClassModel[{}]",
							directStep.getDirection().getSymbol(),
								predicate.getLocalName(), group.hashCode(), classModel.hashCode());
					}
					attachProperty(stepModel, shapeModel, group);
					
					
					priorStep = stepModel;
				} else {
					throw new ShapeTransformException("Unsupported PathStep type: " + step.getClass().getName());
				}
				
				if (hasNextStep(stepList, index)) {
					// TODO: Find a way to define the OWL class.
					URI owlClass = null;
					classModel = new ClassModel(owlClass);
					priorStep.setValueClassModel(classModel);
				}
			}
			
			if (priorStep == null) {
				throw new ShapeTransformException("Path tail not found for property " + direct.getPredicate());
			}
				
			if (logger.isDebugEnabled()) {
				logger.debug("attachPathSteps: Set '{}' as path tail of '{}'", 
						priorStep.getPredicate().getLocalName(), 
						direct.getPredicate().getLocalName());
			}
			direct.setPathTail(priorStep);
			priorStep.getGroup().setTargetProperty(priorStep);
				
			
		}



		private boolean hasNextStep(List<PathStep> stepList, int start) {
			for (int i=start; i<stepList.size(); i++) {
				PathStep step = stepList.get(i);
				if (step instanceof DirectedStep) {
					return true;
				}
			}
			return false;
		}

		private DirectPropertyModel directPropertyModel(ShapeModel shapeModel, URI predicate, PropertyGroup group,
				PropertyConstraint p) throws ShapeTransformException {
			DirectPropertyModel direct = new DirectPropertyModel(predicate, group, p);
			fullAttachProperty(direct, shapeModel, group);
			shapeModel.add(direct);
			Shape childShape = p.getShape();
			if (childShape != null) {
				ShapeModel child = targetShapeModel(childShape, childShape.getTargetClass());
				child.setAccessor(direct);
				direct.getGroup().setValueClassModel(child.getClassModel());
				direct.setValueModel(child);
			}
			return direct;
		}
		
		private void fullAttachProperty(PropertyModel p, ShapeModel shapeModel, PropertyGroup group) {
			attachProperty(p, shapeModel, group);
			shapeModel.add(p);
			if (shapeModel.getClassModel().getTargetShapeModel() == shapeModel) {
				group.setTargetProperty(p);
			}
		}

		private void attachProperty(PropertyModel p, ShapeModel shapeModel, PropertyGroup group) {

			p.setDeclaringShape(shapeModel);
			group.add(p);
		}

		private class MatchMaker implements MatchVisitor {

			@Override
			public void match(PropertyModel sourceProperty, DirectPropertyModel targetProperty) throws ShapeTransformException {

				ShapeModel sourceShapeModel = sourceProperty.getDeclaringShape();
				if (sourceShapeModel.getSourceShapeInfo().isExcluded()) {
					return;
				}
			
				targetProperty.getGroup().setSourceProperty(sourceProperty);
				
				if (sourceShapeModel.getDataChannel() == null) {
					DataChannel channel = dataChannelFactory.createDataChannel(sourceShapeModel.getShape());
					if (channel == null) {
						sourceShapeModel.getSourceShapeInfo().setStatus(SourceShapeStatus.EXCLUDED);
						logger.debug("MatchMaker.match: {} has been excluded as a candidate Shape because no DataChannel exists", RdfUtil.localName(sourceShapeModel.getShape().getId()));
						return;
					}
					channel = filteredChannel(channel, sourceProperty, targetProperty);
					sourceShapeModel.setDataChannel(channel);
					logger.debug("MatchMaker.match: Set DataChannel for {}", RdfUtil.localName(sourceShapeModel.getShape().getId()));
				}
				SourceShapeInfo info = sourceShapeModel.getSourceShapeInfo();
				if (info.getStatus() == SourceShapeStatus.CANDIDATE) {
					ClassModel targetClassModel = targetProperty.getDeclaringShape().getClassModel();
					targetClassModel.addCommittedSource(info);
					info.setStatus(SourceShapeStatus.COMMITTED);
					
				}
				
				// Store the property match.  This will be used later when building join conditions.
				info.addMatchedTargetProperty(targetProperty);
				
				if (logger.isDebugEnabled()) {
					logMatch(sourceProperty, targetProperty);
					
				}
				
			}


			






			private void logMatch(PropertyModel sourceProperty, PropertyModel targetProperty) {

				logger.debug("MatchMaker.match: Mapped {}{} from {} to {} in {}",
					directionSymbol(sourceProperty),
					sourceProperty.getPredicate().getLocalName(),
					RdfUtil.localName(sourceProperty.getDeclaringShape().getShape().getId()),
					targetProperty.getPredicate().getLocalName(),
					RdfUtil.localName(targetProperty.getDeclaringShape().getShape().getId())
					);
				
			}

			@Override
			public void noMatch(DirectPropertyModel sourceProperty) {
				// Do nothing
			}

			@Override
			public void handleValueModel(ShapeModel sourceShapeModel) throws ShapeTransformException {
				sourceShapeModel.getSourceShapeInfo().dispatch(this);
			}

			@Override
			public void matchId(IdPropertyModel sourceProperty, IdPropertyModel targetProperty)
					throws ShapeTransformException {
				
				targetProperty.getGroup().setSourceProperty(sourceProperty);
				logMatch(sourceProperty, targetProperty);
				
			}
			

			
		}
		
	}
	
	
	
	
	

	private String directionSymbol(PropertyModel property) {
		if (property instanceof StepPropertyModel) {
			StepPropertyModel step = (StepPropertyModel) property;
			return Character.toString(step.getDirection().getSymbol());
		}
		return "";
	}


	private DataChannel filteredChannel(DataChannel channel, PropertyModel sourceProperty,
			PropertyModel targetProperty) {
		if (sourceProperty instanceof StepPropertyModel) {
			StepPropertyModel sourceStep = (StepPropertyModel) sourceProperty;
			StepPropertyModel previous = sourceStep.getPreviousStep();
			if (previous != null) {
				if (previous.getDirection() == Direction.IN) {
					URI predicate = previous.getPredicate();
					// TODO: Finish this implementation
				}
			}
		}
		return channel;
	}

}
