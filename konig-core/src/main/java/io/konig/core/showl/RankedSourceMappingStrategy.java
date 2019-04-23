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


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;

public class RankedSourceMappingStrategy implements ShowlMappingStrategy {
	private static Logger logger = LoggerFactory.getLogger(RankedSourceMappingStrategy.class);

	@Override
	public List<ShowlDirectPropertyShape> selectMappings(ShowlManager manager, ShowlNodeShape targetNode) {
		Map<ShowlNodeShape, NodeRanking> rankingMap = new HashMap<>();
		List<ShowlDirectPropertyShape> pool = new ArrayList<>();
		
		init(targetNode, rankingMap, pool);
		
		while (!pool.isEmpty() && !rankingMap.isEmpty()) {
			int originalSize = pool.size();
			
			NodeRanking best = findBestSourceNode(rankingMap);
			
			if (best != null) {
				rankingMap.remove(best.getSourceNode());
				selectExpressions(manager, targetNode, pool, best);
			}
			
			if (pool.size() == originalSize) {
				break;
			}
			
			updateRankings(targetNode, pool, rankingMap);
		}
		
		return pool;
	}
	

	private void selectExpressions(ShowlManager manager, ShowlNodeShape targetNode, List<ShowlDirectPropertyShape> pool, NodeRanking best) {
		
		ShowlNodeShape sourceNode = best.getSourceNode();
		
		boolean wasAdded = false;
		
		Iterator<ShowlDirectPropertyShape> sequence = pool.iterator();
		outer: while (sequence.hasNext()) {
			ShowlDirectPropertyShape p = sequence.next();
			for (ShowlExpression e : p.getExpressionList()) {
				
				if (e instanceof ShowlIriReferenceExpression) {
					sequence.remove();
					p.setSelectedExpression(e);
					continue outer;
				} else	{
					ShowlNodeShape root = e.rootNode();
					if (root == sourceNode) {
						sequence.remove();
						p.setSelectedExpression(e);
						
						if (!wasAdded) {
							
							addChannel(manager, sourceNode, targetNode);
							wasAdded = true;
						}
						continue outer;
					}
					
				}
			}
		}
		
		
	}



	private void addChannel(ShowlManager manager, ShowlNodeShape sourceNode, ShowlNodeShape targetNode) {
		
		
		ShowlNodeShape root = targetNode.getRoot();
		List<ShowlChannel> channelList = root.getChannels();
		
		ShowlStatement joinStatement = joinStatement(manager, sourceNode, targetNode, channelList);
		
		ShowlChannel channel = new ShowlChannel(sourceNode, joinStatement);

		// Don't add channel for static enum shape
		if (!sourceNode.isStaticEnumShape()) {
			targetNode.addChannel(channel);
			if (root != targetNode) {
				root.addChannel(channel);
			}
		}
		
		if (logger.isTraceEnabled()) {
			
			if (channel.getJoinStatement()==null) {
				logger.trace("addChannel: FROM {}", sourceNode.getPath());
			} else {
				logger.trace("addChannel: JOIN {} ON {}", sourceNode.getPath(), channel.getJoinStatement().toString());
			}
		}
		
		sourceNode.setJoinStatement(joinStatement);
		
	}


