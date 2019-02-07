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

public class MappingStrategy {
	public static final Logger logger = LoggerFactory.getLogger(MappingStrategy.class);
	
	private ShowlMappingFilter filter;
	
	public MappingStrategy() {
		
	}

	public MappingStrategy(ShowlMappingFilter filter) {
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
	public List<ShowlDirectPropertyShape> selectMappings(ShowlNodeShape target) {
		if (logger.isTraceEnabled()) {
			logger.trace("selecteMappings: target={}", target.getPath());
		}
		Set<ShowlJoinCondition> set = new LinkedHashSet<>();
		
		List<ShowlDirectPropertyShape> pool = new ArrayList<>();
		buildPool(target, pool, set);
		
		
		Map<ShowlJoinCondition, RankedJoinCondition> rankingMap = new LinkedHashMap<>();
		for (ShowlJoinCondition join : set) {
			rankingMap.put(join, new RankedJoinCondition((ShowlTargetToSourceJoinCondition)join));
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

	private void buildPool(ShowlNodeShape target, List<ShowlDirectPropertyShape> pool, Set<ShowlJoinCondition> set) {
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
			}
			
		}
		
	}

	private boolean addSelectedJoin(ShowlNodeShape targetNode, RankedJoinCondition ranked) {
		
		ShowlTargetToSourceJoinCondition s2t = ranked.getJoin();
		
		ShowlPropertyShape targetProperty = s2t.propertyOf(targetNode);
		ShowlPropertyShape sourceProperty = s2t.otherProperty(targetProperty);
		ShowlNodeShape sourceNode = sourceProperty.getDeclaringShape();
		
		if (targetNode.getSelectedJoins().isEmpty()) {
			targetNode.addSelectedJoin(new ShowlFromCondition(s2t, sourceNode));
			return true;
		} else {
			// Build a source-to-source join condition
			
			List<ShowlJoinCondition> list = targetNode.getSelectedJoins();
			for (int i=list.size()-1; i>=0; i--) {
				ShowlJoinCondition prior = list.get(i);
				ShowlSourceToSourceJoinCondition s2s = sourceToSourceJoin(s2t, prior, sourceNode);
				if (s2s != null) {
					s2t.setSourceToSource(s2s);
					targetNode.addSelectedJoin(s2s);
					return true;
				}
			}
		}
		return false;
		
	}

	private ShowlSourceToSourceJoinCondition sourceToSourceJoin(ShowlTargetToSourceJoinCondition s2t, ShowlJoinCondition prior, ShowlNodeShape b) {
		// For now, we only support join by Id
		
		ShowlNodeShape a = prior.focusNode();
		ShowlPropertyShape aId = a.findProperty(Konig.id);
		ShowlPropertyShape bId = b.findProperty(Konig.id);
		if (aId != null && bId!=null) {
			return new ShowlSourceToSourceJoinCondition(s2t, aId, bId, null);
		}
		
		return null;
	}

	private void selectMappings(ShowlNodeShape node, ShowlJoinCondition join, List<ShowlDirectPropertyShape> pool) {
		ShowlPropertyShape joinProperty = join.propertyOf(node);
		Iterator<ShowlDirectPropertyShape> sequence = pool.iterator();
		while (sequence.hasNext()) {
			ShowlDirectPropertyShape p = sequence.next();
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
			List<ShowlDirectPropertyShape> pool) {
		
		for (RankedJoinCondition e : rankingMap.values()) {
			e.reset();
		}
		
		for (ShowlDirectPropertyShape p : pool) {
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
		private ShowlTargetToSourceJoinCondition join;
		public RankedJoinCondition(ShowlTargetToSourceJoinCondition join) {
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
		
		public ShowlTargetToSourceJoinCondition getJoin() {
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
