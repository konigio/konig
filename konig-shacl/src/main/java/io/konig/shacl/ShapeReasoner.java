package io.konig.shacl;

/*
 * #%L
 * Konig SHACL
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
import io.konig.core.Path;
import io.konig.core.Vertex;
import io.konig.core.path.PathFactory;

public class ShapeReasoner {
	
	private PathFactory pathFactory;

	public ShapeReasoner(PathFactory pathFactory) {
		this.pathFactory = pathFactory;
	}

	/**
	 * Add values to a focus node in accordance with the equivalentPath expressions in 
	 * a given Shape.  This reasoner will add values only if there is no current value for a given property.
	 * @param focusNode The node to which values will be added.
	 * @param shape The shape whose property constraints may defined equivalentPath expressions.
	 */
	public void assertEquivalentPaths(Vertex focusNode, Shape shape) {
		
		for (PropertyConstraint p : shape.getProperty()) {
			assertEquivalentPath(focusNode, p);
		}
	}

	private void assertEquivalentPath(Vertex focusNode, PropertyConstraint p) {
		
		Path path = p.getCompiledEquivalentPath(pathFactory);
		if (path != null) {
			URI predicate = p.getPredicate();
			Set<Edge> result = focusNode.outProperty(predicate);
			if (result==null || result.isEmpty()) {
				Graph graph = focusNode.getGraph();
				Set<Value> derived = path.traverse(focusNode);
				Resource subject = focusNode.getId();
				for (Value object : derived) {
					graph.edge(subject, predicate, object);
				}
			}
		}
		
	}

}