	private ShowlStatement joinStatement(ShowlManager manager, ShowlNodeShape sourceNode, ShowlNodeShape targetNode,
			List<ShowlChannel> channelList) {
		
		
		
		if (!channelList.isEmpty()) {
			for (int i=channelList.size()-1; i>=0; i--) {
				ShowlChannel channel = channelList.get(i);
				ShowlNodeShape leftSourceNode = channel.getSourceNode();
				
				if (sourceNode.getTargetProperty()==null && leftSourceNode.getTargetProperty()==null) {
					//
					// Join at root
					
					ShowlPropertyShape leftId = leftSourceNode.findOut(Konig.id);
					ShowlPropertyShape rightId = sourceNode.findOut(Konig.id);
					
					if ((leftId != null) && (rightId != null)) {
						
						ShowlPropertyExpression left = ShowlPropertyExpression.from(leftId);
						ShowlPropertyExpression right = ShowlPropertyExpression.from(rightId);
						
						ShowlStatement statement = new ShowlEqualStatement(left, right);
						if (logger.isTraceEnabled()) {
							logger.trace("joinStatement: {}", statement.toString());
						}
						return statement;
					}
					
				} else if (hasStaticDataSource(sourceNode)) {
					
					
					ShowlIriReferenceExpression iriRef = iriRef(sourceNode);
					if (iriRef != null) {
						return null;
					}
					
					ShowlNodeShape leftJoinNode = findJoinNode(leftSourceNode, sourceNode.getTargetProperty());
					
					if (leftJoinNode != null) {
					
						// The sourceNode is a static enum shape.  Try to join with leftSourceNode since 
						// they have the same OWL Class.
						
						ShowlPropertyShape leftProperty = enumSourceKey(manager.getReasoner(), leftJoinNode);
						if (leftProperty != null) {
							ShowlDirectPropertyShape rightProperty = produceEnumProperty(sourceNode, leftProperty);
							if (leftProperty != null) {
								ShowlPropertyExpression left = ShowlPropertyExpression.from(leftProperty.maybeDirect());
								ShowlPropertyExpression right = ShowlPropertyExpression.from(rightProperty.maybeDirect());
								ShowlStatement statement = new ShowlEqualStatement(left, right);
								if (logger.isTraceEnabled()) {
									logger.trace("joinStatement: {}", statement.toString());
								}
								return statement;
							}
						}
					}
					
				}
				
				
			}
			
			throw new ShowlProcessingException("In target node, " + targetNode.getPath()  + ", failed to produce join statement for " + sourceNode.getPath());
		}
		return null;
	}


	private ShowlIriReferenceExpression iriRef(ShowlNodeShape sourceNode) {
		ShowlPropertyShape targetProperty = sourceNode.getTargetProperty();
		if (targetProperty != null) {
			for (ShowlExpression e : targetProperty.getExpressionList()) {
				if (e instanceof ShowlIriReferenceExpression) {
					return (ShowlIriReferenceExpression) e;
				}
			}
		}
		return null;
	}


	private ShowlNodeShape findJoinNode(
			ShowlNodeShape sourceNode,
			ShowlPropertyShape targetProperty) {
		
		if (logger.isTraceEnabled()) {
			logger.trace("matchTargetProperty({}, {})", sourceNode.getPath(), targetProperty.getPath());
		}
		
		List<ShowlPropertyShape> path = targetProperty.propertyPath();
		
		if (sourceNode.getAccessor() != null || sourceNode.getTargetProperty()!=null) {
			throw new RuntimeException("Nested enum not supported yet");
		}
		
		for (ShowlPropertyShape p : path) {
			ShowlPropertyShape q = sourceNode.findOut(p.getPredicate());
			if (q == null) {
				return null;
			}
			
			sourceNode = q.getValueShape();
		}
		
		return sourceNode;
	}


	private ShowlPropertyShape enumSourceKey(OwlReasoner reasoner, ShowlNodeShape node) {
		for (ShowlPropertyShape p : node.allOutwardProperties()) {
			if (reasoner.isInverseFunctionalProperty(p.getPredicate())) {
				return p;
			}
		}
		return null;
	}


	private ShowlDirectPropertyShape produceEnumProperty(ShowlNodeShape sourceNode, ShowlPropertyShape leftProperty) {
		URI predicate = leftProperty.getPredicate();
		ShowlDirectPropertyShape direct = sourceNode.getProperty(predicate);
		if (direct == null) {
			direct = new ShowlDirectPropertyShape(sourceNode, leftProperty.getProperty(), null);
			sourceNode.addProperty(direct);
			ShowlNodeShape globalEnum = sourceNode.getLogicalNodeShape();
			if (globalEnum != null && globalEnum != sourceNode) {
				produceEnumProperty(globalEnum, leftProperty);
			}
		}
		return direct;
	}


