package io.konig.core.extract;

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

import org.openrdf.model.BNode;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;

/**
 * A utility that extracts all properties of a given resource -- including,
 * recursively, any anonymous individuals (BNodes) referenced.
 * @author Greg McFall
 *
 */
public class ResourceExtractor {

	/**
	 * Copy all properties of a given resource into a specified graph.
	 * @param subject The resource whose properties are to be copied.
	 * @param target The graph into which the subject's properties are to be copied.
	 */
	public void extract(Vertex subject, Graph target) {
		Graph source = subject.getGraph();
		Set<Edge> out = subject.outEdgeSet();
		for (Edge e : out) {
			target.edge(e);
			Value object = e.getObject();
			if (object instanceof BNode) {
				BNode bnode = (BNode) object;
				Vertex v = source.getVertex(bnode);
				extract(v, target);
			}
		}
	}
}
