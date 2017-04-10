package io.konig.core.impl;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2017 Gregory McFall
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.KonigException;

/**
 * A Directed Acyclic Graph
 * @author Greg McFall
 *
 */
public class Dag {
	
	private Map<Resource, DagVertex> map = new HashMap<>();

	public Dag() {
		
	}
	
	public void addEdge(Resource subject, Resource object) {
		addEdge(vertex(subject), vertex(object));
	}
	
	public void addEdge(DagVertex subject, DagVertex object) {
		subject.getOutward().add(object);
		object.getInward().add(subject);
	}
	
	public void removeEdge(DagVertex subject, DagVertex object) {
		if (subject!=null && object!=null) {
			subject.getOutward().remove(object);
			object.getInward().remove(subject);
			
			if (subject.getOutward().isEmpty() && subject.getInward().isEmpty()) {
				map.remove(subject.getId());
			}
			
			if (object.getOutward().isEmpty() && object.getInward().isEmpty()) {
				map.remove(object.getId());
			}
		}
	}
	
	public DagVertex vertex(Resource id) {
		DagVertex v = map.get(id);
		if (v == null) {
			v = new DagVertex(id);
			map.put(id, v);
		}
		return v;
	}
	
	public List<Resource> sort() {
		List<Resource> result = new ArrayList<>();
		List<DagVertex> stack = startNodes();
		while (!stack.isEmpty()) {
			DagVertex subject = stack.remove(stack.size()-1);
			result.add(subject.getId());
			
			List<DagVertex> out = new ArrayList<>(subject.getOutward());
			for (DagVertex object : out) {
				removeEdge(subject, object);
				if (object.getInward().isEmpty()) {
					stack.add(object);
				}
			}
		}
		
		if (!map.isEmpty()) {
			throw new KonigException("The supplied graph is not a directed acyclic graph");
		}
		
		return result;
	}
	
	private List<DagVertex> startNodes() {
		List<DagVertex> set = new ArrayList<>();
		for (DagVertex v : map.values()) {
			if (v.getInward().isEmpty()) {
				set.add(v);
			}
		}
		return set;
	}

	public static Dag create(Graph graph, URI predicate) {
		Dag dag = new Dag();
		for (Edge e : graph) {
			if (predicate.equals(e.getPredicate())) {
				Resource subject = e.getSubject();
				if (!(e.getObject() instanceof Resource)) {
					throw new KonigException("Invalid Kahn graph with predicate <" + predicate + ">.  Object must be a resource.");
				}
				Resource object = (Resource) e.getObject();
				DagVertex s = dag.vertex(subject);
				DagVertex o = dag.vertex(object);
				o.getInward().add(s);
				s.getOutward().add(o);
			}
		}
		return dag;
	}
}
