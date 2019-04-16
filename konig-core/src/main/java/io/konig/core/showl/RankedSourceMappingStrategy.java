package io.konig.core.showl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;

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
				selectExpressions(targetNode, pool, best);
			}
			
			if (pool.size() == originalSize) {
				break;
			}
			
			updateRankings(targetNode, pool, rankingMap);
		}
		
		return pool;
	}
	

	private void selectExpressions(ShowlNodeShape targetNode, List<ShowlDirectPropertyShape> pool, NodeRanking best) {
		
		ShowlNodeShape sourceNode = best.getSourceNode();
		
		boolean wasAdded = false;
		
		Iterator<ShowlDirectPropertyShape> sequence = pool.iterator();
		while (sequence.hasNext()) {
			ShowlDirectPropertyShape p = sequence.next();
			for (ShowlExpression e : p.getExpressionList()) {
				if (e.rootNode() == sourceNode) {
					sequence.remove();
					p.setSelectedExpression(e);
					
					if (!wasAdded) {
						
						addChannel(sourceNode, targetNode);
						wasAdded = true;
					}
					
				}
			}
		}
		
		
	}



	private void addChannel(ShowlNodeShape sourceNode, ShowlNodeShape targetNode) {
		
		ShowlNodeShape root = targetNode.getRoot();
		List<ShowlChannel> channelList = root.getChannels();
		
		ShowlStatement joinStatement = joinStatement(sourceNode, targetNode, channelList);
		
		ShowlChannel channel = new ShowlChannel(sourceNode, joinStatement);
		
		targetNode.addChannel(channel);
		
		if (logger.isTraceEnabled()) {
			
			if (channel.getJoinStatement()==null) {
				logger.trace("addChannel: FROM {}", sourceNode.getPath());
			} else {
				logger.trace("addChannel: JOIN {} ON {}", sourceNode.getPath(), channel.getJoinStatement().toString());
			}
		}
		
		if (root != targetNode) {
			root.addChannel(channel);
		}
	}


	private ShowlStatement joinStatement(ShowlNodeShape sourceNode, ShowlNodeShape targetNode,
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
						
						return new ShowlEqualStatement(left, right);
					}
					
				}
				
				
			}
			
			throw new ShowlProcessingException("In target node, " + targetNode.getPath()  + ", failed to produce join statement for " + sourceNode.getPath());
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
		while (sequence.hasNext()) {
			NodeRanking r = sequence.next();
			if (r.getRanking()==0) {
				sequence.remove();
			} else  if (r.getRanking() > best) {
				best = r.getRanking();
				result = r;
			}
		}
		return result;
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
