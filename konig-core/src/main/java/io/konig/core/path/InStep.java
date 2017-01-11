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


import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.SPARQLBuilder;
import io.konig.core.TraversalException;
import io.konig.core.Traverser;
import io.konig.core.Vertex;

public class InStep implements Step {
	
	private URI predicate;

	public InStep(URI predicate) {
		this.predicate = predicate;
	}

	@Override
	public void traverse(Traverser traverser) throws TraversalException {
		
		Graph graph = traverser.getGraph();
		Set<Value> source = traverser.getSource();
		for (Value s : source) {
			if (s instanceof Resource) {
				Vertex subject = graph.vertex((Resource)s);
				Set<Edge> edges = subject.inProperty(predicate);
				for (Edge edge : edges) {
					Resource target = edge.getSubject();
					traverser.addResult(target);
				}
			}
		}
		
		
	}

	@Override
	public void visit(SPARQLBuilder builder) {

		builder.append('^');
		builder.append(predicate);
		
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append('^');
		builder.append(predicate.getLocalName());
		return builder.toString();
	}

	@Override
	public void append(StringBuilder builder, NamespaceManager nsManager) {
		throw new RuntimeException("Not implemented");
		
	}

}