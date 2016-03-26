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


import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class BNodeShapeBuilder {
	
	private Map<String, Shape> shapeMap = new HashMap<>();

	public Shape buildShape(Vertex node) {
		
		Shape shape = shapeMap.get(node.getId().stringValue());
		if (shape == null) {
			Graph graph = node.getGraph();
			shape = new Shape();
			
			shapeMap.put(node.getId().stringValue(), shape);
			
			Set<Entry<URI, Set<Edge>>> out = node.outEdges();
			
			for (Entry<URI, Set<Edge>> e : out) {
				URI predicate = e.getKey();
				PropertyConstraint constraint = new PropertyConstraint(predicate);
				shape.add(constraint);
				
				Set<Edge> set = e.getValue();
				
				for (Edge edge : set) {
					Value object = edge.getObject();
					if (object instanceof BNode) {
						Vertex child = graph.vertex((BNode) object);
						Shape childShape = buildShape(child);
						
						// TODO: If there is more than one child for the given predicate,
						//       use an AND constraint.
						
						constraint.setValueShape(childShape);
					} else {
						constraint.addHasValue(edge.getObject());
					}
				}
			}
		}
		
		return shape;
		
	}
}
