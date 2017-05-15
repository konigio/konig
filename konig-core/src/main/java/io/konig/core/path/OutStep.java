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

import io.konig.core.Context;
import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.SPARQLBuilder;
import io.konig.core.TraversalException;
import io.konig.core.Traverser;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.util.TurtleElements;

public class OutStep implements Step {
	URI predicate;
	

	public OutStep(URI predicate) {
		this.predicate = predicate;
	}
	

	public URI getPredicate() {
		return predicate;
	}

	@Override
	public boolean equals(Object other) {
		boolean result = false;
		if (other instanceof OutStep) {
			OutStep b = (OutStep) other;
			result = predicate.equals(b.predicate);
		}
		return result;
	}

	@Override
	public void traverse(Traverser traverser) throws TraversalException {
		
		Graph graph = traverser.getGraph();
		Set<Value> source = traverser.getSource();
		for (Value s : source) {
			if (s instanceof Resource) {
				Vertex subject = graph.vertex((Resource)s);
				Set<Edge> edges = subject.outProperty(predicate);
				for (Edge edge : edges) {
					Value object = edge.getObject();
					traverser.addResult(object);
				}
			}
		}
		
	}


	@Override
	public void visit(SPARQLBuilder builder) {
		builder.append('/');
		builder.append(predicate);
		
	}
	
	public String toString() {
		return toString(null);
	}


	@Override
	public void append(StringBuilder builder, NamespaceManager nsManager) {
		builder.append('/');
		String curie = RdfUtil.optionalCurie(nsManager, predicate);
		String value = predicate.stringValue();
		if (value.equals(curie)) {
			builder.append('<');
			builder.append(value);
			builder.append('>');
		} else {
			builder.append(curie);
		}
		
	}


	@Override
	public String toString(Context context) {

		StringBuilder builder = new StringBuilder();
		builder.append('/');
		builder.append(TurtleElements.iri(context, predicate));
		return builder.toString();
	}

}