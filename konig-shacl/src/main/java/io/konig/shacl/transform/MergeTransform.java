package io.konig.shacl.transform;

import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.URI;

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
	
	private Vertex source;
	private Vertex target;
	private Shape targetShape;
	
	public MergeTransform(Vertex source, Vertex target, Shape targetShape) {
		this.source = source;
		this.target = target;
		this.targetShape = targetShape;
	}
	
	public void execute() throws InvalidShapeException {
		Graph graph = target.getGraph();
		Set<Entry<URI, Set<Edge>>> set = source.outEdges();
		for (Entry<URI, Set<Edge>> e : set) {
			URI predicate = e.getKey();
			Set<Edge> edgeSet = e.getValue();
			if (edgeSet.isEmpty()) {
				continue;
			}
			PropertyConstraint constraint = targetShape.property(predicate);
			int maxCount = maxCount(constraint);
			
			
			if (maxCount==1) {
				// In this case, we prefer the supplied value over the existing value.
				if (edgeSet.size()>1) {
					throw new InvalidShapeException("Property " + predicate.getLocalName() + 
						" maxCount of 1 was exceeded: found " + edgeSet.size()); 
				}
				
				Edge edge = edgeSet.iterator().next();
				
				Set<Edge> targetSet = target.outProperty(predicate);
				if (!targetSet.contains(edge)) {
					targetSet.clear();
					targetSet.add(edge);
				}
				
				
			} else if (maxCount>1) {
				
				Set<Edge> targetSet = target.outProperty(predicate);
				
				int count = targetSet.size();
				for (Edge edge : edgeSet) {
					if (targetSet.contains(edge)) {
						continue;
					}
					count++;
					if (count > maxCount) {
						throw new InvalidShapeException("Property " + predicate.getLocalName() + " maxCount exceeded: " + maxCount);
					}
					graph.add(edge);
				}
				
				
			} else {
				
			}
		}
		
	}

	private int maxCount(PropertyConstraint constraint) {
		Integer maxCount = constraint==null ? null : constraint.getMaxCount();
		return maxCount==0 ? -1 : maxCount.intValue();
	}
	
	

}
