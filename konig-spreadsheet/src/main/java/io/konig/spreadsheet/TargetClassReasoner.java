package io.konig.spreadsheet;

/*
 * #%L
 * Konig Spreadsheet
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


import java.util.List;

import org.openrdf.model.URI;

import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.core.vocab.SH;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeVisitor;

public class TargetClassReasoner implements ShapeVisitor {
	private Graph graph;
	
	

	public TargetClassReasoner(Graph graph) {
		this.graph = graph;
	}



	@Override
	public void visit(Shape shape) {
		
		if (shape.getTargetClass() == null) {
			Vertex v = graph.getVertex(shape.getId());	
			if (v != null) {
				List<Vertex> list = v.asTraversal().in(SH.shape).toVertexList();
				URI targetClass = null;
				for (Vertex w : list) {
					URI classValue = w.getURI(SH.valueClass);
					if (classValue == null) {
						return;
					}
					if (targetClass == null) {
						targetClass = classValue;
					} else if (!targetClass.equals(classValue)) {
						return;
					}
				}
				if (targetClass != null) {
					shape.setTargetClass(targetClass);
					graph.edge(shape.getId(), SH.targetClass, targetClass);
				}
			}
		}

	}

}
