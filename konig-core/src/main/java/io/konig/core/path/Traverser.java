package io.konig.core.path;

import java.util.HashSet;
import java.util.Set;

import org.openrdf.model.Value;

import io.konig.core.Graph;

public class Traverser {
	private Graph graph;
	private Set<Value> source;
	private Set<Value> resultSet;
	
	public Traverser(Graph graph, Set<Value> source) {
		this.graph = graph;
		this.resultSet = source;
	}
	
	void visit(Step step) {
		source = resultSet;
		resultSet = new HashSet<>();
		step.traverse(this);
	}
	
	Graph getGraph() {
		return graph;
	}

	Set<Value> getSource() {
		return source;
	}
	
	Set<Value> getResultSet() {
		return resultSet;
	}
	
	void addResult(Value result) {
		resultSet.add(result);
	}

}
