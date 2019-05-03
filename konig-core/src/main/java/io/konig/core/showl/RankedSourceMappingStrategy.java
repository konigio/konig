package io.konig.core.showl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.datasource.DataSource;

public class RankedSourceMappingStrategy implements ShowlMappingStrategy {
	private static Logger logger = LoggerFactory.getLogger(RankedSourceMappingStrategy.class);

	@Override
	public Set<ShowlPropertyShape> selectMappings(ShowlManager manager, ShowlNodeShape targetNode) {
		Map<ShowlNodeShape, NodeRanking> rankingMap = new HashMap<>();
		Set<ShowlPropertyShape> pool = new LinkedHashSet<>();
		
		init(targetNode, rankingMap, pool);
		if (logger.isTraceEnabled()) {
			logger.trace("selectMappings:  pool contains...");
			for (ShowlPropertyShape p : pool) {
				logger.trace("   {}", p.getPath());
			}
		}
		
		
		while (!pool.isEmpty() && !rankingMap.isEmpty()) {
			int originalSize = pool.size();
			
			NodeRanking best = findBestSourceNode(rankingMap);
			
			if (best != null) {
				rankingMap.remove(best.getSourceNode());
				handleSourceNode(manager, pool, best.getSourceNode(), targetNode);
			}
			
			
			if (pool.size() == originalSize) {
				
				break;
			}
			
			updateRankings(targetNode, pool, rankingMap);
		}
		
		return pool;
	}
	


	private void handleSourceNode(ShowlManager manager, Set<ShowlPropertyShape> pool, ShowlNodeShape sourceNode, ShowlNodeShape defaultTargetNode) {
		
		ShowlNodeShape targetNode = sourceNode.getTargetNode();
		if (targetNode == null) {
			targetNode = defaultTargetNode;
		}
		
		if (logger.isTraceEnabled()) {
			logger.trace("handleSourceNode: Map from source {} to target {}", sourceNode.getPath(), targetNode.getPath());
		}
		

		Set<ShowlPropertyShape> set = new HashSet<>();
		boolean addChannel = true;
		Iterator<ShowlPropertyShape> sequence = pool.iterator();
		outerLoop: while (sequence.hasNext()) {
			ShowlPropertyShape p = sequence.next();
			if (logger.isTraceEnabled()) {
				
			}
			
			
			// Iterate through the list of expressions for the given property,
			// and check whether each expression is satisfied.
			// An expression is 'satisified' if there is a direct source property
			// for each path within the expression.
			
			
			
			for (ShowlExpression e : p.getExpressionList()) {
		
				expressionProperties(e, set);
				if (satisifiedBySourceNode(set, sourceNode)) {
					p.setSelectedExpression(e);
					if (addChannel) {
						addChannel = false;
						addChannel(manager, sourceNode, targetNode);
					}
					sequence.remove();
					continue outerLoop;
				}
				
				

			}
		}
		
	}



	private boolean satisifiedBySourceNode(Set<ShowlPropertyShape> set, ShowlNodeShape sourceNode) {
		ShowlNodeShape root = sourceNode.getRoot();
		for (ShowlPropertyShape p : set) {
			if (p.getRootNode() != root) {
				return false;
			}
		}
		return true;
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
					
					if (hasHardCodedValue(sourceNode.getTargetProperty())) {
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


	private boolean hasHardCodedValue(ShowlPropertyShape targetProperty) {
		ShowlExpression formula = targetProperty.getFormula();
		if (formula instanceof ShowlIriReferenceExpression) {
			targetProperty.setSelectedExpression(formula);
			return true;
		}
		return false;
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
			logger.trace("findJoinNode({}, {})", sourceNode.getPath(), targetProperty.getPath());
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

//
//	private List<ShowlDirectPropertyShape> inverseFunctionList(ShowlManager manager, ShowlNodeShape sourceNode) {
//		if (hasStaticDataSource(sourceNode)) {
//			ShowlNodeShape globalEnumShape = sourceNode.getLogicalNodeShape();
//			if (globalEnumShape != null) {
//				OwlReasoner reasoner = manager.getReasoner();
//				List<ShowlDirectPropertyShape> list = new ArrayList<>();
//				for (ShowlDirectPropertyShape direct : globalEnumShape.getProperties()) {
//					URI predicate = direct.getPredicate();
//					if (reasoner.isInverseFunctionalProperty(predicate)) {
//						list.add(direct);
//					}
//				}
//				return list;
//			}
//		}
//		return null;
//	}


	private void updateRankings(ShowlNodeShape targetNode, Set<ShowlPropertyShape> pool, Map<ShowlNodeShape, NodeRanking> rankingMap) {
		for (NodeRanking ranking : rankingMap.values()) {
			ranking.reset();
		}
		
		ShowlNodeShape targetRoot = targetNode.getRoot();

		Set<ShowlPropertyShape> set = new HashSet<>();
		for (ShowlPropertyShape p : pool) {
			for (ShowlExpression e : p.getExpressionList()) {
				for (ShowlPropertyShape sourceProperty : expressionProperties(e, set)) {
					ShowlNodeShape sourceRoot = sourceProperty.getRootNode();
					if (sourceRoot != targetRoot) {
						NodeRanking ranking = rankingMap.get(sourceRoot);
						if (ranking != null) {
							ranking.increment();
						}
					}
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
			Set<ShowlPropertyShape> pool) {
		
		Set<ShowlPropertyShape> sourcePropertySet = new HashSet<>();
		ShowlNodeShape targetRoot = targetNode.getRoot();
		for (ShowlDirectPropertyShape p : targetNode.getProperties()) {
			if (p.getSelectedExpression() == null) {
				
				if (p.getFormula()==null) {
					pool.add(p);
				} else {
					p.getFormula().addProperties(pool);
				}
				
				
				for (ShowlExpression e : p.getExpressionList()) {
					
					
					for (ShowlPropertyShape sourceProperty : expressionProperties(e, sourcePropertySet)) {
						ShowlNodeShape sourceRoot = sourceProperty.getRootNode();
						if (sourceRoot != targetRoot) {

							NodeRanking ranking = rankingMap.get(sourceRoot);
							if (ranking == null) {
								ranking = new NodeRanking(sourceRoot);
								rankingMap.put(sourceRoot, ranking);

								if (logger.isTraceEnabled()) {
									logger.trace("init: new NodeRanking({}) for {}", sourceRoot.getPath());
								}
							}
							ranking.increment();
						}
					}
				}
				
				if (p.getValueShape() != null) {
					init(p.getValueShape(), rankingMap, pool);
				}
			}
		}
		
	}








	private Set<ShowlPropertyShape> expressionProperties(ShowlExpression e, Set<ShowlPropertyShape> set) {
		set.clear();
		e.addProperties(set);
		return set;
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
