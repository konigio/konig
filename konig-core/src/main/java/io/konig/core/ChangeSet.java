package io.konig.core;

import org.openrdf.model.Resource;

public interface ChangeSet {
	
	Vertex asVertex();
	Resource getId();
	
	Vertex assertPriorState();
	Vertex assertAddition();
	Vertex assertRemoval();
	
	Vertex getPriorState();
	Vertex getAddition();
	Vertex getRemoval();

}
