package io.konig.shacl.transform;

/*
 * #%L
 * konig-shacl
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

import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

/**
 * A transform that merges statements from a source vertex into a target vertex that is has a
 * particular shape.
 * 
 * @author Greg McFall
 *
 */
public class MergeTransform {
	private static final Logger logger = LoggerFactory.getLogger(MergeTransform.class);
	private Vertex source;
	private Vertex target;
	private Shape targetShape;
	
	
	public MergeTransform(Vertex source, Vertex target, Shape targetShape) {
		this.source = source;
		this.target = target;
		this.targetShape = targetShape;
	}
	
	public static void merge(Vertex source, Vertex target, Shape targetShape) throws InvalidShapeException {
		MergeTransform transform = new MergeTransform(source, target, targetShape);
		transform.execute();
	}

	private void execute(Vertex source, Vertex target, Shape targetShape) throws InvalidShapeException {
//		if (logger.isDebugEnabled()) {
//			logger.debug("------------ SOURCE ----------\n" + source.toString());
//			logger.debug("------------ TARGET ----------\n" + target.toString());
//		}
		Graph sourceGraph = source.getGraph();
		Graph graph = target.getGraph();
		
		Resource targetSubject = target.getId();
		
		
		List<PropertyConstraint> list = targetShape.getProperty();
		for (PropertyConstraint property : list) {
			URI predicate = property.getPredicate();
			Shape valueShape = property.getValueShape();
			
			Set<Edge> sourceSet = source.outProperty(predicate);
			Set<Edge> targetSet = target.outProperty(predicate);
			
			for (Edge doomed : targetSet) {
				graph.remove(doomed);
			}
			
			for (Edge edge : sourceSet) {
				
				URI sourcePredicate = edge.getPredicate();
				Value sourceObject = edge.getObject();
				
				Value targetObject = sourceObject;
				if (sourceObject instanceof BNode) {
					// TODO: use semantic reasoning to find equivalent BNode
					targetObject = graph.vertex((Resource)sourceObject).getId();
				}
				graph.edge(targetSubject, sourcePredicate, targetObject);
				if ( (targetObject instanceof Resource) && (valueShape != null) ) {
					Vertex sourceVertex = sourceGraph.vertex((Resource)sourceObject);
					Vertex targetVertex = graph.vertex((Resource)targetObject);
					
					execute(sourceVertex, targetVertex, valueShape);
				}
				
			}
		}
		
	}
	public void execute() throws InvalidShapeException {
		execute(source, target, targetShape);
	}
	
	

}
