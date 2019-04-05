package io.konig.core.showl;

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
	public Set<Shape> selectCandidateSources(ShowlNodeShape targetShape) {
		Set<Shape> set = null;
		for (ShowlSourceNodeSelector s : list) {
			Set<Shape> t = s.selectCandidateSources(targetShape);
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
