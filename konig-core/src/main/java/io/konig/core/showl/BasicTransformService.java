package io.konig.core.showl;

import java.text.MessageFormat;
import java.util.ArrayList;

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
	
	
	public BasicTransformService(ShowlSchemaService schemaService, ShowlNodeShapeService nodeService, ShowlSourceNodeFactory sourceNodeFactory) {
		this.schemaService = schemaService;
		this.nodeService = nodeService;
		this.sourceNodeFactory = sourceNodeFactory;
	}
	
	static class State {
		ShowlNodeShape targetNode;
		Set<ShowlPropertyShapeGroup> propertyPool;
		Set<ShowlNodeShape> candidateSet;
		Set<Object> memory = new HashSet<>();
		
		public State(ShowlNodeShape targetNode, Set<ShowlPropertyShapeGroup> propertyPool, Set<ShowlNodeShape> candidateSet) {
			this.propertyPool = propertyPool;
			this.candidateSet = candidateSet;
			this.targetNode = targetNode;
		}
		
		public boolean done() {
			return propertyPool.isEmpty() || candidateSet.isEmpty();
		}
		
	}


	@Override
	public Set<ShowlPropertyShapeGroup> computeTransform(ShowlNodeShape targetNode)
			throws ShowlProcessingException {
		
		
		State state = new State(
				targetNode,
				propertyPool(targetNode),
				sourceNodeFactory.candidateSourceNodes(targetNode));
		
		state.memory.add(targetNode);
		
		if (state.propertyPool.isEmpty()) {
			throw new ShowlProcessingException("No properties found in target node " + targetNode.getPath());
		}
		
		if (state.candidateSet.isEmpty()) {
			logger.warn("Failed to transform {}.  No candidate source shapes were found.", 
					targetNode.getPath());
		} else {
			
			
			while (!state.done()) {
				
				ShowlNodeShape sourceShape = nextSource(state);
				if (sourceShape == null) {
					break;
				}
				if (!addChannel(sourceShape, targetNode)) {
					break;
				}
				
				computeMapping(sourceShape, state);
				mapEnumProperties(state);
				removeWellDefinedNodes(state);
				addCandidateSource(state);
			}
			
			
		}
		
		handleTargetFormulas(state);
		handleTeleportFormulas(state);
		
		return state.propertyPool;
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
					
					loop2 : 
					for (ShowlPropertyShape p : set) {
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
						Set<ShowlPropertyShape> set = new HashSet<>();
						formula.addProperties(set);
						
						int count = 0;
						for (ShowlPropertyShape p : set) {
							ShowlExpression e = p.getSelectedExpression();
							if (e == null) {
								if (!p.isDirect()) {
									ShowlDirectPropertyShape pDirect = p.getDeclaringShape().getProperty(p.getPredicate());
									e = pDirect.getSelectedExpression();
									if (e != null) {
										p.setSelectedExpression(e);
									}
								}
							}
							if (e!=null) {
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
			
			if (direct==null || direct.getSelectedExpression() != null) {
				continue;
			}
			ShowlNodeShape enumTargetNode = ShowlUtil.containingEnumNode(direct, schemaService.getOwlReasoner());
			if (enumTargetNode != null) {
				ShowlDirectPropertyShape uniqueKey = uniqueKey(enumTargetNode);
				if (uniqueKey != null) {
					ShowlExpression sourceKeyExpression = uniqueKey.getSelectedExpression();
					
					if (sourceKeyExpression != null) {
					
						ShowlNodeShape enumSourceNode = enumSourceNode(enumTargetNode);
						ShowlEnumNodeExpression enumNodeExpression = new ShowlEnumNodeExpression(enumSourceNode);
						enumTargetNode.getAccessor().setSelectedExpression(enumNodeExpression);
						
						ShowlPropertyShape sourceKey = enumSourceNode.getProperty(uniqueKey.getPredicate());
						if (sourceKey == null) {
							throw new ShowlProcessingException("Unique key '" + uniqueKey.getPredicate().getLocalName() + 
									"' not found in for " + enumTargetNode.getPath());
						}
						computeEnumMapping(enumSourceNode, enumTargetNode);
						
						ShowlEqualStatement statement = new ShowlEqualStatement(expression(sourceKey), sourceKeyExpression);
						ShowlChannel channel = new ShowlChannel(enumSourceNode, statement);
						state.targetNode.addChannel(channel);
						
						enumNodeExpression.setChannel(channel);
					}
				}
			}
		}
		
	}


	


	private void computeEnumMapping(ShowlNodeShape enumSourceNode, ShowlNodeShape enumTargetNode) {
		
		for (ShowlPropertyShape targetProperty : enumTargetNode.getProperties()) {
			ShowlPropertyShape sourceProperty = enumSourceNode.getProperty(targetProperty.getPredicate());
			if (sourceProperty == null) {
				continue;
			}
			
			if (targetProperty.getValueShape() != null) {
				if (sourceProperty.getValueShape()!=null) {
					if (targetProperty.getSelectedExpression()==null) {
						targetProperty.setSelectedExpression(new ShowlEnumNodeExpression(sourceProperty.getValueShape()));
					}
					computeEnumMapping(sourceProperty.getValueShape(), targetProperty.getValueShape());
				}
			} else if (targetProperty.getSelectedExpression()==null){
				targetProperty.setSelectedExpression(new ShowlEnumPropertyExpression(sourceProperty));
			}
		}
		
	}


	private ShowlNodeShape enumSourceNode(ShowlNodeShape enumTargetNode) {
		// First check to see if we have already created an enum source node
		ShowlExpression e = enumTargetNode.getAccessor().getSelectedExpression();
		if (e instanceof HasEnumNode) {
			return ((HasEnumNode) e).getEnumNode();
		}
		for (ShowlPropertyShape p : enumTargetNode.getProperties()) {
			if (p instanceof HasEnumNode) {
				return ((HasEnumNode) p).getEnumNode();
			}
		}
		
		ShowlNodeShape enumSourceNode = enumNode(enumTargetNode.getOwlClass());
		
		enumSourceNode.setTargetNode(enumTargetNode);
		enumSourceNode.setTargetProperty(enumTargetNode.getAccessor());
		
		return enumSourceNode;
	}


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
				if (direct != null && direct.getValueShape()!=null && ShowlUtil.isWellDefined(direct.getValueShape())) {
					
					
//					direct.setSelectedExpression(new ShowlStructExpression(direct));
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
	 * @param state
	 */
	private void addCandidateSource(State state) throws ShowlProcessingException {
		Set<ShowlPropertyShapeGroup> propertyPool = state.propertyPool;
		if (!propertyPool.isEmpty() && state.candidateSet.isEmpty()) {
			Iterator<ShowlPropertyShapeGroup> sequence = propertyPool.iterator();
			while ( sequence.hasNext() ) {
				ShowlPropertyShapeGroup targetGroup = sequence.next();
				ShowlDirectPropertyShape targetDirect = targetGroup.direct();
				if (targetDirect == null) {
					throw new ShowlProcessingException("Direct property missing for " + targetGroup.pathString() + ", perhaps because derived properties are not yet supported.");
				}
				if (targetDirect.getSelectedExpression() != null) {
					if (logger.isTraceEnabled()) {
						logger.trace("addCandidateSource: Removing property from pool because it was mapped out of sequence: {}", targetDirect.getPath());
					}
					sequence.remove();
					continue;
				}
				
				ShowlEffectiveNodeShape parentNode = targetGroup.getDeclaringShape();
				ShowlClass targetClass = parentNode.getTargetClass();
				if (!state.memory.contains(targetGroup)) {
					state.memory.add(targetGroup);
					if (isEnumClass(targetClass)) {
						
						ShowlNodeShape enumNode = enumNode(targetClass);
						
						enumNode.setTargetNode(targetGroup.iterator().next().getDeclaringShape());
						state.candidateSet.add(enumNode);
						return;
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
	
	private ShowlNodeShape enumNode(ShowlClass enumClass) {
		Shape enumShape = nodeService.enumNodeShape(enumClass);
		return nodeService.createShowlNodeShape(null, enumShape, enumClass);
	}


	private boolean isEnumClass(ShowlClass targetClass) {
		return targetClass==null ? false : schemaService.getOwlReasoner().isEnumerationClass(targetClass.getId());
	}

	private boolean addChannel(ShowlNodeShape sourceShape, ShowlNodeShape targetRoot) {


		if (targetRoot.getChannels().isEmpty()) {
			targetRoot.addChannel(new ShowlChannel(sourceShape, null));

			if (logger.isTraceEnabled()) {
				logger.trace("addChannel({})", sourceShape.getPath());
			}

			return true;
		} else if (isEnumClass(sourceShape.getOwlClass())) {
			ShowlStatement join = enumJoinStatement(sourceShape);
			ShowlChannel channel = new ShowlChannel(sourceShape, join);
			
			// The following if statement is ugly.  Probably ought to have enumJoinStatement method create the ShowlChannel and
			// perform this work.
				
			if (sourceShape.getTargetNode()!=null) {
				ShowlNodeShape targetNode = sourceShape.getTargetNode();
				ShowlPropertyShape targetAccessor = targetNode.getAccessor();
				if (targetAccessor!=null) {

					ShowlExpression e = targetAccessor.getSelectedExpression();
					if (e instanceof ShowlEnumNodeExpression) {
						ShowlEnumNodeExpression enumNodeExpr = (ShowlEnumNodeExpression) e;
						enumNodeExpr.setChannel(channel);
					}
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

		if (logger.isWarnEnabled()) {

			logger.warn("Failed to create channel for {} in transform of {}", sourceShape.getPath(),
					sourceShape.getTargetNode().getPath());
		}
		return false;
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


	private ShowlStatement enumJoinStatement(ShowlNodeShape sourceShape) throws ShowlProcessingException {
		
		ShowlNodeShape root = sourceShape.getTargetNode().getRoot();
		
		ShowlEffectiveNodeShape enumNode = sourceShape.effectiveNode();
		
		ShowlDirectPropertyShape enumId = sourceShape.getProperty(Konig.id);
		
		
		OwlReasoner reasoner = schemaService.getOwlReasoner();
		ShowlNodeShape targetNode = sourceShape.getTargetNode();
		
		ShowlPropertyShape targetAccessor = targetNode.getAccessor();
		
		// Consider the case where the Source shape contains an IRI reference to the enum member.
		//
		// For instance, suppose we have the following mapping:
		//
		//    {TargetPersonShape}.gender ... {SourcePersonShape}.gender_id
		//    
		//  In this case, targetAccessor.selectedExpression will be a ShowlDirectPropertyExpression that
		//  wraps {SourcePersonShape}.gender_id
		
		if (targetAccessor.getSelectedExpression() instanceof ShowlDirectPropertyExpression) {
			return new ShowlEqualStatement(new ShowlDirectPropertyExpression(enumId), targetAccessor.getSelectedExpression());
		}
		
		

		// Map the enum accessor
		
		

		// If the targetAccessor is not null and the targetNode has a konig:id
		// property, then the targetNode accessor will be reset when the
		// konig:id property is mapped.  We should not change it here.
		//
		// This dependency on the future mapping of konig:id is unfortunate.
		// I wish we didn't have to have that dependency.  Is there a cleaner
		// solution?
		
		if (targetAccessor.getSelectedExpression() == null || 
			targetNode.getProperty(Konig.id)==null
		) {
			ShowlEnumNodeExpression accessorExpression = 
					new ShowlEnumNodeExpression(enumId.getDeclaringShape());
			targetAccessor.setSelectedExpression(accessorExpression);			
		}
		
		
		if (targetAccessor.getFormula() instanceof ShowlEnumIndividualReference ||
				targetAccessor.getFormula() instanceof ShowlIriReferenceExpression) {
			
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
						if ((
									constraint.getNodeKind() == NodeKind.IRI ||
									XMLSchema.STRING.equals(constraint.getDatatype())
								) && constraint.getShape()==null) {
							// channelJoinAccessor is an IRI reference  We can use it to join.

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
							
							
							if (
									reasoner.isInverseFunctionalProperty(predicate) || 
									channelPropertyGroup.isUniqueKey() || 
									(targetProperty!=null && targetProperty.isUniqueKey())
							) {
							
							
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
		if (targetProperty!=null) {
			ShowlExpression formula = targetProperty.getFormula();
			if (formula != null) {
				ShowlExpression left = new ShowlEnumPropertyExpression(enumId.direct());
				ShowlExpression right = formula;
				return new ShowlEqualStatement(left, right);
			}
		}
		
		String msg = MessageFormat.format("Failed to create join condition for {0} at {1}", 
				sourceShape.getPath(), 
				sourceShape.getTargetNode()==null ? "null" : sourceShape.getTargetNode().getPath());
		
		throw new ShowlProcessingException(msg);
		
	}



	private ShowlStatement enumFilter(ShowlNodeShape targetNode, ShowlNodeShape sourceShape,
			ShowlPropertyShapeGroup channelJoinAccessor) {
		
		if ( hasFilter(channelJoinAccessor) ) {
		
			ShowlPropertyShape multiValuedProperty = findMultiValuedProperty(targetNode.getAccessor());
			if (multiValuedProperty != null) {
				ShowlExpression e = multiValuedProperty.getSelectedExpression();
				if (e == null) {
					e = new ShowlArrayExpression();
					multiValuedProperty.setSelectedExpression(e);
				}
				if (e instanceof ShowlArrayExpression) {
					
					
					// TODO: build array expression
					
					ShowlArrayExpression array = (ShowlArrayExpression) e;
					return new ArrayFilterStatement(multiValuedProperty, targetNode);
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
				if ( maxCount == null || maxCount>1 ) {
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
	 * Find the property within a given source node that maps to the given target node.
	 * @param sourceNode
	 * @param targetNode
	 * @return
	 */
	private ShowlPropertyShapeGroup findPeer(ShowlNodeShape sourceNode, ShowlNodeShape targetNode) {
		
		List<URI> relativePath = ShowlUtil.relativePath(targetNode, sourceNode.getTargetNode());
		if (relativePath==null || relativePath.isEmpty()) {
			return null;
		}
		
		
		return sourceNode.effectiveNode().findPropertyByPredicatePath(relativePath);
	}

	// There is something fishy here.  We are using targetNode to build a relative path.
	// But I don't think we are passing the correct targetNode for that to work.
	
	private void computeMapping(ShowlNodeShape source,	State state) {
		ShowlEffectiveNodeShape sourceNode = source.effectiveNode();
		ShowlEffectiveNodeShape targetNode = source.getTargetNode().effectiveNode();
		
		boolean isEnum = schemaService.getOwlReasoner()
			.isEnumerationClass(sourceNode.getTargetClass().getId());
		
		Set<ShowlPropertyShapeGroup> propertyPool = state.propertyPool;
		Iterator<ShowlPropertyShapeGroup> sequence = propertyPool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShapeGroup targetProperty = sequence.next();
			
			if (mapModified(targetProperty)) {
				sequence.remove();
				continue;
			}
			
			List<ShowlPropertyShapeGroup> path = targetProperty.relativePath(targetNode);
			
			ShowlPropertyShapeGroup sourceProperty = sourceNode.findPropertyByPath(path);
			
			if (createMapping(isEnum, source, sourceProperty, targetProperty)) {
				sequence.remove();
				setAccessorExpression(targetProperty);
			}
		}
	}





	private boolean mapModified(ShowlPropertyShapeGroup targetProperty) {
		if (targetProperty.getPredicate().equals(Konig.modified)) {
			targetProperty.direct().setSelectedExpression(ShowlSystimeExpression.INSTANCE);
			return true;
		}
		return false;
	}


	private void setAccessorExpression(ShowlPropertyShapeGroup targetProperty) {
		if (targetProperty.getPredicate().equals(Konig.id)) {
			ShowlPropertyShape direct = targetProperty.direct();
			if (direct != null && direct.getSelectedExpression()!=null) {
				ShowlPropertyShape accessor = direct.getDeclaringShape().getAccessor();
				if (accessor != null && accessor.getSelectedExpression()==null) {
					accessor.setSelectedExpression(direct.getSelectedExpression());
					if (logger.isTraceEnabled()) {
						logger.trace("setAccessorExpression: {} = {}", accessor.getPath(), direct.getSelectedExpression().displayValue() );
					}
				}
			}
		} else if (targetProperty.getValueShape() != null) {
			
			ShowlEffectiveNodeShape node = targetProperty.getValueShape();
			ShowlPropertyShapeGroup group = node.findPropertyByPredicate(Konig.id);
			if (group != null) {
				
				ShowlDirectPropertyShape direct = group.direct();
				if (direct != null && direct.getSelectedExpression()==null) {
					ShowlDirectPropertyShape targetDirect = targetProperty.direct();
					if (targetDirect != null && targetDirect.getSelectedExpression()!=null) {
						direct.setSelectedExpression(targetDirect.getSelectedExpression());
						if (logger.isTraceEnabled()) {
							logger.trace("setAccessorExpression: {} = {}", direct.getPath(), targetDirect.getSelectedExpression().displayValue());
						}
					}
				}
			}
		}
		
	}


	private boolean createMapping(boolean isEnum, ShowlNodeShape sourceNode, ShowlPropertyShapeGroup sourceProperty, ShowlPropertyShapeGroup targetProperty) {
		ShowlDirectPropertyShape targetDirect = targetProperty.synonymDirect();
		if (targetDirect != null) {
			if (targetDirect.getSelectedExpression() != null) {
				if (logger.isTraceEnabled()) {
					logger.trace("createMapping: Mapping already established, {} = {}", targetDirect.getPath(), targetDirect.getSelectedExpression().displayValue());
				}
				return true;
			}
			if (sourceProperty == null) {
				return createMappingFromFormula(sourceNode, targetDirect);
			} else {
				
				if (alternativePathsMapping(isEnum, sourceProperty, targetProperty)) {
					return true;
				}
				
				ShowlDirectPropertyShape sourceDirect = sourceProperty.synonymDirect();

				if (isEnum) {
					if (Konig.id.equals(sourceDirect.getPredicate())) {
						ShowlPropertyShape enumAccessor = targetDirect.getDeclaringShape().getAccessor();
						if (enumAccessor != null) {
							ShowlExpression enumAccessorExpression = enumAccessor.getSelectedExpression();
							if (enumAccessorExpression != null && !(enumAccessorExpression instanceof ShowlEnumNodeExpression)) {
								targetDirect.setSelectedExpression(enumAccessor.getSelectedExpression());
								enumAccessor.setSelectedExpression(new ShowlEnumNodeExpression(sourceDirect.getDeclaringShape()));
								return true;
							}
						}
					}
					targetDirect.setSelectedExpression(new ShowlEnumPropertyExpression(sourceDirect));
					// Make sure the accessor is using a ShowlEnumNodeExpression as the mapping.
					
					ShowlPropertyShape accessor = targetDirect.getDeclaringShape().getAccessor();
					if (accessor != null && !(accessor.getSelectedExpression() instanceof ShowlEnumNodeExpression)) {
						ShowlNodeShape enumNode = sourceDirect.getDeclaringShape();
						accessor.setSelectedExpression(new ShowlEnumNodeExpression(enumNode));
					}
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
				
				if (logger.isTraceEnabled() && sourceProperty.getValueShape()==null) {
					logger.trace("createMapping: Failed to create mapping: {}...{}", sourceProperty, targetProperty);
				}
			}
		}
		return false;
	}
	
	private boolean createMappingFromFormula(ShowlNodeShape sourceNode, ShowlDirectPropertyShape targetDirect) {
		ShowlExpression e = targetDirect.getFormula();
		if (e != null) {
			Set<ShowlPropertyShape> set = new HashSet<>();
			e.addProperties(set);
			
			int count = 0;
			for (ShowlPropertyShape p : set) {
				if (
						p.getSelectedExpression()!=null || 
						createMapping(sourceNode, p)
					) {
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
		ShowlNodeShape joinNode = joinProperty==null ? sourceNode.getTargetNode() : joinProperty.getDeclaringShape();
		
		for (int i=0; i<path.size(); i++) {
			ShowlPropertyShape pathElement = path.get(i);
			ShowlPropertyShapeGroup group = null;
			if (pathElement.getDeclaringShape() == joinNode) {
				ShowlEffectiveNodeShape node = sourceNode.effectiveNode();
				for (int j=i; j<path.size(); j++) {
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
					if (direct!=null) {
						return new ShowlDirectPropertyExpression(direct);
					}
				}
			}
			
		}
		
		return null;
	}


	private boolean arrayMapping(ShowlPropertyShapeGroup sourcePropertyGroup, ShowlPropertyShapeGroup targetPropertyGroup) {
		ShowlDirectPropertyShape targetDirect = targetPropertyGroup.direct();
		PropertyConstraint constraint = targetDirect.getPropertyConstraint();
		if (constraint != null) {
			Integer maxCount = constraint.getMaxCount();
			if (maxCount==null || maxCount>1) {
				// Multi-valued property detected
				
				ShowlArrayExpression array = new ShowlArrayExpression();
				
				addMembers(array, targetDirect, sourcePropertyGroup);
				
				targetDirect.setSelectedExpression(array);
				return true;
			}
		}
		
		return false;
	}

	private void addMembers(ShowlArrayExpression array, ShowlDirectPropertyShape targetDirect,
			ShowlPropertyShapeGroup sourcePropertyGroup) {
		
		if (targetDirect.getValueShape() != null) {

			int memberIndex = 0;
			for (ShowlPropertyShape sourceProperty : sourcePropertyGroup) {
				if (logger.isTraceEnabled()) {
					logger.trace("addMembers: Building {}[{}]", sourcePropertyGroup.pathString(), memberIndex++);
				}
				ShowlStructExpression struct = new ShowlBasicStructExpression(targetDirect);
				addStructProperties(struct, targetDirect, sourceProperty);
				array.addMember(struct);
				
			}
			
		} else {
			// TODO: Support array of IRI reference and array of datatype values
			throw new ShowlProcessingException("Array of IRI reference or datatype value not supported yet: " + targetDirect.getPath());
		}
		
		
	}


	private void addStructProperties(ShowlStructExpression struct, ShowlDirectPropertyShape targetDirect,
			ShowlPropertyShape sourceProperty) {
		
		ShowlNodeShape sourceNode = sourceProperty.getValueShape();
		if (sourceNode != null) {
			
			for (ShowlDirectPropertyShape targetField : targetDirect.getValueShape().getProperties()) {
				URI predicate = targetField.getPredicate();
				ShowlDirectPropertyShape sourceDirectField = sourceNode.getProperty(predicate);
				ShowlPropertyShape selectedSourceField = null;
				if (sourceDirectField != null) {
					struct.put(predicate, new ShowlDirectPropertyExpression(sourceDirectField));
				} else {
					ShowlDerivedPropertyList list = sourceNode.getDerivedProperty(predicate);
					
					
					if (list.size() == 1) {
						ShowlDerivedPropertyShape sourceField = list.get(0);
						sourceDirectField = sourceField.direct();
						
						if (sourceDirectField != null) {
							struct.put(predicate, new ShowlDirectPropertyExpression(sourceDirectField));
							selectedSourceField = sourceDirectField;
						} else {
							Set<ShowlExpression> valueSet = sourceField.getHasValue();
							if (valueSet.size()==1) {
								ShowlExpression value = valueSet.iterator().next();
								if (value instanceof ShowlFilterExpression) {
									value = ((ShowlFilterExpression) value).getValue();
								}
								selectedSourceField = sourceField;
								if (value instanceof ShowlFunctionExpression) {
									if (targetField.getValueShape()==null) {
										struct.put(predicate, value);
									} else {
										fail("Function returning a struct not supported yet at {0}", sourceProperty.getPath());
									}
								} else if (value instanceof ShowlEnumIndividualReference) {
									if (targetField.getValueShape()!=null) {
										
										ShowlClass enumClass = sourceField.getValueType(schemaService);
										ShowlNodeShape enumNode = enumNode(enumClass);
										enumNode.setTargetProperty(targetField);
										ShowlEnumStructExpression enumStruct = new ShowlEnumStructExpression(targetField, enumNode);

										struct.put(predicate, enumStruct);
										addEnumStructProperties(enumStruct, targetField.getValueShape(), enumNode);
										ShowlDirectPropertyShape enumId = enumNode.getProperty(Konig.id);
										ShowlEnumIndividualReference enumRef = (ShowlEnumIndividualReference) value;
										enumStruct.put(Konig.id, enumRef);
										ShowlStatement joinCondition = 
												new ShowlEqualStatement(
														new ShowlDirectPropertyExpression(enumId), 
														enumRef);
										
										ShowlChannel channel = new ShowlChannel(enumNode, joinCondition);
										targetDirect.getRootNode().addChannel(channel);
										continue;
										
									}

									struct.put(predicate, value);
									
								}
							} else if (valueSet.size()>1) {
								// TODO: handle multiple values
								String msg = MessageFormat.format("Cannot handle multiple values at {}.{}", 
										sourceNode.getPath(), predicate.getLocalName() );
								throw new ShowlProcessingException(msg);
							} else {

								// TODO: handle multiple variants
								String msg = MessageFormat.format("Cannot derived field at {}.{}", 
										sourceNode.getPath(), predicate.getLocalName() );
								throw new ShowlProcessingException(msg);
							}
						}
						
					} else if (!list.isEmpty()) {
						// TODO: handle multiple variants
						String msg = MessageFormat.format("Cannot handle multiple variants at {}.{}", 
								sourceNode.getPath(), predicate.getLocalName() );
						throw new ShowlProcessingException(msg);
					}
				}
				
				if (selectedSourceField != null && targetField.getSelectedExpression()==null) {
					targetField.setSelectedExpression(ShowlDelegationExpression.getInstance());
				}
				
				ShowlNodeShape targetValueNode = targetField.getValueShape();
				
				
				if (targetValueNode != null) {

					if (selectedSourceField != null) {
						ShowlStructExpression child = new ShowlBasicStructExpression(targetField);
						addStructProperties(child, targetField,
								selectedSourceField);
						
					} else {

						String msg = MessageFormat.format("Target field {0} contains a nested record, but no mapping was found.", 
								targetField.getPath());
						throw new ShowlProcessingException(msg);
					}
				}
			}
		}
		
	}


	private void fail(String pattern, Object...arguments) throws ShowlProcessingException {
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


	private boolean alternativePathsMapping(boolean isEnum, ShowlPropertyShapeGroup sourcePropertyGroup,
			ShowlPropertyShapeGroup targetProperty) {
		
		if (arrayMapping(sourcePropertyGroup, targetProperty)) {
			return true;
		}
		
		// TODO: remove the following code block
		
		if (sourcePropertyGroup.size()>1 && sourcePropertyGroup.getValueShape()!=null) {
			ShowlDirectPropertyShape targetDirect = targetProperty.direct();
			ShowlNodeShape targetValueShape = targetDirect.getValueShape();
			ShowlEffectiveNodeShape sourceNode = sourcePropertyGroup.getDeclaringShape();
			if (sourceNode.getAccessor()==null) {
				List<ShowlAlternativePath> pathList = new ArrayList<>();
				for (ShowlPropertyShape p : sourcePropertyGroup) {
					ShowlNodeShape node = p.getValueShape();
					if (node != null && ShowlAlternativePath.canApplyPath(node, targetValueShape)) {
						ShowlAlternativePath path = ShowlAlternativePath.forNode(node);
						if (path != null) {
							pathList.add(path);
						}
					}
				}
				
				
				if (!pathList.isEmpty()) {
					targetDirect.setSelectedExpression(new AlternativePathsExpression(pathList));
					return true;
				}
			}
			
		}
		
		return false;
	}


	private boolean useHasValue(ShowlPropertyShapeGroup sourcePropertyGroup, ShowlDirectPropertyShape targetDirect) {
		
		for (ShowlPropertyShape sourceProperty : sourcePropertyGroup) {
			Set<ShowlExpression> valueSet = sourceProperty.getHasValue();
			if (valueSet.size() == 1) {
				ShowlExpression e = valueSet.iterator().next();
				targetDirect.setSelectedExpression(e);
				
				return true;
			}
		}
		return false;
	}


	/**
	 * If the value class of the target property declares an IRI and if one of the source PropertyShapes 
	 * can populate that template, then use that template expression. 
	 * @param sourcePropertyGroup
	 * @param targetProperty
	 * @return True if the template expression is selected within the target property, and false otherwise.
	 */
	private boolean useClassIriTemplate(ShowlPropertyShapeGroup sourcePropertyGroup, ShowlDirectPropertyShape targetProperty) {
		ShowlClass owlClass = targetProperty.getValueType(schemaService);
		Graph graph = schemaService.getOwlReasoner().getGraph();
		Vertex v = graph.getVertex(owlClass.getId());
		if (v != null) {
			Value templateValue = v.getValue(Konig.iriTemplate);
			if (templateValue != null) {
				IriTemplate iriTemplate = new IriTemplate(templateValue.stringValue());
				
				for (ShowlPropertyShape sourceProperty : sourcePropertyGroup) {
					ShowlExpression e = ShowlFunctionExpression.fromIriTemplate(schemaService, nodeService, sourceProperty, iriTemplate);
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


	private int rank(ShowlEffectiveNodeShape sourceNode, ShowlEffectiveNodeShape targetNode, Set<ShowlPropertyShapeGroup> propertyPool) {
		int rank = 0;
		
		for (ShowlPropertyShapeGroup p : propertyPool) {
			if (p.getSelectedExpression()==null) {
				List<ShowlPropertyShapeGroup> path = p.relativePath(targetNode);
				ShowlPropertyShapeGroup q = sourceNode.findPropertyByPath(path);
				if (q!=null) {
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


	private void addPropertiesToPool(Set<ShowlPropertyShapeGroup> pool,
			ShowlEffectiveNodeShape eNode) {
		
		for (ShowlPropertyShapeGroup p : eNode.getProperties()) {
			if (p.withSelectedExpression()==null && p.direct()!=null) {
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


}
