package io.konig.core.showl;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.XMLSchema;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class BasicTransformService implements ShowlTransformService {

	private static final Logger logger = LoggerFactory.getLogger(BasicTransformService.class);

	private ShowlSourceNodeFactory sourceNodeFactory;
	private ShowlSchemaService schemaService;
	private ShowlNodeShapeService nodeService;

	public BasicTransformService(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService,
			ShowlSourceNodeFactory sourceNodeFactory) {
		this.schemaService = schemaService;
		this.nodeService = nodeService;
		this.sourceNodeFactory = sourceNodeFactory;
	}

	static class State {
		ShowlNodeShape targetNode;
		Set<ShowlPropertyShapeGroup> propertyPool;
		Set<ShowlNodeShape> candidateSet;
		Set<Object> memory = new HashSet<>();
		
		Map<ShowlPropertyShape,ShowlNodeShape> enumMap;
		private ShowlPropertyShapeGroup targetModified;
		private ShowlNodeShapeService nodeService;

		public State(
				ShowlNodeShape targetNode, 
				Set<ShowlPropertyShapeGroup> propertyPool,
				Set<ShowlNodeShape> candidateSet,
				ShowlNodeShapeService nodeService,
				Map<ShowlPropertyShape,ShowlNodeShape> enumMap
		) {
			this.propertyPool = propertyPool;
			this.candidateSet = candidateSet;
			this.targetNode = targetNode;
			this.nodeService = nodeService;
			this.enumMap = enumMap;
		}
		
		

		public ShowlPropertyShapeGroup getTargetModified() {
			return targetModified;
		}



		public void setTargetModified(ShowlPropertyShapeGroup targetModified) {
			this.targetModified = targetModified;
		}



		public boolean done() {
			return propertyPool.isEmpty() || candidateSet.isEmpty();
		}
		
		public ShowlNodeShape enumNode(ShowlNodeShape targetNode) {
			ShowlPropertyShape targetProperty = targetNode.getAccessor();
			ShowlNodeShape enumNode = enumMap.get(targetProperty);
			if (enumNode == null) {
				ShowlClass enumClass = targetProperty.getValueShape().getOwlClass();
				Shape enumShape = nodeService.enumNodeShape(enumClass);
				enumNode = nodeService.createShowlNodeShape(null, enumShape, enumClass);
				enumMap.put(targetProperty, enumNode);
				enumNode.setTargetNode(targetProperty.getValueShape());
				enumNode.setTargetProperty(targetProperty);
			}
			if (logger.isTraceEnabled()) {
				logger.trace("State.enumNode({})", targetNode.getPath());
			}
			return enumNode;
		}

	}

	
	protected OwlReasoner owlReasoner() {
		return schemaService.getOwlReasoner();
	}

	public static Logger getLogger() {
		return logger;
	}

	public ShowlSourceNodeFactory getSourceNodeFactory() {
		return sourceNodeFactory;
	}

	public ShowlSchemaService getSchemaService() {
		return schemaService;
	}

	public void setSourceNodeFactory(ShowlSourceNodeFactory sourceNodeFactory) {
		this.sourceNodeFactory = sourceNodeFactory;
	}

	public ShowlNodeShapeService getNodeService() {
		return nodeService;
	}
	

	protected Set<ShowlPropertyShapeGroup> basicComputeTransform(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		Set<ShowlNodeShape> candidates = sourceNodeFactory.candidateSourceNodes(targetNode);

		
//		State state = new State(targetNode, propertyPool(targetNode), candidates, nodeService);

		State state = createState(targetNode, propertyPool(targetNode), candidates, nodeService);

		state.memory.add(targetNode);

		if (state.propertyPool.isEmpty()) {
			throw new ShowlProcessingException("No properties found in target node " + targetNode.getPath());
		}

		if (state.candidateSet.isEmpty()) {
			logger.warn("Failed to transform {}.  No candidate source shapes were found.", targetNode.getPath());
		} else {

			while (!state.done()) {

				ShowlNodeShape sourceShape = nextSource(state);
				if (sourceShape == null) {
					break;
				}
				if (!addChannel(state, sourceShape, targetNode)) {
					break;
				}

				computeMapping(sourceShape, state);
				mapEnumProperties(state);
				removeWellDefinedNodes(state);
				addCandidateSource(state);
			}

		}

		handleModifiedProperty(state);
		handleTargetFormulas(state);
		handleTeleportFormulas(state);
		setTargetProperties(state.targetNode);

		return state.propertyPool;
	}

	protected State createState(ShowlNodeShape targetNode, Set<ShowlPropertyShapeGroup> propertyPool,
			Set<ShowlNodeShape> candidates, ShowlNodeShapeService nodeService) {
		return  new State(targetNode, propertyPool, candidates, nodeService, enumMap());
	}
	
	protected Map<ShowlPropertyShape,ShowlNodeShape> enumMap() {
		return new HashMap<>();
	}

	private void setTargetProperties(ShowlNodeShape targetNode) {
		
		for (ShowlChannel channel : targetNode.getChannels()) {
			ShowlNodeShape sourceNode = channel.getSourceNode();
			setTargetProperties(sourceNode, sourceNode.getTargetNode().effectiveNode());
		}
	}

	private void setTargetProperties(ShowlNodeShape sourceNode, ShowlEffectiveNodeShape targetNode) {
		
		setTargetProperties(sourceNode.getProperties(), targetNode);
		for (ShowlDerivedPropertyList list : sourceNode.getDerivedProperties()) {
			setTargetProperties(list, targetNode);
		}
	}

	private void setTargetProperties(Collection<? extends ShowlPropertyShape> sourceProperties,	ShowlEffectiveNodeShape targetNode) {
		
		for (ShowlPropertyShape p : sourceProperties) {
			ShowlPropertyShapeGroup targetGroup = targetNode.findPropertyByPredicate(p.getPredicate());
			if (targetGroup != null) {
				setTargetProperty(p, targetGroup);
				for (ShowlPropertyShape q : p.synonyms()) {
					setTargetProperty(q, targetGroup);
				}
			}
		}
		
	}

	private void setTargetProperty(ShowlPropertyShape p, ShowlPropertyShapeGroup targetGroup) {

		p.setTargetProperty(targetGroup);
		if (p.getValueShape()!=null && targetGroup.getValueShape()!=null) {
			setTargetProperties(p.getValueShape(), targetGroup.getValueShape());
		}
		
	}

	@Override
	public Set<ShowlPropertyShapeGroup> computeTransform(ShowlNodeShape targetNode) throws ShowlProcessingException {
		
		Set<ShowlNodeShape> candidates = sourceNodeFactory.candidateSourceNodes(targetNode);
		if (candidates.size()>1) {
			OverlayTransformService overlayTransform = new OverlayTransformService(schemaService, nodeService, sourceNodeFactory, candidates);
			return overlayTransform.computeTransform(targetNode);
		}

		return basicComputeTransform(targetNode);
	}

	private void handleTeleportFormulas(State state) {

		Iterator<ShowlPropertyShapeGroup> sequence = state.propertyPool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShapeGroup group = sequence.next();
			ShowlDirectPropertyShape direct = group.direct();
			if (direct != null) {
				ShowlTeleportExpression teleport = teleport(group);
				if (teleport != null) {
					ShowlExpression delegate = teleport.getDelegate();
					ShowlNodeShape focusNode = teleport.getFocusNode();

					Set<ShowlPropertyShape> set = new HashSet<>();
					delegate.addProperties(set);

					int count = 0;

					loop2: for (ShowlPropertyShape p : set) {
						if (p.getSelectedExpression() != null) {
							count++;
						} else {
							for (ShowlChannel channel : state.targetNode.getChannels()) {
								ShowlNodeShape sourceNode = channel.getSourceNode();
								ShowlExpression e = findMatch(sourceNode, p);
								if (e != null) {
									p.setSelectedExpression(e);
									count++;
									continue loop2;
								}
							}
						}

					}

					if (count == set.size()) {
						direct.setSelectedExpression(delegate.transform());
						sequence.remove();
					}
				}

			}
		}

	}

	private ShowlTeleportExpression teleport(ShowlPropertyShapeGroup group) {
		for (ShowlPropertyShape p : group) {
			ShowlExpression formula = p.getFormula();
			if (formula instanceof ShowlTeleportExpression) {
				return (ShowlTeleportExpression) formula;
			}
		}
		return null;
	}

	private void handleTargetFormulas(State state) {

		if (!state.propertyPool.isEmpty()) {
			Iterator<ShowlPropertyShapeGroup> sequence = state.propertyPool.iterator();
			while (sequence.hasNext()) {
				ShowlPropertyShapeGroup group = sequence.next();
				ShowlDirectPropertyShape direct = group.direct();
				if (direct != null) {
					ShowlExpression formula = direct.getFormula();
					if (formula != null) {
						
						// The direct property declares a formula.
						// Check whether the formula is satisfied, i.e. check
						// that all the properties the formula needs are well-defined.
						
						// We start by collecting the set of properties referenced
						
						Set<ShowlPropertyShape> set = new HashSet<>();
						formula.addProperties(set);
						
						// Now that we have the set of properties, let's count the number of those
						// properties that are well-defined.  A property is well-defined if it 
						// has a mapping.

						int count = 0;
						for (ShowlPropertyShape p : set) {
							ShowlExpression e = p.getSelectedExpression();
							if (e == null) {
								if (!p.isDirect()) {
									ShowlDirectPropertyShape pDirect = p.getDeclaringShape().getProperty(p.getPredicate());
									if (pDirect != null) {
										e = pDirect.getSelectedExpression();
										if (e != null) {
											p.setSelectedExpression(e);
										}
									}
								}
							}
							if (e != null) {
								count++;
							}
						}
						if (count == set.size()) {
							ShowlExpression e = formula.transform();
							direct.setSelectedExpression(e);
							sequence.remove();

						}
					}
				}
			}
		}

	}

	private void mapEnumProperties(State state) {

		for (ShowlPropertyShapeGroup group : state.propertyPool) {
			ShowlDirectPropertyShape direct = group.direct();

			if (direct == null || direct.getSelectedExpression() != null) {
				continue;
			}
			ShowlNodeShape enumTargetNode = ShowlUtil.containingEnumNode(direct, schemaService.getOwlReasoner());
			if (enumTargetNode != null) {
				ShowlDirectPropertyShape uniqueKey = uniqueKey(enumTargetNode);
				if (uniqueKey != null) {
					ShowlExpression sourceKeyExpression = uniqueKey.getSelectedExpression();

					if (sourceKeyExpression != null) {

						ShowlNodeShape enumSourceNode = state.enumNode(enumTargetNode);
						ShowlEnumNodeExpression enumNodeExpression = new ShowlEnumNodeExpression(enumSourceNode);
						enumTargetNode.getAccessor().setSelectedExpression(enumNodeExpression);

						ShowlPropertyShape sourceKey = enumSourceNode.getProperty(uniqueKey.getPredicate());
						if (sourceKey == null) {
							throw new ShowlProcessingException("Unique key '" + uniqueKey.getPredicate().getLocalName()
									+ "' not found in for " + enumTargetNode.getPath());
						}
						computeEnumMapping(state, enumSourceNode, enumTargetNode);

						ShowlEqualStatement statement = new ShowlEqualStatement(expression(sourceKey), sourceKeyExpression);
						ShowlChannel channel = new ShowlChannel(enumSourceNode, statement);
						state.targetNode.addChannel(channel);

						enumNodeExpression.setChannel(channel);
						enumNodeExpression.setJoinStatement(statement);
					}
				}
			}
		}

	}

	private void computeEnumMapping(State state, ShowlNodeShape enumSourceNode, ShowlNodeShape enumTargetNode) {

		for (ShowlPropertyShape targetProperty : enumTargetNode.getProperties()) {
			ShowlPropertyShape sourceProperty = enumSourceNode.getProperty(targetProperty.getPredicate());
			if (sourceProperty == null) {
				continue;
			}

			if (targetProperty.getValueShape() != null) {
				if (sourceProperty.getValueShape() != null) {
					if (targetProperty.getSelectedExpression() == null) {
//						targetProperty.setSelectedExpression(new ShowlEnumNodeExpression(sourceProperty.getValueShape()));
						fail("EnumNodeExpression not found for {0}", targetProperty.getPath());
					}
					computeEnumMapping(state, sourceProperty.getValueShape(), targetProperty.getValueShape());
				}
			} else  {
				if (targetProperty.getSelectedExpression() == null) {
					if (logger.isTraceEnabled()) {
						logger.trace("Replacing expression for {} with enum property expression", targetProperty.getPath());
					}
				}
				targetProperty.setSelectedExpression(new ShowlEnumPropertyExpression(sourceProperty));
			}
		}

	}

