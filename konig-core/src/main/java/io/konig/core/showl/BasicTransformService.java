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


import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.util.IriTemplate;
import io.konig.core.vocab.Konig;
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
		Set<ShowlPropertyShapeGroup> memory = new HashSet<>();
		
		public State(ShowlNodeShape targetNode, Set<ShowlPropertyShapeGroup> propertyPool, Set<ShowlNodeShape> candidateSet) {
			this.propertyPool = propertyPool;
			this.candidateSet = candidateSet;
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
		
		if (state.candidateSet.isEmpty()) {
			logger.warn("Failed to transform {}.  No candidate source shapes were found.", 
					targetNode.getPath());
		} else {
			
			
			while (!state.done()) {
				
				ShowlNodeShape sourceShape = nextSource(state);
				if (sourceShape == null) {
					break;
				}
				if (!addChannel(sourceShape)) {
					break;
				}
				
				computeMapping(sourceShape, state);
				
				removeWellDefinedNodes(state);
				addCandidateSource(state);
			}
			
			
		}
		
		return state.propertyPool;
	}


	private void removeWellDefinedNodes(State state) {
		
		Iterator<ShowlPropertyShapeGroup> sequence = state.propertyPool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShapeGroup group = sequence.next();
			if (group.getValueShape() != null) {
				ShowlDirectPropertyShape direct = group.direct();
				if (direct != null && direct.getValueShape()!=null && ShowlUtil.isWellDefined(direct.getValueShape())) {
//					direct.setSelectedExpression(new ShowlStructExpression(direct));
					sequence.remove();
				}
			}
		}
		
	}


	/**
	 * If the candidate set is empty, try to add a new Candidate source
	 * @param state
	 */
	private void addCandidateSource(State state) {
		Set<ShowlPropertyShapeGroup> propertyPool = state.propertyPool;
		if (!propertyPool.isEmpty() && state.candidateSet.isEmpty()) {
			for (ShowlPropertyShapeGroup targetGroup : propertyPool) {
				ShowlEffectiveNodeShape parentNode = targetGroup.getDeclaringShape();
				ShowlClass targetClass = parentNode.getTargetClass();
				if (!state.memory.contains(targetGroup)) {
					state.memory.add(targetGroup);
					if (isEnumClass(targetClass)) {
						
						Shape enumShape = nodeService.enumNodeShape(targetClass);
						
						ShowlNodeShape enumNode = nodeService.createShowlNodeShape(null, enumShape, targetClass);
						
						enumNode.setTargetNode(targetGroup.iterator().next().getDeclaringShape());
						state.candidateSet.add(enumNode);
						return;
					} else {
						
						ShowlNodeShape targetNodeShape = parentNode.directNode();
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


	private boolean isEnumClass(ShowlClass targetClass) {
		return targetClass==null ? false : schemaService.getOwlReasoner().isEnumerationClass(targetClass.getId());
	}


	private boolean addChannel(ShowlNodeShape sourceShape) {
		
		
		ShowlNodeShape targetRoot = sourceShape.getTargetNode().getRoot();
	
		if (targetRoot.getChannels().isEmpty()) {
			targetRoot.addChannel(new ShowlChannel(sourceShape, null));

			if (logger.isTraceEnabled()) {
				logger.trace("addChannel({})", sourceShape.getPath());
			}
			
			return true;
		} else if (isEnumClass(sourceShape.getOwlClass())) {
			ShowlStatement join = enumJoinStatement(sourceShape);
			targetRoot.addChannel(new ShowlChannel(sourceShape, join));

			if (logger.isTraceEnabled()) {
				logger.trace("addChannel({}, {})", sourceShape.getPath(), join.toString());
			}
			
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
						
						// Look for owl:inverseOf attribute of accessor, and see if the sourceShape contains this value
						
						Set<URI> inverseSet = schemaService.getOwlReasoner().inverseOf(targetAccessorPredicate);
						
						for (URI inverseProperty : inverseSet) {
							ShowlPropertyShape sourceJoinProperty = sourceShape.findOut(inverseProperty);
							if (sourceJoinProperty != null) {
								ShowlEqualStatement join = new ShowlEqualStatement(leftJoinExpression, ShowlUtil.propertyExpression(sourceJoinProperty));
								targetRoot.addChannel(new ShowlChannel(sourceShape, join));
								return true;
							}
						}
						
						// Look for inverse path in sourceShape
						
						for (ShowlInwardPropertyShape inwardProperty : sourceShape.getInwardProperties()) {
							URI inwardPredicate = inwardProperty.getPredicate();
							ShowlPropertyShape inwardDirect = inwardProperty.getSynonym();
							if (inwardDirect instanceof ShowlDirectPropertyShape && targetAccessorPredicate.equals(inwardPredicate)) {
								ShowlEqualStatement join = new ShowlEqualStatement(leftJoinExpression, ShowlUtil.propertyExpression(inwardDirect));
								targetRoot.addChannel(new ShowlChannel(sourceShape, join));
								return true;
								
							}
						}
					}
					
					
				}
			}
		}
		
		if (logger.isWarnEnabled()) {
			logger.warn("Failed to create channel for {} in transform of {}", sourceShape.getPath(), sourceShape.getTargetNode().getPath());
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
		
		
		if (targetAccessor.getFormula() instanceof ShowlIriReferenceExpression) {
			
			// Special handling for hard-coded enum value
			
			return new ShowlEqualStatement(new ShowlDirectPropertyExpression(enumId), targetAccessor.getFormula());
		}

		for (ShowlChannel channel : root.getChannels()) {
			
			ShowlNodeShape channelSourceNode = channel.getSourceNode();
		
			ShowlPropertyShapeGroup channelJoinAccessor = findPeer(channelSourceNode, targetNode);
			if (channelJoinAccessor != null) {
				
				ShowlEffectiveNodeShape channelJoinNode = channelJoinAccessor.getValueShape();
				if (channelJoinNode != null) {
			
					for (ShowlPropertyShapeGroup enumProperty : enumNode.getProperties()) {	
						
						URI predicate = enumProperty.getPredicate();
						if (reasoner.isInverseFunctionalProperty(predicate)) {
							
							ShowlPropertyShapeGroup channelPropertyGroup = channelJoinNode.findPropertyByPredicate(predicate);
							
							if (channelPropertyGroup != null) {
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
		
		return null;
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
			List<ShowlPropertyShapeGroup> path = targetProperty.relativePath(targetNode);
			
			ShowlPropertyShapeGroup sourceProperty = sourceNode.findPropertyByPath(path);
			
			if (createMapping(isEnum, sourceProperty, targetProperty)) {
				sequence.remove();
			}
		}
	}





	private boolean createMapping(boolean isEnum, ShowlPropertyShapeGroup sourceProperty, ShowlPropertyShapeGroup targetProperty) {
		if (sourceProperty != null) {
			ShowlDirectPropertyShape targetDirect = targetProperty.synonymDirect();
			ShowlDirectPropertyShape sourceDirect = sourceProperty.synonymDirect();

			
			if (targetDirect != null) {
				
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
			}
			if (logger.isWarnEnabled()) {
				logger.warn("createMapping: Failed to create mapping: {}...{}", sourceProperty, targetProperty);
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
		Set<ShowlPropertyShapeGroup> pool = new HashSet<>();
		
		addPropertiesToPool(pool, targetNode.effectiveNode());
		
		return pool;
	}


	private void addPropertiesToPool(Set<ShowlPropertyShapeGroup> pool,
			ShowlEffectiveNodeShape eNode) {
		
		for (ShowlPropertyShapeGroup p : eNode.getProperties()) {
			if (p.withSelectedExpression()==null) {
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
