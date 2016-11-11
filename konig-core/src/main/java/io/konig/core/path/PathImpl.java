package io.konig.core.path;

/*
 * #%L
 * konig-core
 * %%
 * Copyright (C) 2015 - 2016 Gregory McFall
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Graph;
import io.konig.core.Path;
import io.konig.core.Vertex;

public class PathImpl implements Path {
	
	private List<Step> stepList;

	public PathImpl() {
		stepList =new ArrayList<>();
	}
	
	public PathImpl(List<Step> list) {
		stepList = list;
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

	@Override
	public Path copy() {
		return new PathImpl(new ArrayList<>(stepList));
	}

	@Override
	public Path has(URI predicate, Value value) {
		stepList.add(new HasStep(predicate, value));
		return this;
	}


}