//	private ShowlNodeShape enumNode(ShowlClass enumClass) {
//		Shape enumShape = nodeService.enumNodeShape(enumClass);
//		return nodeService.createShowlNodeShape(null, enumShape, enumClass);
//	}
//
//	private ShowlNodeShape enumSourceNode(ShowlNodeShape enumTargetNode) {
//		// First check to see if we have already created an enum source node
//		ShowlExpression e = enumTargetNode.getAccessor().getSelectedExpression();
//		if (e instanceof HasEnumNode) {
//			return ((HasEnumNode) e).getEnumNode();
//		}
//		for (ShowlPropertyShape p : enumTargetNode.getProperties()) {
//			if (p instanceof HasEnumNode) {
//				return ((HasEnumNode) p).getEnumNode();
//			}
//		}
//
//		ShowlNodeShape enumSourceNode = enumNode(enumTargetNode.getOwlClass());
//
//		enumSourceNode.setTargetNode(enumTargetNode);
//		enumSourceNode.setTargetProperty(enumTargetNode.getAccessor());
//
//		return enumSourceNode;
//	}

	private ShowlDirectPropertyShape uniqueKey(ShowlNodeShape enumNode) {
		for (ShowlDirectPropertyShape p : enumNode.getProperties()) {
			if (p.getSelectedExpression() != null && ShowlUtil.isUniqueKey(p, schemaService.getOwlReasoner())) {
				return p;
			}
		}

		return null;
	}

	private void removeWellDefinedNodes(State state) {

		Iterator<ShowlPropertyShapeGroup> sequence = state.propertyPool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShapeGroup group = sequence.next();
			if (group.getValueShape() != null) {
				ShowlDirectPropertyShape direct = group.direct();
				if (direct != null && direct.getValueShape() != null && ShowlUtil.isWellDefined(direct.getValueShape())) {

					// direct.setSelectedExpression(new ShowlStructExpression(direct));
					if (logger.isTraceEnabled()) {
						logger.trace("Nested record is well-defined: {}", direct.getPath());
					}
					sequence.remove();
				}
			}
		}

	}

	/**
	 * If the candidate set is empty, try to add a new Candidate source
	 * 
	 * @param state
	 */
	private void addCandidateSource(State state) throws ShowlProcessingException {
		Set<ShowlPropertyShapeGroup> propertyPool = state.propertyPool;
		if (!propertyPool.isEmpty() && state.candidateSet.isEmpty()) {
			Iterator<ShowlPropertyShapeGroup> sequence = propertyPool.iterator();
			while (sequence.hasNext()) {
				ShowlPropertyShapeGroup targetGroup = sequence.next();
				ShowlDirectPropertyShape targetDirect = targetGroup.direct();
				if (targetDirect == null) {
					throw new ShowlProcessingException("Direct property missing for " + targetGroup.pathString()
							+ ", perhaps because derived properties are not yet supported.");
				}
				if (targetDirect.getSelectedExpression() != null) {
					if (logger.isTraceEnabled()) {
						logger.trace("addCandidateSource: Removing property from pool because it was mapped out of sequence: {}",
								targetDirect.getPath());
					}
					sequence.remove();
					continue;
				}

				ShowlEffectiveNodeShape parentNode = targetGroup.getDeclaringShape();
				ShowlClass targetClass = parentNode.getTargetClass();
				if (!state.memory.contains(targetGroup)) {
					state.memory.add(targetGroup);
					if (isEnumClass(targetClass)) {
						if (acceptEnumClass(targetGroup)) {
							ShowlNodeShape enumNode = state.enumNode(parentNode.canonicalNode());

							enumNode.setTargetNode(targetGroup.iterator().next().getDeclaringShape());
							state.candidateSet.add(enumNode);
							return;
						}

					} else {

						ShowlNodeShape targetNodeShape = parentNode.directNode();
						if (!state.memory.contains(targetNodeShape)) {
							state.memory.add(targetNodeShape);
							Set<ShowlNodeShape> candidates = sourceNodeFactory.candidateSourceNodes(targetNodeShape);
							if (!candidates.isEmpty()) {
								state.candidateSet.addAll(candidates);

								if (logger.isTraceEnabled()) {
									logger.trace("addCandidateSource: For {}, adding candidates...", targetGroup.pathString());
									for (ShowlNodeShape c : candidates) {
										logger.trace("    " + c.getPath());
									}
								}
								return;
							}
						}
					}
				}
			}
		}

	}

	protected boolean acceptEnumClass(ShowlPropertyShapeGroup targetGroup) {
		return true;
	}


	private boolean isEnumClass(ShowlClass targetClass) {
		return targetClass == null ? false : schemaService.getOwlReasoner().isEnumerationClass(targetClass.getId());
	}

	private boolean addChannel(State state, ShowlNodeShape sourceShape, ShowlNodeShape targetRoot) {

		if (targetRoot.getChannels().isEmpty()) {
			targetRoot.addChannel(new ShowlChannel(sourceShape, null));

			if (logger.isTraceEnabled()) {
				logger.trace("addChannel({})", sourceShape.getPath());
			}

			return true;
		} else if (isEnumClass(sourceShape.getOwlClass())) {
			ShowlChannel priorChannel = targetRoot.findChannelFor(sourceShape);
			ShowlStatement join = null;
			if (requiresEnumJoin(priorChannel)) {

				join = enumJoinStatement(state, sourceShape);
				if (join == null) {
					return false;
				}
			}
			
			if (priorChannel!=null) {
				if (join!=null) {
					ShowlStatement priorStatement = priorChannel.getJoinStatement();
					if (priorStatement==null) {
						priorChannel.setJoinStatement(join);
					} else {
						Set<ShowlPropertyShape> set = new HashSet<>();
						priorStatement.addDeclaredProperties(sourceShape, set);
						if (set.isEmpty()) {
							if (priorStatement instanceof ShowlOverlayExpression) {
								ShowlOverlayExpression overlay = (ShowlOverlayExpression)priorStatement;
								overlay.add(join);
							} else {
								ShowlOverlayExpression overlay = new ShowlOverlayExpression();
								overlay.add(priorStatement);
								overlay.add(join);
								priorChannel.setJoinStatement(overlay);
							}
						}
					}
				}
				return true;
			}
			ShowlChannel channel = new ShowlChannel(sourceShape, join);

			// The following if statement is ugly. Probably ought to have
			// enumJoinStatement method create the ShowlChannel and
			// perform this work.

			if (sourceShape.getTargetNode() != null) {
				ShowlNodeShape targetNode = sourceShape.getTargetNode();
				ShowlPropertyShape targetAccessor = targetNode.getAccessor();
				
				if (targetAccessor != null && targetAccessor.getSelectedExpression()==null) {

					ShowlNodeShape enode = state.enumNode(targetNode);
					ShowlEnumNodeExpression accessorExpression = new ShowlEnumNodeExpression(enode, channel);
					targetAccessor.setSelectedExpression(accessorExpression);
				}
			
			}
			targetRoot.addChannel(channel);

			if (logger.isTraceEnabled()) {
				String joinString = join == null ? "null" : join.toString();
				logger.trace("addChannel({}, {})", sourceShape.getPath(), joinString);
			}

			return true;
		} else if (channelExists(sourceShape, targetRoot)) {
			return true;
		} else {
			ShowlNodeShape targetNode = sourceShape.getTargetNode();
			ShowlPropertyShape targetAccessor = targetNode.getAccessor();
			if (targetAccessor == null) {
				// Top-level source shape.

				if (joinById(sourceShape)) {
					return true;
				}

			} else {

				ShowlPropertyShape targetId = targetAccessor.getDeclaringShape().findOut(Konig.id);
				URI targetAccessorPredicate = targetAccessor.getPredicate();

				if (targetId != null) {
					if (targetId.getSelectedExpression() != null) {
						ShowlExpression leftJoinExpression = targetId.getSelectedExpression();

						// Look for owl:inverseOf attribute of accessor, and see if the
						// sourceShape contains this value

						Set<URI> inverseSet = schemaService.getOwlReasoner().inverseOf(targetAccessorPredicate);

						for (URI inverseProperty : inverseSet) {
							ShowlPropertyShape sourceJoinProperty = sourceShape.findOut(inverseProperty);
							if (sourceJoinProperty != null) {
								ShowlEqualStatement join = new ShowlEqualStatement(leftJoinExpression,
										ShowlUtil.propertyExpression(sourceJoinProperty));
								targetRoot.addChannel(new ShowlChannel(sourceShape, join));
								return true;
							}
						}

						// Look for inverse path in sourceShape

						for (ShowlInwardPropertyShape inwardProperty : sourceShape.getInwardProperties()) {
							URI inwardPredicate = inwardProperty.getPredicate();
							ShowlPropertyShape inwardDirect = inwardProperty.getSynonym();
							if (inwardDirect instanceof ShowlDirectPropertyShape && targetAccessorPredicate.equals(inwardPredicate)) {
								ShowlEqualStatement join = new ShowlEqualStatement(leftJoinExpression,
										ShowlUtil.propertyExpression(inwardDirect));
								targetRoot.addChannel(new ShowlChannel(sourceShape, join));
								return true;

							}
						}
					}

				}
			}
		}
		
		if (joinByUniqueKey(sourceShape)) {
			return true;
		}

		if (logger.isWarnEnabled()) {

			logger.warn("Failed to create channel for {} in transform of {}", sourceShape.getPath(),
					sourceShape.getTargetNode().getPath());
		}
		return false;
	}

	private boolean requiresEnumJoin(ShowlChannel priorChannel) {
		 
		if (priorChannel==null) {
			return true;
		}

		ShowlStatement priorStatement = priorChannel.getJoinStatement();
		if (priorStatement==null) {
			return true;
		}
		ShowlNodeShape sourceShape = priorChannel.getSourceNode();
		Set<ShowlPropertyShape> set = new HashSet<>();
		priorStatement.addDeclaredProperties(sourceShape, set);
		return set.isEmpty();
	}

	private boolean joinByUniqueKey(ShowlNodeShape sourceNode) {
		ShowlNodeShape targetNode = sourceNode.getTargetNode();

		UniqueKeyFactory factory = new UniqueKeyFactory(schemaService.getOwlReasoner());
		ShowlUniqueKeyCollection keyCollection = keyCollection(factory, sourceNode);
		
		UniqueKeySelector selector = new UniqueKeySelector(schemaService.getOwlReasoner());
		
		for (ShowlChannel channel : targetNode.getChannels()) {
			ShowlNodeShape otherSourceNode = channel.getSourceNode();
			ShowlUniqueKeyCollection otherKeyCollection = keyCollection(factory, otherSourceNode);
			List<ShowlUniqueKeyCollection> list = new ArrayList<>();
			list.add(keyCollection);
			list.add(otherKeyCollection);
			Map<ShowlNodeShape, ShowlUniqueKey> map = selector.selectBestKey(list);
			if (!map.isEmpty()) {
				ShowlUniqueKey key = map.get(sourceNode);
				ShowlUniqueKey otherKey = map.get(otherSourceNode);
				ShowlStatement join = joinStatement(key, otherKey);

				ShowlNodeShape targetRoot = targetNode.getRoot();
				targetRoot.addChannel(new ShowlChannel(sourceNode, join));
				return true;
			}
			
		}
		return false;
	}

	private ShowlStatement joinStatement(ShowlUniqueKey leftKey, ShowlUniqueKey rightKey) {
		
		List<UniqueKeyElement> leftList = leftKey.flatten();
		List<UniqueKeyElement> rightList = rightKey.flatten();
		if (leftList.size() == 1) {
			return equalStatement(leftList.get(0), rightList.get(0));
		}
		
		List<ShowlExpression> operands = new ArrayList<>();
		for (int i=0; i<leftList.size(); i++) {
			operands.add(equalStatement(leftList.get(i), rightList.get(i)));
		}
		
		return new ShowlAndExpression(operands);
	}

	private ShowlEqualStatement equalStatement(UniqueKeyElement leftElement, UniqueKeyElement rightElement) {
		return new ShowlEqualStatement(
				expression(leftElement.getPropertyShape()), 
				expression(rightElement.getPropertyShape()));
	}

	private ShowlUniqueKeyCollection keyCollection(UniqueKeyFactory factory, ShowlNodeShape sourceNode) {
		ShowlUniqueKeyCollection key = sourceNode.getUniqueKeyCollection();
		if (key == null) {
			key = factory.createKeyCollection(sourceNode);
			sourceNode.setUniqueKeyCollection(key);
		}
		return key;
	}

	private boolean channelExists(ShowlNodeShape sourceShape, ShowlNodeShape targetRoot) {
		for (ShowlChannel channel : targetRoot.getChannels()) {
			if (channel.getSourceNode() == sourceShape) {
				return true;
			}
		}
		return false;
	}

	private boolean joinById(ShowlNodeShape sourceShape) throws ShowlProcessingException {

		ShowlPropertyShape leftId = sourceShape.findOut(Konig.id);

		if (leftId != null) {
			ShowlExpression leftExpression = expression(leftId);
			ShowlNodeShape targetRoot = sourceShape.getTargetNode().getRoot();
			for (ShowlChannel leftChannel : targetRoot.getChannels()) {
				ShowlNodeShape leftSource = leftChannel.getSourceNode();
				ShowlPropertyShape rightId = leftSource.findOut(Konig.id);
				if (rightId != null) {
					ShowlExpression rightExpression = expression(rightId);
					ShowlEqualStatement equals = new ShowlEqualStatement(leftExpression, rightExpression);
					ShowlChannel channel = new ShowlChannel(sourceShape, equals);
					targetRoot.addChannel(channel);

					if (logger.isTraceEnabled()) {
						logger.trace("joinById: addChannel({}, {})", sourceShape.getPath(), equals.toString());
					}
					return true;
				}

			}
		}
		return false;
	}

	private ShowlExpression expression(ShowlPropertyShape p) throws ShowlProcessingException {
		if (p instanceof ShowlDirectPropertyShape) {
			return new ShowlDirectPropertyExpression((ShowlDirectPropertyShape) p);
		}
		if (p.getFormula() != null) {
			return p.getFormula();
		}

		throw new ShowlProcessingException("Failed to get expression for " + p.getPath());
	}

	protected ShowlStatement enumJoinStatement(State state, ShowlNodeShape sourceShape) throws ShowlProcessingException {

		ShowlNodeShape root = sourceShape.getTargetNode().getRoot();

		ShowlEffectiveNodeShape enumNode = sourceShape.effectiveNode();

		ShowlDirectPropertyShape enumId = sourceShape.getProperty(Konig.id);

		OwlReasoner reasoner = schemaService.getOwlReasoner();
		ShowlNodeShape targetNode = sourceShape.getTargetNode();

		ShowlPropertyShape targetAccessor = targetNode.getAccessor();

		// Consider the case where the Source shape contains an IRI reference to the
		// enum member.
		//
		// For instance, suppose we have the following mapping:
		//
		// {TargetPersonShape}.gender ... {SourcePersonShape}.gender_id
		//
		// In this case, targetAccessor.selectedExpression will be a
		// ShowlDirectPropertyExpression that
		// wraps {SourcePersonShape}.gender_id

		if (targetAccessor.getSelectedExpression() instanceof ShowlDirectPropertyExpression) {
			return new ShowlEqualStatement(new ShowlDirectPropertyExpression(enumId), targetAccessor.getSelectedExpression());
		}

		// Map the enum accessor

		

		if (targetAccessor.getFormula() instanceof ShowlEnumIndividualReference
				|| targetAccessor.getFormula() instanceof ShowlIriReferenceExpression) {

			// Special handling for hard-coded enum value

			return new ShowlEqualStatement(new ShowlDirectPropertyExpression(enumId), targetAccessor.getFormula());
		}

		for (ShowlChannel channel : root.getChannels()) {

			ShowlNodeShape channelSourceNode = channel.getSourceNode();

			ShowlPropertyShapeGroup channelJoinAccessor = findPeer(channelSourceNode, targetNode);
			if (channelJoinAccessor != null) {

				ShowlStatement result = enumFilter(targetNode, sourceShape, channelJoinAccessor);
				if (result != null) {
					return result;
				}

				ShowlEffectiveNodeShape channelJoinNode = channelJoinAccessor.getValueShape();
				if (channelJoinNode == null) {

					ShowlDirectPropertyShape channelJoinAccessorDirect = channelJoinAccessor.synonymDirect();
					if (channelJoinAccessorDirect != null) {
						PropertyConstraint constraint = channelJoinAccessorDirect.getPropertyConstraint();
						if ((constraint.getNodeKind() == NodeKind.IRI || XMLSchema.STRING.equals(constraint.getDatatype()))
								&& constraint.getShape() == null) {
							// channelJoinAccessor is an IRI reference We can use it to join.

							ShowlExpression left = new ShowlEnumPropertyExpression(enumId.direct());
							ShowlExpression right = ShowlUtil.propertyExpression(channelJoinAccessorDirect);
							return new ShowlEqualStatement(left, right);

						}
					}

				}
				if (channelJoinNode != null) {

					for (ShowlPropertyShapeGroup enumProperty : enumNode.getProperties()) {

						URI predicate = enumProperty.getPredicate();
						ShowlPropertyShapeGroup channelPropertyGroup = channelJoinNode.findPropertyByPredicate(predicate);
						if (channelPropertyGroup != null) {

							ShowlPropertyShape targetProperty = targetNode.getProperty(predicate);

							if (reasoner.isInverseFunctionalProperty(predicate) || channelPropertyGroup.isUniqueKey()
									|| (targetProperty != null && targetProperty.isUniqueKey())) {

								ShowlPropertyShape channelProperty = channelPropertyGroup.direct();
								if (channelProperty == null) {
									for (ShowlPropertyShape p : channelPropertyGroup) {

										ShowlPropertyShape synonym = p.getSynonym();
										if (synonym instanceof ShowlDirectPropertyShape) {
											channelProperty = synonym;
										} else if (ShowlUtil.isWellDefined(p)) {
											channelProperty = p;
											break;
										}
									}
								}
								if (channelProperty != null) {

									// Generate the join statement

									ShowlExpression left = new ShowlEnumPropertyExpression(enumProperty.direct());
									ShowlExpression right = ShowlUtil.propertyExpression(channelProperty);
									return new ShowlEqualStatement(left, right);
								}
							}

						}
					}
				}
			}
		}

		ShowlPropertyShape targetProperty = targetNode.getAccessor();
		if (targetProperty != null) {
			ShowlExpression formula = targetProperty.getFormula();
			if (formula != null) {
				ShowlExpression left = new ShowlEnumPropertyExpression(enumId.direct());
				ShowlExpression right = formula;
				return new ShowlEqualStatement(left, right);
			}
		}

		return null;

	}

	private ShowlStatement enumFilter(ShowlNodeShape targetNode, ShowlNodeShape sourceShape,
			ShowlPropertyShapeGroup channelJoinAccessor) {

		if (hasFilter(channelJoinAccessor)) {

			ShowlPropertyShape multiValuedProperty = findMultiValuedProperty(targetNode.getAccessor());
			if (multiValuedProperty != null) {
				ShowlExpression e = multiValuedProperty.getSelectedExpression();
				if (e == null) {
					e = new ShowlArrayExpression();
					multiValuedProperty.setSelectedExpression(e);
				}
				if (e instanceof ShowlArrayExpression) {

					// TODO: build array expression
//
//					ShowlArrayExpression array = (ShowlArrayExpression) e;
//					return new ArrayFilterStatement(multiValuedProperty, targetNode);
					fail("Array of enum not supported yet");
				}

			}

			throw new ShowlProcessingException("Failed to create enum filter for " + targetNode.getPath());
		}

		return null;
	}

	private ShowlPropertyShape findMultiValuedProperty(ShowlPropertyShape p) {
		while (p != null) {
			PropertyConstraint constraint = p.getPropertyConstraint();
			if (constraint != null) {
				Integer maxCount = constraint.getMaxCount();
				if (maxCount == null || maxCount > 1) {
					return p;
				}
			}

			p = p.getDeclaringShape().getAccessor();
		}
		return null;
	}

	private boolean hasFilter(ShowlPropertyShapeGroup channelJoinAccessor) {
		for (ShowlPropertyShape p : channelJoinAccessor) {
			if (!p.getHasValue().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Find the property within a given source node that maps to the given target
	 * node.
	 * 
	 * @param sourceNode
	 * @param targetNode
	 * @return
	 */
	protected ShowlPropertyShapeGroup findPeer(ShowlNodeShape sourceNode, ShowlNodeShape targetNode) {

		List<URI> relativePath = ShowlUtil.relativePath(targetNode, sourceNode.getTargetNode());
		if (relativePath == null || relativePath.isEmpty()) {
			return null;
		}

		return sourceNode.effectiveNode().findPropertyByPredicatePath(relativePath);
	}

	// There is something fishy here. We are using targetNode to build a relative
	// path.
	// But I don't think we are passing the correct targetNode for that to work.

	private void computeMapping(ShowlNodeShape source, State state) {
		ShowlEffectiveNodeShape sourceNode = source.effectiveNode();
		ShowlEffectiveNodeShape targetNode = source.getTargetNode().effectiveNode();

		boolean isEnum = schemaService.getOwlReasoner().isEnumerationClass(sourceNode.getTargetClass().getId());

		Set<ShowlPropertyShapeGroup> propertyPool = state.propertyPool;
		Iterator<ShowlPropertyShapeGroup> sequence = propertyPool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShapeGroup targetProperty = sequence.next();

			if (mapModified(state, targetProperty)) {
				sequence.remove();
				continue;
			}

			List<ShowlPropertyShapeGroup> path = targetProperty.relativePath(targetNode);

			ShowlPropertyShapeGroup sourceProperty = sourceNode.findPropertyByPath(path);
			if (sourceProperty == null) {
				sourceProperty = findPropertyWithSynset(source, path);
			}

			if (createMapping(state, isEnum, source, sourceProperty, targetProperty)) {
				sequence.remove();
				setAccessorExpression(targetProperty);
			}
		}
	}

	private ShowlPropertyShapeGroup findPropertyWithSynset(ShowlNodeShape source,
			List<ShowlPropertyShapeGroup> path) {
		List<URI> uriPath = uriPath(path);
		SynsetNode synset = source.synsetNode();
		SynsetProperty p = synset.findPropertyByPath(uriPath);
		if (p != null) {
			ShowlPropertyShape q = p.select();
			if (q != null) {
				return q.asGroup();
			}
		}
		return null;
	}

	private List<URI> uriPath(List<ShowlPropertyShapeGroup> path) {
		List<URI> result = new ArrayList<>();
		for (ShowlPropertyShapeGroup group : path) {
			result.add(group.getPredicate());
		}
		return result;
	}

	private boolean mapModified(State state, ShowlPropertyShapeGroup targetProperty) {
		if (targetProperty.getPredicate().equals(Konig.modified)) {
			state.setTargetModified(targetProperty);
			return true;
		}
		return false;
	}

	private void setAccessorExpression(ShowlPropertyShapeGroup targetProperty) {
		if (targetProperty.getPredicate().equals(Konig.id)) {
			ShowlPropertyShape direct = targetProperty.direct();
			if (direct != null && direct.getSelectedExpression() != null) {
				ShowlPropertyShape accessor = direct.getDeclaringShape().getAccessor();
				if (accessor != null && accessor.getSelectedExpression() == null) {
					accessor.setSelectedExpression(direct.getSelectedExpression());
					if (logger.isTraceEnabled()) {
						logger.trace("setAccessorExpression: {} = {}", accessor.getPath(),
								direct.getSelectedExpression().displayValue());
					}
				}
			}
		} else if (targetProperty.getValueShape() != null) {

			ShowlEffectiveNodeShape node = targetProperty.getValueShape();
			ShowlPropertyShapeGroup group = node.findPropertyByPredicate(Konig.id);
			if (group != null) {

				ShowlDirectPropertyShape direct = group.direct();
				if (direct != null && direct.getSelectedExpression() == null) {
					ShowlDirectPropertyShape targetDirect = targetProperty.direct();
					if (targetDirect != null && targetDirect.getSelectedExpression() != null) {
						ShowlExpression s = targetDirect.getSelectedExpression();
						if (s instanceof ShowlEnumNodeExpression && direct.getPredicate().equals(Konig.id)) {
							ShowlNodeShape enumNode = ((ShowlEnumNodeExpression)s).getEnumNode();
							ShowlPropertyShape enumId = enumNode.getProperty(Konig.id);
							s = new ShowlEnumPropertyExpression(enumId);
						}
						direct.setSelectedExpression(s);
						if (logger.isTraceEnabled()) {
							logger.trace("setAccessorExpression: {} = {}", direct.getPath(),
									targetDirect.getSelectedExpression().displayValue());
						}
					}
				}
			}
		}

	}

	private boolean createMapping(
			State state,
			boolean isEnum, 
			ShowlNodeShape sourceNode, 
			ShowlPropertyShapeGroup sourceProperty,
			ShowlPropertyShapeGroup targetProperty
	) {
		ShowlDirectPropertyShape targetDirect = targetProperty.synonymDirect();
		if (targetDirect != null) {
			if (targetDirect.getSelectedExpression() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("createMapping: Mapping already established, {} = {}", targetDirect.getPath(),
							targetDirect.getSelectedExpression().displayValue());
				}
				return true;
			}
			if (sourceProperty == null) {
				return createMappingFromFormula(sourceNode, targetDirect);
			} else {

				if (alternativePathsMapping(state, isEnum, sourceProperty, targetProperty)) {
					return true;
				}

				ShowlDirectPropertyShape sourceDirect = sourceProperty.synonymDirect();

				isEnum = (isEnum) ? isEnum : schemaService.getOwlReasoner().isEnumerationClass(targetDirect.getDeclaringShape().getOwlClass().getId());
				if (isEnum) {
					if (Konig.id.equals(sourceDirect.getPredicate())) {
						ShowlPropertyShape enumAccessor = targetDirect.getDeclaringShape().getAccessor();
						if (enumAccessor != null) {
							ShowlExpression enumAccessorExpression = enumAccessor.getSelectedExpression();
							if (enumAccessorExpression != null && !(enumAccessorExpression instanceof ShowlEnumNodeExpression)) {
								fail("enum accessor is null for target property {0}", targetDirect.getPath());
							}
						}
					}
					targetDirect.setSelectedExpression(new ShowlEnumPropertyExpression(sourceDirect));
					// Make sure the accessor is using a ShowlEnumNodeExpression as the
					// mapping.

					ShowlPropertyShape accessor = targetDirect.getDeclaringShape().getAccessor();
					if (accessor != null && !(accessor.getSelectedExpression() instanceof ShowlEnumNodeExpression)) {
						ShowlNodeShape enumNode = state.enumNode(targetDirect.getDeclaringShape());
						ShowlChannel channel = produceChannel(state, enumNode);
						accessor.setSelectedExpression(new ShowlEnumNodeExpression(enumNode, channel));
					}
					return true;

				}
				
				if (sourceDirect!=null && targetDirect.isEnumIndividual(schemaService.getOwlReasoner()) && targetDirect.getValueShape()!=null) {
					
					ShowlNodeShape enumNode = state.enumNode(targetDirect.getValueShape());
					enumNode.setTargetNode(targetDirect.getValueShape());
					enumNode.setTargetProperty(targetDirect);
					
					ShowlEnumNodeExpression enumNodeExpr = new ShowlEnumNodeExpression(enumNode);
					targetDirect.setSelectedExpression(enumNodeExpr);
					ShowlPropertyShape enumId = enumNode.getProperty(Konig.id);
					
					ShowlPropertyShape sourceId = sourceDirect;
					if (sourceDirect.getValueShape() != null) {
						ShowlPropertyShape sourceIdProperty = sourceDirect.getValueShape().getProperty(Konig.id);
						if (sourceIdProperty!=null) {
							sourceId = sourceIdProperty;
						}
					}
					
					ShowlStatement joinStatement = new ShowlEqualStatement(
							ShowlUtil.propertyExpression(enumId), ShowlUtil.propertyExpression(sourceId));
					
					// TODO: eliminate the use of ShowlChannel for enum properties
					ShowlChannel channel = new ShowlChannel(enumNode, joinStatement);
					enumNodeExpr.setChannel(channel);
					enumNodeExpr.setJoinStatement(joinStatement);
					
					targetDirect.getRootNode().addChannel(channel);
					
					return true;
				}

				// If there is a direct source property, then use a direct mapping.
				if (sourceDirect != null) {
					targetDirect.setSelectedExpression(new ShowlDirectPropertyExpression(sourceDirect));
					return true;
				}

				// If there is a well-defined formula for the source property, use it.
				for (ShowlPropertyShape sourcePropertyElement : sourceProperty) {
					ShowlExpression formula = sourcePropertyElement.getFormula();
					if (ShowlUtil.isWellDefined(formula)) {
						targetDirect.setSelectedExpression(formula);
						return true;
					}
				}

				if (useClassIriTemplate(sourceProperty, targetDirect)) {
					return true;
				}

				if (useHasValue(sourceProperty, targetDirect)) {
					return true;
				}

				if (logger.isTraceEnabled() && sourceProperty.getValueShape() == null) {
					logger.trace("createMapping: Failed to create mapping: {}...{}", sourceProperty, targetProperty);
				}
			}
		}
		return false;
	}

	private ShowlChannel produceChannel(State state, ShowlNodeShape enumNode) {
		ShowlNodeShape targetRoot = enumNode.getTargetNode().getRoot();
		addChannel(state, enumNode, targetRoot);
		return targetRoot.findChannelFor(enumNode);
	}

	private boolean createMappingFromFormula(ShowlNodeShape sourceNode, ShowlDirectPropertyShape targetDirect) {
		ShowlExpression e = targetDirect.getFormula();
		if (e != null) {
			Set<ShowlPropertyShape> set = new HashSet<>();
			e.addProperties(set);

			int count = 0;
			for (ShowlPropertyShape p : set) {
				if (p.getSelectedExpression() != null || createMapping(sourceNode, p)) {
					count++;
				}
			}
			if (count == set.size()) {
				targetDirect.setSelectedExpression(e.transform());
				return true;
			}
		}
		return false;
	}

	private boolean createMapping(ShowlNodeShape sourceNode, ShowlPropertyShape targetProperty) {

		Set<ShowlPropertyShape> synonyms = targetProperty.synonyms();

		for (ShowlPropertyShape s : synonyms) {
			ShowlExpression match = findMatch(sourceNode, s);
			if (match != null) {
				targetProperty.setSelectedExpression(match);
				return true;
			}
		}

		return false;
	}

	private ShowlExpression findMatch(ShowlNodeShape sourceNode, ShowlPropertyShape targetProperty) {

		List<ShowlPropertyShape> path = targetProperty.propertyPath();
		ShowlPropertyShape joinProperty = sourceNode.getTargetProperty();
		ShowlNodeShape joinNode = joinProperty == null ? sourceNode.getTargetNode() : joinProperty.getDeclaringShape();

		for (int i = 0; i < path.size(); i++) {
			ShowlPropertyShape pathElement = path.get(i);
			ShowlPropertyShapeGroup group = null;
			if (pathElement.getDeclaringShape() == joinNode) {
				ShowlEffectiveNodeShape node = sourceNode.effectiveNode();
				for (int j = i; j < path.size(); j++) {
					if (node == null) {
						return null;
					}
					URI predicate = path.get(j).getPredicate();
					group = node.findPropertyByPredicate(predicate);
					if (group == null) {
						return null;
					}
					node = group.getValueShape();
				}

				if (group != null) {
					// For now, we only match direct properties.
					// We might want to consider other options going forward.
					ShowlDirectPropertyShape direct = group.direct();
					if (direct != null) {
						return new ShowlDirectPropertyExpression(direct);
					}
				}
			}

		}

		return null;
	}

	private boolean arrayMapping(
			State state,
			ShowlPropertyShapeGroup sourcePropertyGroup,
			ShowlPropertyShapeGroup targetPropertyGroup
	) {
		ShowlDirectPropertyShape targetDirect = targetPropertyGroup.direct();
		PropertyConstraint constraint = targetDirect.getPropertyConstraint();
		if (constraint != null) {
			Integer maxCount = constraint.getMaxCount();
			if (maxCount == null || maxCount > 1) {
				// Multi-valued property detected

				ShowlArrayExpression array = new ShowlArrayExpression();

				addMembers(state, array, targetDirect, sourcePropertyGroup);

				targetDirect.setSelectedExpression(array);
				return true;
			}
		}

		return false;
	}

	private void addMembers(
			State state,
			ShowlListExpression array, 
			ShowlDirectPropertyShape targetDirect,
			ShowlPropertyShapeGroup sourcePropertyGroup
	) {

		if (targetDirect.getValueShape() != null) {

			int memberIndex = 0;
			for (ShowlPropertyShape sourceProperty : sourcePropertyGroup) {
				if (logger.isTraceEnabled()) {
					logger.trace("addMembers: Building {}[{}]", sourcePropertyGroup.pathString(), memberIndex++);
				}
				ShowlStructExpression struct = new ShowlBasicStructExpression(targetDirect);
				addStructProperties(state, struct, targetDirect, sourceProperty);
				array.addMember(struct);

			}

		} else {
			// TODO: Support array of IRI reference and array of datatype values
			throw new ShowlProcessingException(
					"Array of IRI reference or datatype value not supported yet: " + targetDirect.getPath());
		}

	}

	private void addStructProperties(
			State state,
			ShowlStructExpression struct, 
			ShowlDirectPropertyShape targetDirect,
			ShowlPropertyShape sourceProperty
	) {

	
		SynsetProperty synSourceProp = sourceProperty.asSynsetProperty();
		SynsetNode sourceSynValue = synSourceProp.getValueNode();
		
		if (sourceSynValue != null) {

			for (ShowlDirectPropertyShape targetField : targetDirect.getValueShape().getProperties()) {
				URI predicate = targetField.getPredicate();
				
				SynsetProperty s = sourceSynValue.findPropertyByPredicate(predicate);
				
				if (s == null) {
					continue;
				}
				
				ShowlDirectPropertyShape sourceDirectField = s.direct();
				
						
				ShowlPropertyShape selectedSourceField = null;
				if (sourceDirectField != null) {
					selectedSourceField = sourceDirectField;
					struct.put(predicate, new ShowlDirectPropertyExpression(sourceDirectField));
				} else {
					

					if (s.size() == 1) {
						ShowlPropertyShape sourceField = s.get(0);
						sourceDirectField = sourceField.direct();

						Set<ShowlExpression> valueSet = sourceField.getHasValue();

						if (valueSet.isEmpty()) {

							ShowlNodeShape sourceChildNode = sourceField.getValueShape();
							ShowlNodeShape targetChildNode = targetField.getValueShape();

							if (sourceChildNode != null && targetChildNode != null) {
								ShowlStructExpression childStruct = new ShowlBasicStructExpression(targetField);
								targetField.setSelectedExpression(ShowlDelegationExpression.getInstance());
								struct.put(predicate, childStruct);
								addStructProperties(state, childStruct, targetField, sourceField);
								continue;

							}


						} else if (valueSet.size() == 1) {
							ShowlExpression value = valueSet.iterator().next();
							if (value instanceof ShowlFilterExpression) {
								value = ((ShowlFilterExpression) value).getValue();
								struct.put(predicate, value);
							}
							selectedSourceField = sourceField;
							if (value instanceof ShowlFunctionExpression) {
								if (targetField.getValueShape() == null) {
									struct.put(predicate, value);
								} else {
									fail("Function returning a struct not supported yet at {0}", sourceProperty.getPath());
								}
							} else if (value instanceof ShowlEnumIndividualReference) {
								if (targetField.getValueShape() != null) {

									ShowlNodeShape enumNode = state.enumNode(targetField.getValueShape());
									enumNode.setTargetProperty(targetField);
									ShowlEnumStructExpression enumStruct = new ShowlEnumStructExpression(targetField, enumNode);

									struct.put(predicate, enumStruct);
									addEnumStructProperties(enumStruct, targetField.getValueShape(), enumNode);
									ShowlDirectPropertyShape enumId = enumNode.getProperty(Konig.id);
									ShowlEnumIndividualReference enumRef = (ShowlEnumIndividualReference) value;
									enumStruct.put(Konig.id, enumRef);
									ShowlStatement joinCondition = new ShowlEqualStatement(new ShowlDirectPropertyExpression(enumId),
											enumRef);

									ShowlChannel channel = new ShowlChannel(enumNode, joinCondition);
									targetDirect.getRootNode().addChannel(channel);
									continue;

								}

								struct.put(predicate, value);

							}
						} else if (valueSet.size() > 1) {
							// TODO: handle multiple values
							String msg = MessageFormat.format("Cannot handle multiple values at {}.{}", sourceProperty.getPath(),
									predicate.getLocalName());
							throw new ShowlProcessingException(msg);
						} else {

							// TODO: handle multiple variants
							String msg = MessageFormat.format("Cannot derive field at {0}.{1}", sourceProperty.getPath(),
									predicate.getLocalName());
							throw new ShowlProcessingException(msg);
						}
						

					} else if (!s.isEmpty()) {
						// TODO: handle multiple variants
						String msg = MessageFormat.format("Cannot handle multiple variants at {}.{}", sourceProperty.getPath(),
								predicate.getLocalName());
						throw new ShowlProcessingException(msg);
					}
				}

				if (selectedSourceField != null && targetField.getSelectedExpression() == null) {
					targetField.setSelectedExpression(ShowlDelegationExpression.getInstance());
				}

				ShowlNodeShape targetValueNode = targetField.getValueShape();

				if (targetValueNode != null) {

					if (selectedSourceField != null) {
						ShowlStructExpression child = new ShowlBasicStructExpression(targetField);
						addStructProperties(state, child, targetField, selectedSourceField);

					} else {

						String msg = MessageFormat.format("Target field {0} contains a nested record, but no mapping was found.",
								targetField.getPath());
						throw new ShowlProcessingException(msg);
					}
				}
			}
		}

	}

	private void fail(String pattern, Object... arguments) throws ShowlProcessingException {
		String msg = MessageFormat.format(pattern, arguments);
		throw new ShowlProcessingException(msg);

	}

	private void addEnumStructProperties(ShowlStructExpression enumStruct, ShowlNodeShape targetNode,
			ShowlNodeShape enumNode) {

		for (ShowlDirectPropertyShape targetProperty : targetNode.getProperties()) {
			ShowlDirectPropertyShape enumProperty = enumNode.getProperty(targetProperty.getPredicate());
			if (enumProperty == null) {
				throw new ShowlProcessingException("Enum property not found for " + targetProperty.getPath());
			}
			enumStruct.put(targetProperty.getPredicate(), new ShowlEnumPropertyExpression(enumProperty));
		}

	}

	private boolean alternativePathsMapping(
			State state,
			boolean isEnum, 
			ShowlPropertyShapeGroup sourcePropertyGroup,
			ShowlPropertyShapeGroup targetProperty
	) {

		if (arrayMapping(state, sourcePropertyGroup, targetProperty)) {
			return true;
		}

		if (alternativePaths(state, sourcePropertyGroup, targetProperty)) {
			return true;
		}

		return false;
	}

	private boolean alternativePaths(
			State state,
			ShowlPropertyShapeGroup sourcePropertyGroup,
			ShowlPropertyShapeGroup targetProperty
	) {

		ShowlDirectPropertyShape targetDirect = targetProperty.direct();
		PropertyConstraint constraint = targetDirect.getPropertyConstraint();
		if (constraint != null && constraint.getMaxCount() != null && constraint.getMaxCount() == 1) {
			if (containsAlternativePaths(sourcePropertyGroup)) {

				ShowlAlternativePathsExpression e = createAlternativePathsExpression(state, sourcePropertyGroup, targetDirect);
				targetDirect.setSelectedExpression(e);

				return true;
			}

		}

		return false;
	}

	private ShowlAlternativePathsExpression createAlternativePathsExpression(
			State state,
			ShowlPropertyShapeGroup sourcePropertyGroup,
			ShowlDirectPropertyShape targetDirect
	) {

		ShowlAlternativePathsExpression e = new ShowlAlternativePathsExpression();
		addMembers(state, e, targetDirect, sourcePropertyGroup);
		return e;
	}

	private boolean containsAlternativePaths(ShowlPropertyShapeGroup sourcePropertyGroup) {
		if (sourcePropertyGroup.size() < 2) {
			return false;
		}

		int count = 0;

		for (ShowlPropertyShape p : sourcePropertyGroup) {
			if (containsHasValue(p) && ++count > 1) {
				return true;
			}
		}

		return false;
	}

	private boolean containsHasValue(ShowlPropertyShape p) {
		if (!p.getHasValue().isEmpty()) {
			return true;
		}

		if (p.getValueShape() != null) {
			return containsHasValue(p.getValueShape());
		}

		return false;
	}

	private boolean containsHasValue(ShowlNodeShape node) {

		return containsHasValue(node.getProperties()) || listContainsHasValue(node.getDerivedProperties());
	}

	private boolean listContainsHasValue(Collection<ShowlDerivedPropertyList> derivedProperties) {
		for (ShowlDerivedPropertyList list : derivedProperties) {
			if (containsHasValue(list)) {
				return true;
			}
		}
		return false;
	}

	private boolean containsHasValue(Collection<? extends ShowlPropertyShape> properties) {
		for (ShowlPropertyShape p : properties) {
			if (containsHasValue(p)) {
				return true;
			}
		}
		return false;
	}

	private boolean useHasValue(ShowlPropertyShapeGroup sourcePropertyGroup, ShowlDirectPropertyShape targetDirect) {
		ShowlExpression s = null;
		int count = 0;
		for (ShowlPropertyShape sourceProperty : sourcePropertyGroup) {
			Set<ShowlExpression> valueSet = sourceProperty.getHasValue();
			if (valueSet.size() > 1) {
				return false;
			}
			if (valueSet.size() == 1) {
				if (++count > 1) {
					return false;
				}
				s = valueSet.iterator().next();
			}
		}

		if (count == 1) {
			targetDirect.setSelectedExpression(s);
		}
		return count == 1;
	}

	/**
	 * If the value class of the target property declares an IRI and if one of the
	 * source PropertyShapes can populate that template, then use that template
	 * expression.
	 * 
	 * @param sourcePropertyGroup
	 * @param targetProperty
	 * @return True if the template expression is selected within the target
	 *         property, and false otherwise.
	 */
	private boolean useClassIriTemplate(ShowlPropertyShapeGroup sourcePropertyGroup,
			ShowlDirectPropertyShape targetProperty) {
		ShowlClass owlClass = targetProperty.getValueType(schemaService);
		Graph graph = schemaService.getOwlReasoner().getGraph();
		Vertex v = graph.getVertex(owlClass.getId());
		if (v != null) {
			Value templateValue = v.getValue(Konig.iriTemplate);
			if (templateValue != null) {
				IriTemplate iriTemplate = new IriTemplate(templateValue.stringValue());

				for (ShowlPropertyShape sourceProperty : sourcePropertyGroup) {
					ShowlExpression e = ShowlFunctionExpression.fromIriTemplate(schemaService, nodeService, sourceProperty,
							iriTemplate);
					if (ShowlUtil.isWellDefined(e)) {
						targetProperty.setSelectedExpression(e);
						return true;
					}
				}
			}
		}

		return false;
	}

	private ShowlNodeShape nextSource(State state) {

		Set<ShowlPropertyShapeGroup> propertyPool = state.propertyPool;
		Set<ShowlNodeShape> candidateSet = state.candidateSet;

		Iterator<ShowlNodeShape> sequence = candidateSet.iterator();
		if (candidateSet.size() == 1) {
			ShowlNodeShape result = sequence.next();
			sequence.remove();
			return result;
		} else {
			int bestRank = 0;
			ShowlNodeShape result = null;
			while (sequence.hasNext()) {
				ShowlNodeShape candidate = sequence.next();

				ShowlEffectiveNodeShape targetNode = candidate.getTargetNode().effectiveNode();
				ShowlEffectiveNodeShape sourceNode = candidate.effectiveNode();

				int rank = rank(sourceNode, targetNode, propertyPool);
				if (logger.isTraceEnabled()) {
					logger.trace("nextSource: rank({})={}", sourceNode.toString(), rank);
				}
				if (rank == 0) {
					sequence.remove();
				} else if(rank == bestRank) {
					if (candidate.getId().stringValue().length() < result.getId().stringValue().length()) {
						bestRank = rank;
						result = candidate;
					}					
				} else if (rank > bestRank) {
					bestRank = rank;
					result = candidate;
				}
			}
			if (result != null) {
				candidateSet.remove(result);
			}
			return result;
		}
	}

	private int rank(ShowlEffectiveNodeShape sourceNode, ShowlEffectiveNodeShape targetNode,
			Set<ShowlPropertyShapeGroup> propertyPool) {
		int rank = 0;

		for (ShowlPropertyShapeGroup p : propertyPool) {
			if (p.getSelectedExpression() == null) {
				List<ShowlPropertyShapeGroup> path = p.relativePath(targetNode);
				ShowlPropertyShapeGroup q = sourceNode.findPropertyByPath(path);
				if (q != null) {
					if (q.isWellDefined()) {
						rank++;
					}
				}

			}
		}

		if (logger.isTraceEnabled()) {
			logger.trace("rank: rank({})={}", sourceNode, rank);
		}

		return rank;
	}

	/**
	 * Get the set of properties that need to be mapped.
	 */
	private Set<ShowlPropertyShapeGroup> propertyPool(ShowlNodeShape targetNode) {
		Set<ShowlPropertyShapeGroup> pool = new LinkedHashSet<>();

		addPropertiesToPool(pool, targetNode.effectiveNode());

		return pool;
	}

	private void addPropertiesToPool(Set<ShowlPropertyShapeGroup> pool, ShowlEffectiveNodeShape eNode) {

		for (ShowlPropertyShapeGroup p : eNode.getProperties()) {
			if (p.withSelectedExpression() == null && p.direct() != null) {
				pool.add(p);

				if (logger.isTraceEnabled()) {
					logger.trace("addPropertiesToPool: added {}", p.pathString());
				}

			}
			if (p.getValueShape() != null) {
				addPropertiesToPool(pool, p.getValueShape());
			}
		}

	}
	
	private void handleModifiedProperty(State state) {

		ShowlPropertyShapeGroup targetProperty = state.getTargetModified();
		
		if (targetProperty != null && targetProperty.getSelectedExpression()==null) {
			ShowlDirectPropertyShape targetModified = targetProperty.direct();

			ShowlNodeShape focusNode = focusSourceNode(state);
			if (focusNode != null) {
				ShowlDirectPropertyShape sourceModified = focusNode.getProperty(Konig.id);
				if (sourceModified != null) {
					targetModified.setSelectedExpression(new ShowlDirectPropertyExpression(sourceModified));
					return;
				}
			}
		
			targetModified.setSelectedExpression(ShowlSystimeExpression.INSTANCE);
		}
	}

	private ShowlNodeShape focusSourceNode(State state) {
		ShowlNodeShape result = focusSourceNode();
		if (result != null) {
			return result;
		}
		List<ShowlChannel> list = state.targetNode.nonEnumChannels(schemaService.getOwlReasoner());
		
		return list.isEmpty() ? null : list.get(0).getSourceNode();
	}

	protected ShowlNodeShape focusSourceNode() {
		return null;
	}

}
