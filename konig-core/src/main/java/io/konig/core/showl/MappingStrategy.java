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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MappingStrategy {
	

	/**
	 * Select the source-to-target-mappings for the specified target NodeShape.
	 * As a side-effect, the selected mappings are stored in each ShowlDirectPropertyShape contained in the node,
	 * and the selected join conditions are stored in the target node.
	 * 
	 * @param target The NodeShape whose mappings are to be selected.
	 * @return The set of properties for which no mapping was found.
	 */
	public List<ShowlDirectPropertyShape> selectMappings(ShowlNodeShape target) {
		Set<ShowlJoinCondition> set = new HashSet<>();
		
		List<ShowlDirectPropertyShape> pool = new ArrayList<>();
		for (ShowlDirectPropertyShape direct : target.getProperties()) {
			pool.add(direct);
			for (ShowlMapping m : direct.getMappings()) {
				set.add(m.getJoinCondition());
			}
		}
		
		Map<ShowlJoinCondition, RankedJoinCondition> rankingMap = new HashMap<>();
		for (ShowlJoinCondition join : set) {
			rankingMap.put(join, new RankedJoinCondition(join));
		}
		
		while (!pool.isEmpty()) {
			int originalSize = pool.size();
			
			updateRankings(rankingMap, pool);
			RankedJoinCondition best = findBestJoinCondition(rankingMap.values());
			
			if (best != null) {
				rankingMap.remove(best.getJoin());
				target.addSelectedJoin(best.getJoin());
				
				selectMappings(target, best.getJoin(), pool);
			}
			
			if (pool.size() == originalSize) {
				break;
			}
		}
		
		
		
		return pool;	
	}

	private void selectMappings(ShowlNodeShape node, ShowlJoinCondition join, List<ShowlDirectPropertyShape> pool) {
		ShowlPropertyShape joinProperty = join.getPropertyOf(node);
		Iterator<ShowlDirectPropertyShape> sequence = pool.iterator();
		while (sequence.hasNext()) {
			ShowlDirectPropertyShape p = sequence.next();
			ShowlMapping m = p.getMapping(join);
			if (m == null && p==joinProperty) {
				m = new ShowlJoinMapping(join);
			}
			if (m != null) {
				p.setSelectedMapping(m);
				sequence.remove();
			} 
		}
		
	}



	private RankedJoinCondition findBestJoinCondition(Collection<RankedJoinCondition> values) {
		int best = 0;
		RankedJoinCondition result = null;
		for (RankedJoinCondition r : values) {
			if (r.getRanking() > best) {
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
			for (ShowlMapping m : p.getMappings()) {
				RankedJoinCondition r = rankingMap.get(m.getJoinCondition());
				if (r != null) {
					r.incrementRanking();
				}
			}
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
		
		
		
		
	}

}
