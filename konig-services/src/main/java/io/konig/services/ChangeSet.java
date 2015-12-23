package io.konig.services;

import io.konig.core.Graph;

public class ChangeSet {

	private Graph priorState;
	private Graph additions;
	private Graph removals;
	
	public ChangeSet(Graph priorState, Graph additions, Graph removals) {
		this.priorState = priorState;
		this.additions = additions;
		this.removals = removals;
	}

	public Graph getPriorState() {
		return priorState;
	}

	public Graph getAdditions() {
		return additions;
	}

	public Graph getRemovals() {
		return removals;
	}
	
	

}