	private List<ShowlDirectPropertyShape> inverseFunctionList(ShowlManager manager, ShowlNodeShape sourceNode) {
		if (hasStaticDataSource(sourceNode)) {
			ShowlNodeShape globalEnumShape = sourceNode.getLogicalNodeShape();
			if (globalEnumShape != null) {
				OwlReasoner reasoner = manager.getReasoner();
				List<ShowlDirectPropertyShape> list = new ArrayList<>();
				for (ShowlDirectPropertyShape direct : globalEnumShape.getProperties()) {
					URI predicate = direct.getPredicate();
					if (reasoner.isInverseFunctionalProperty(predicate)) {
						list.add(direct);
					}
				}
				return list;
			}
		}
		return null;
	}


	private void updateRankings(ShowlNodeShape targetNode, List<ShowlDirectPropertyShape> pool, Map<ShowlNodeShape, NodeRanking> rankingMap) {
		for (NodeRanking ranking : rankingMap.values()) {
			ranking.reset();
		}
		
		for (ShowlPropertyShape p : pool) {
			for (ShowlExpression e : p.getExpressionList()) {
				
				ShowlNodeShape root = e.rootNode();
				if (root == targetNode) {
					continue;
				}
				NodeRanking ranking = rankingMap.get(root);
				if (ranking != null) {
					ranking.increment();
				}
			}
		}
		
	}


	private NodeRanking findBestSourceNode(Map<ShowlNodeShape, NodeRanking> rankingMap) {
		int best = 0;
		NodeRanking result = null;
		Iterator<NodeRanking> sequence = rankingMap.values().iterator();
		
		// First, we consider only non-static sources
		while (sequence.hasNext()) {
			NodeRanking r = sequence.next();
			if (r.getRanking()==0) {
				sequence.remove();
			} else  if (r.getRanking() > best  && !hasStaticDataSource(r.getSourceNode())) {
				
				best = r.getRanking();
				result = r;
			}
		}
		
		if (result == null) {
			// Now consider static sources

			for (NodeRanking r : rankingMap.values()) {
				if (r.getRanking() > best  && hasStaticDataSource(r.getSourceNode())) {
					
					best = r.getRanking();
					result = r;
				}
			}
		}
		return result;
	}


	private boolean hasStaticDataSource(ShowlNodeShape sourceNode) {
		
		for (DataSource ds : sourceNode.getShape().getShapeDataSource()) {
			if (ds instanceof StaticDataSource) {
				return true;
			}
		}
		
		return false;
	}


	private void init(ShowlNodeShape targetNode, Map<ShowlNodeShape, NodeRanking> rankingMap,
			List<ShowlDirectPropertyShape> pool) {
		
		for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
			if (p.getSelectedExpression() == null) {
				pool.add(p);
				
				for (ShowlExpression e : p.getExpressionList()) {
					
					ShowlNodeShape root = e.rootNode();
					if (root == targetNode) {
						continue;
					}
					NodeRanking ranking = rankingMap.get(root);
					if (ranking == null) {
						ranking = new NodeRanking(root);
						rankingMap.put(root, ranking);

						if (logger.isTraceEnabled()) {
							logger.trace("init: new NodeRanking({}) for {}", root.getPath());
						}
					}
					ranking.increment();
				}
				
				if (p.getValueShape() != null) {
					init(p.getValueShape(), rankingMap, pool);
				}
			}
		}
		
	}


	private static class NodeRanking  {
		private ShowlNodeShape sourceNode;
		private int ranking;
		public NodeRanking(ShowlNodeShape sourceNode) {
			this.sourceNode = sourceNode;
		}
		public void reset() {
			ranking = 0;
			
		}
		public ShowlNodeShape getSourceNode() {
			return sourceNode;
		}
		
		public void increment() {
			ranking++;
		}
		
		public int getRanking() {
			return ranking;
		}
	}

}
