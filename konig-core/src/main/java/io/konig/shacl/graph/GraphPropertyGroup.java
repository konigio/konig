package io.konig.shacl.graph;

import java.util.HashSet;

public class GraphPropertyGroup extends HashSet<GraphPropertyShape>{
	private static final long serialVersionUID = 1L;
	
	@Override
	public boolean add(GraphPropertyShape p) {
		p.setGroup(this);
		return super.add(p);
	}

}
