package io.konig.core.impl;

import org.openrdf.model.Resource;

import io.konig.core.ChangeSet;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.KC;

public class ChangeSetImpl implements ChangeSet {
	private Vertex self;
	private Graph main;
	private Vertex priorState;
	private Vertex addition;
	private Vertex removal;

	public ChangeSetImpl(Graph main) {
		this.main = main;
		self = main.vertex();
	}
	
	public ChangeSetImpl(Vertex v) {
		self = v;
		main = v.getGraph();
	}
	
	public ChangeSetImpl(Resource id) {
		main = new MemoryGraph();
		self = main.vertex(id);
	}

	@Override
	public Vertex asVertex() {
		return self;
	}

	@Override
	public Resource getId() {
		return self.getId();
	}

	@Override
	public Vertex assertPriorState() {
		if (priorState == null) {
			priorState = main.vertex();
			priorState.assertNamedGraph();
			main.edge(getId(), KC.priorState, priorState.getId());
		}
		return priorState;
	}

	@Override
	public Vertex assertAddition() {
		if (addition == null) {
			addition = main.vertex();
			addition.assertNamedGraph();
			main.edge(getId(), KC.addition, addition.getId());
		}
		return addition;
	}

	@Override
	public Vertex assertRemoval() {
		if (removal == null) {
			removal = main.vertex();
			removal.assertNamedGraph();
			main.edge(getId(), KC.removal, removal.getId());
		}
		return removal;
	}

	@Override
	public Vertex getPriorState() {
		if (priorState == null) {
			priorState = self.asTraversal().firstVertex(KC.priorState);
		}
		return priorState;
	}

	@Override
	public Vertex getAddition() {
		if (addition == null) {
			addition = self.asTraversal().firstVertex(KC.addition);
		}
		return addition;
	}

	@Override
	public Vertex getRemoval() {
		if (removal == null) {
			removal = self.asTraversal().firstVertex(KC.removal);
		}
		return removal;
	}



}
