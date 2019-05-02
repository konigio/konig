package io.konig.core.showl;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.vocab.Konig;

public class ObsoleteMappingStrategy implements ShowlMappingStrategy {
	public static final Logger logger = LoggerFactory.getLogger(ObsoleteMappingStrategy.class);
	
	private ShowlMappingFilter filter;
	
	public ObsoleteMappingStrategy() {
		
	}

	public ObsoleteMappingStrategy(ShowlMappingFilter filter) {
		this.filter = filter;
	}

	/**
	 * Select the source-to-target-mappings for the specified target NodeShape.
	 * As a side-effect, the selected mappings are stored in each ShowlDirectPropertyShape contained in the node,
	 * and the selected join conditions are stored in the target node.
	 * 
	 * @param target The NodeShape whose mappings are to be selected.
	 * @return The set of properties for which no mapping was found.
	 */
	@Override
	public Set<ShowlPropertyShape> selectMappings(ShowlManager manager, ShowlNodeShape target) {
		if (logger.isTraceEnabled()) {
			logger.trace("selecteMappings: target={}", target.getPath());
		}
		Set<ShowlJoinCondition> set = new LinkedHashSet<>();
		
		Set<ShowlPropertyShape> pool = new LinkedHashSet<>();
		buildPool(target, pool, set);
		
		
		Map<ShowlJoinCondition, RankedJoinCondition> rankingMap = new LinkedHashMap<>();
		for (ShowlJoinCondition join : set) {
			rankingMap.put(join, new RankedJoinCondition(join));
			if (logger.isTraceEnabled()) {
				logger.trace("selectMappings: rankingMap put {}", join);
			}
		}
		
		while (!pool.isEmpty()) {
			int originalSize = pool.size();
			updateRankings(rankingMap, pool);

			RankedJoinCondition best=null;
			do {
				best = findBestJoinCondition(rankingMap.values());
				
				if (best != null) {
					if (addSelectedJoin(target, best)) {
						rankingMap.remove(best.getJoin());
					
						selectMappings(target, best.getJoin(), pool);
						break;
					} else {
						best.invalidate();
						logger.trace("selectMappings: Failed to generate join condition.");
					}
				}
			} while (best != null);
			
			if (pool.size() == originalSize) {
				break;
			}
		}
		
		
		
		return pool;	
	}

	private void buildPool(ShowlNodeShape target, Set<ShowlPropertyShape> pool, Set<ShowlJoinCondition> set) {
		for (ShowlDirectPropertyShape direct : target.getProperties()) {
			pool.add(direct);
			if (logger.isTraceEnabled()) {
				logger.trace("buildPool: added {}", direct.getPath());
			}
			for (ShowlMapping m : direct.getMappings()) {
				
				if (filter != null) {
					ShowlPropertyShape other = m.findOther(direct);
					ShowlNodeShape sourceNode = other.getDeclaringShape();
					if (!filter.allowMapping(sourceNode, target)) {
						logger.trace("selectMappings: filtering {}", sourceNode);
						continue;
					}
				}
				
				set.add(m.getJoinCondition());
			}
			ShowlNodeShape valueShape = direct.getValueShape();
			if (valueShape != null) {
				buildPool(valueShape, pool, set);
			} else {
				// Does this logic really belong inside the else block?
				// Is there a better way to handle this case?

				ShowlPropertyShape peer = direct.getPeer();
				if (peer != null) {
					valueShape = peer.getValueShape();
					if (valueShape != null) {
						buildPeerPool(valueShape, pool, set);
					}
				}
			}
			
			
			
		}
		
	}

	private void buildPeerPool(ShowlNodeShape target, Set<ShowlPropertyShape> pool,	Set<ShowlJoinCondition> set) {
//		for (ShowlDerivedPropertyList list : target.getDerivedProperties()) {
//			for (ShowlPropertyShape ps : list) {
//				pool.add(ps);
//				if (logger.isTraceEnabled()) {
//					logger.trace("buildPool: added {}", ps.getPath());
//				}
//				for (ShowlMapping m : ps.getMappings()) {
//					
//					if (filter != null) {
//						ShowlPropertyShape other = m.findOther(ps);
//						ShowlNodeShape sourceNode = other.getDeclaringShape();
//						if (!filter.allowMapping(sourceNode, target)) {
//							logger.trace("selectMappings: filtering {}", sourceNode);
//							continue;
//						}
//					}
//					
//					set.add(m.getJoinCondition());
//				}
//				ShowlNodeShape valueShape = ps.getValueShape();
//				if (valueShape != null) {
//					buildPool(valueShape, pool, set);
//				} else {
//					// Does this logic really belong inside the else block?
//					// Is there a better way to handle this case?
//	
//					ShowlPropertyShape peer = ps.getPeer();
//					if (peer != null) {
//						valueShape = peer.getValueShape();
//						if (valueShape != null) {
//							buildPeerPool(valueShape, pool, set);
//						}
//					}
//				}
//				
//				
//				
//			}
//		}
		
	}

