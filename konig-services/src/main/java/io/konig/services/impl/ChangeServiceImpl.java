package io.konig.services.impl;

/*
 * #%L
 * Konig Services
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


import java.util.HashMap;
import java.util.Map;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.ChangeSet;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.impl.EdgeImpl;
import io.konig.services.ChangeService;

public class ChangeServiceImpl implements ChangeService {
	
	public ChangeServiceImpl() {
	}


	@Override
	public void apply(Graph source, ChangeSet delta) {

		Vertex refVertex = delta.getReference();
		Vertex addVertex = delta.getAddition();
		Vertex removeVertex = delta.getRemoval();
		
		Graph reference = refVertex==null ? null : refVertex.asNamedGraph();
		Graph add = addVertex==null ? null : addVertex.asNamedGraph();
		Graph remove = removeVertex==null ? null : removeVertex.asNamedGraph();
		
		execute(source, reference, add, remove);
	}

	@Override
	public void undo(Graph target, ChangeSet delta) {
		
		
		Vertex refVertex = delta.getReference();
		Vertex addVertex = delta.getAddition();
		Vertex removeVertex = delta.getRemoval();
		
		Graph reference = refVertex==null ? null : refVertex.asNamedGraph();
		Graph add = addVertex==null ? null : addVertex.asNamedGraph();
		Graph remove = removeVertex==null ? null : removeVertex.asNamedGraph();
		
		execute(target, reference, remove, add);
		
	}
	
	void execute(Graph source,  Graph reference, Graph add, Graph remove) {
		Map<String, Vertex> bnodeMap = new HashMap<>();
		
		if (remove != null) {
			for (Edge edge : remove) {
				edge = targetEdge(bnodeMap, reference, source, edge);
				source.remove(edge);
			}
		}
		
		if (add != null) {
			for (Edge edge : add) {
				edge = targetEdge(bnodeMap, reference, source, edge);
				source.add(edge);
			}
		}
	}


	private Edge targetEdge(Map<String, Vertex> bnodeMap, Graph reference, Graph source, Edge edge) {
		Resource subject = edge.getSubject();
		Value object = edge.getObject();
		
		
		if (!(subject instanceof BNode) && !(object instanceof BNode)) {
			return edge;
		}
		
		if (subject instanceof BNode) {
			subject = mapBNode(bnodeMap, reference, source, (BNode)subject);
		}
		
		if (object instanceof BNode) {
			object = mapBNode(bnodeMap, reference, source, (BNode)object);
		}

		URI predicate = edge.getPredicate();
		
		return new EdgeImpl(subject, predicate, object);
	}


	private BNode mapBNode(Map<String, Vertex> bnodeMap, Graph reference, Graph graph, BNode subject) {
		
		
		
		
		return null;
	}
	
	
	

}
