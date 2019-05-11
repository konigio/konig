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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicTransformService implements ShowlTransformService {
	
	private static final Logger logger = LoggerFactory.getLogger(BasicTransformService.class);

	private ShowlSourceNodeFactory sourceNodeFactory;
	
	
	
	public BasicTransformService(ShowlSourceNodeFactory sourceNodeFactory) {
		this.sourceNodeFactory = sourceNodeFactory;
	}


	@Override
	public Set<ShowlPropertyShapeGroup> computeTransform(ShowlNodeShape targetNode)
			throws ShowlProcessingException {
		
		Set<ShowlPropertyShapeGroup> propertyPool = propertyPool(targetNode);
		Set<ShowlNodeShape> candidateSet = sourceNodeFactory.candidateSourceNodes(targetNode);
		if (candidateSet.isEmpty()) {
			logger.warn("Failed to transform {}.  No candidate source shapes were found.", 
					targetNode.getPath());
		} else {
			
			while (!propertyPool.isEmpty() && !candidateSet.isEmpty()) {
				
				ShowlNodeShape sourceShape = nextSource(propertyPool, candidateSet);
				if (sourceShape == null) {
					break;
				}
				
				computeMapping(sourceShape.effectiveNode(), targetNode.effectiveNode(), propertyPool);
			}
			
			
		}
		
		return propertyPool;
	}
	

	private void computeMapping(ShowlEffectiveNodeShape sourceNode, ShowlEffectiveNodeShape targetNode,
			Set<ShowlPropertyShapeGroup> propertyPool) {
		
		Iterator<ShowlPropertyShapeGroup> sequence = propertyPool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShapeGroup targetProperty = sequence.next();
			List<ShowlPropertyShapeGroup> path = targetProperty.relativePath(targetNode);
			
			ShowlPropertyShapeGroup sourceProperty = sourceNode.findPropertyByPath(path);
			
			if (createMapping(sourceProperty, targetProperty)) {
				sequence.remove();
			}
		}
	}



	private boolean createMapping(ShowlPropertyShapeGroup sourceProperty, ShowlPropertyShapeGroup targetProperty) {
		if (sourceProperty != null) {
			ShowlDirectPropertyShape sourceDirect = sourceProperty.synonymDirect();
			ShowlDirectPropertyShape targetDirect = targetProperty.synonymDirect();
			
			if (targetDirect != null) {
				if (sourceDirect != null) {
					targetDirect.setSelectedExpression(new ShowlDirectPropertyExpression(sourceDirect));
					return true;
				}
				
				for (ShowlPropertyShape sourcePropertyElement : sourceProperty) {
					ShowlExpression formula = sourcePropertyElement.getFormula();
					if (ShowlUtil.isWellDefined(formula)) {
						targetDirect.setSelectedExpression(formula);
						return true;
					}
				}
			}
			if (logger.isWarnEnabled()) {
				logger.warn("Failed to create mapping: {}...{}", sourceProperty, targetProperty);
			}
		}
		return false;
	}


	private ShowlNodeShape nextSource(Set<ShowlPropertyShapeGroup> propertyPool, Set<ShowlNodeShape> candidateSet) {
	
		
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