	private boolean addSelectedJoin(ShowlNodeShape targetNode, RankedJoinCondition ranked) {

		ShowlJoinCondition rankedJoin = ranked.getJoin();

		if (rankedJoin instanceof ShowlFromCondition) {
			targetNode.addSelectedJoin(rankedJoin);
			return true;
		}
		
		
		ShowlPropertyShape targetProperty = rankedJoin.propertyOf(targetNode);
		ShowlPropertyShape sourceProperty = rankedJoin.otherProperty(targetProperty);
		ShowlNodeShape sourceNode = sourceProperty.getDeclaringShape();
		
		if (targetNode.getSelectedJoins().isEmpty()) {
			targetNode.addSelectedJoin(new ShowlFromCondition(rankedJoin, sourceNode));
			return true;
		} else {
			// Build a source-to-source join condition
			
			List<ShowlJoinCondition> list = targetNode.getSelectedJoins();
			for (int i=list.size()-1; i>=0; i--) {
				ShowlJoinCondition prior = list.get(i);
				ShowlSourceToSourceJoinCondition s2s = sourceToSourceJoin(prior, sourceNode);
				if (s2s != null) {
					targetNode.addSelectedJoin(s2s);
					return true;
				}
			}
		}
		return false;
		
	}

	private ShowlSourceToSourceJoinCondition sourceToSourceJoin(ShowlJoinCondition prior, ShowlNodeShape b) {
		// For now, we only support join by Id
		
		ShowlNodeShape a = prior.focusNode();
		ShowlPropertyShape aId = a.findProperty(Konig.id);
		ShowlPropertyShape bId = b.findProperty(Konig.id);
		if (aId != null && bId!=null) {
			return new ShowlSourceToSourceJoinCondition(null, aId, bId);
		}
		
		return null;
	}

	private void selectMappings(ShowlNodeShape node, ShowlJoinCondition join, Set<ShowlPropertyShape> pool) {
		ShowlPropertyShape joinProperty = join.propertyOf(node);
		Iterator<ShowlPropertyShape> sequence = pool.iterator();
		while (sequence.hasNext()) {
			ShowlPropertyShape p = sequence.next();
			String action = "was NOT selected";
			ShowlMapping m = p.getMapping(join);
			if (m == null && p==joinProperty) {
				m = new ShowlJoinMapping(join);
			}
			if (m != null) {
				p.setSelectedMapping(m);
				sequence.remove();
				action = "was selected";
			} 
			
			if (logger.isTraceEnabled()) {
				
				logger.trace("selectMappings: {} {}", p.getPath(), action);
			}
			
		}
		
	}



	private RankedJoinCondition findBestJoinCondition(Collection<RankedJoinCondition> values) {
		int best = 0;
		RankedJoinCondition result = null;
		Iterator<RankedJoinCondition> sequence = values.iterator();
		while (sequence.hasNext()) {
			RankedJoinCondition r = sequence.next();
			if (r.getRanking()==0) {
				sequence.remove();
			} else  if (r.getRanking() > best) {
				best = r.getRanking();
				result = r;
			}
		}
		return result;
	}



	private void updateRankings(Map<ShowlJoinCondition, RankedJoinCondition> rankingMap,
			Set<ShowlPropertyShape> pool) {
		
		for (RankedJoinCondition e : rankingMap.values()) {
			e.reset();
		}
		
		for (ShowlPropertyShape p : pool) {
			if (logger.isTraceEnabled()) {
				logger.trace("updateRankings: updating {}", p.getPath());
			}
			for (ShowlMapping m : p.getMappings()) {

				
				RankedJoinCondition r = rankingMap.get(m.getJoinCondition());
				if (r != null) {
					r.incrementRanking();
					if (logger.isTraceEnabled()) {
						logger.trace("updateRankings: ranking({}...{})={}", 
							m.getLeftProperty().getPath(), 
							m.getRightProperty().getPath(),
							r.getRanking());
					}
				} else {
					if (logger.isTraceEnabled()) {
						logger.trace("updateRankings: ranking({}...{})=null",
								m.getLeftProperty().getPath(), 
								m.getRightProperty().getPath());
					}
				}
				
			}
		}
	}




	private void updateNestedRanking(ShowlNodeShape valueShape, RankedJoinCondition r) {
		
		if (logger.isTraceEnabled()) {
			logger.trace("updateNestedRanking({})", valueShape.getPath());
		}
		
	}




	private static class RankedJoinCondition  {
		private int ranking;
		private ShowlJoinCondition join;
		public RankedJoinCondition(ShowlJoinCondition join) {
			this.join = join;
		}
		public void incrementRanking() {
			ranking++;
			
		}
		public void reset() {
			ranking = 0;
			
		}
		
		public int getRanking() {
			return ranking;
		}
		
		public ShowlJoinCondition getJoin() {
			return join;
		}
		
		public void invalidate() {
			ranking = -1;
		}
		
		public String toString() {
			return "RankedJoinCondition(ranking: " + ranking + ", join: " + join.toString()+")";
		}
		
		
		
	}

}
