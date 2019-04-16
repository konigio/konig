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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.konig.shacl.Shape;

public class CompositeSourceNodeSelector implements ShowlSourceNodeSelector {
	private List<ShowlSourceNodeSelector> list = new ArrayList<>();
	
	public void add(ShowlSourceNodeSelector selector) {
		list.add(selector);
	}
	
	public CompositeSourceNodeSelector(ShowlSourceNodeSelector...members) {
		for (ShowlSourceNodeSelector m : members) {
			add(m);
		}
	}

	@Override
	public Set<ShowlNodeShape> selectCandidateSources(ShowlFactory factory, ShowlNodeShape targetShape) {
		Set<ShowlNodeShape> set = null;
		for (ShowlSourceNodeSelector s : list) {
			Set<ShowlNodeShape> t = s.selectCandidateSources(factory, targetShape);
			if (!t.isEmpty()) {
				if (set == null) {
					set = new HashSet<>();
				}
				set.addAll(t);
			}
		}
		
		return set==null ? Collections.emptySet() : set;
	}

}
