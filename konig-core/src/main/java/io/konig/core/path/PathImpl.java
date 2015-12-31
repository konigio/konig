package io.konig.core.path;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.Path;
import io.konig.core.Vertex;

public class PathImpl implements Path {
	
	private List<Step> stepList = new ArrayList<>();

	public PathImpl() {
	}

	@Override
	public Path out(URI predicate) {
		stepList.add(new OutStep(predicate));
		return this;
	}

	@Override
	public Path in(URI predicate) {
		stepList.add(new InStep(predicate));
		return this;
	}

	@Override
	public Set<Value> traverse(Vertex source) {
		Set<Value> input = new HashSet<>();
		input.add(source.getId());
		
		Graph graph = source.getGraph();
		Traverser traverser = new Traverser(graph, input);
		for (Step step : stepList) {
			traverser.visit(step);
		}
		return traverser.getResultSet();
	}

	@Override
	public List<Step> asList() {
		return stepList;
	}


}
