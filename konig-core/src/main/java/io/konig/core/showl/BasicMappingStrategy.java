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
import java.util.Set;

public class BasicMappingStrategy implements ShowlMappingStrategy {

	@Override
	public Set<ShowlPropertyShape> selectMappings(ShowlManager manager, ShowlNodeShape target) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private class Worker {
		private ShowlManager manager;
		private ShowlNodeShape targetRoot;
		
		
		private Set<ShowlPropertyShape> selectMappings(ShowlNodeShape target) {
			
			ShowlEffectiveNodeShape targetNode = target.effectiveNode();
			
			Set<ShowlNodeShape> candidates = new HashSet<>();
			
			Set<ShowlPropertyShapeGroup> pool = new HashSet<>();
			
			scanProperties(targetNode, pool, candidates);
		
			
			
			
			
			return null;
		}


		private void scanProperties(ShowlEffectiveNodeShape targetNode, Set<ShowlPropertyShapeGroup> pool,
				Set<ShowlNodeShape> candidates) {
			
			
			for (ShowlPropertyShapeGroup p : targetNode.getProperties()) {
				pool.add(p);
				
				for (ShowlPropertyShape pElement : p) {
					
				}
				
			}
			
		}


	}


}
