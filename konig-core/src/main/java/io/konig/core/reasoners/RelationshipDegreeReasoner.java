package io.konig.core.reasoners;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.URI;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.Vertex;
import io.konig.shacl.PredicatePath;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.PropertyPath;
import io.konig.shacl.RelationshipDegree;
import io.konig.shacl.SequencePath;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class RelationshipDegreeReasoner {

	public RelationshipDegreeReasoner() {
	}
	
	public void computeRelationshipDegree(Graph graph, ShapeManager shapeManager) {
	
		Worker worker = new Worker(graph, shapeManager);
		worker.run();
	}
	
	private static class PropertyInfo {
		/**
		 * The property whose relationship degree we wish to compute.
		 */
		private Vertex property;
		
		/**
		 * The inverse of the property whose relationship degree we wish to compute
		 */
		private Vertex inverse;
		
		
		private RelationshipDegree relationshipDegree;
		
		/**
		 * The set of PropertyInfo for other properties that are aliases of
		 * the property whose relationship degree we wish to compute.
		 * 
		 * The target property and all of the alias properties have the same 
		 * relationship degree.
		 */
		private Set<PropertyInfo> alias;
		
		private Set<PropertyConstraint> propertyConstraints = new HashSet<>();
		
		public PropertyInfo(Vertex property, Vertex inverse) {
			this.property = property;
			this.inverse = inverse;
		}
		
		
		public Vertex getProperty() {
			return property;
		}


		public Vertex getInverse() {
			return inverse;
		}


		public Set<PropertyConstraint> getPropertyConstraints() {
			return propertyConstraints;
		}


		public URI getPropertyId() {
			return (URI) property.getId();
		}

		public RelationshipDegree getRelationshipDegree() {
			return relationshipDegree;
		}

		public void setRelationshipDegree(RelationshipDegree relationshipDegree) {
			this.relationshipDegree = relationshipDegree;
		}

		public void addPropertyConstraint(PropertyConstraint p) {
			propertyConstraints.add(p);
		}

		public Set<PropertyInfo> getAlias() {
			return alias;
		}

		public void setAlias(Set<PropertyInfo> set) {
			alias = set;
		}
		
		
		
		
	}
	
	private static class Worker {
		private Graph graph;
		private ShapeManager shapeManager;
		
		private Map<URI, PropertyInfo> propertyInfo = new HashMap<>();
		
		public Worker(Graph graph, ShapeManager shapeManager) {
			this.graph = graph;
			this.shapeManager = shapeManager;
		}

		private void run() {

			collectPropertyInfo();
			computeRelationshipDegrees();
			
		}

		private void collectPropertyInfo() {
			
			collectRdfProperties();
			collectPropertyConstraints();
			
			
		}

		private void collectPropertyConstraints() {
			
			for (Shape shape : shapeManager.listShapes()) {
				collectPropertyConstraintsFromShape(shape);
			}
			
		}

		private void collectPropertyConstraintsFromShape(Shape shape) {
			for (PropertyConstraint p : shape.getProperty()) {
				URI predicate = p.getPredicate();
				PropertyInfo info = null;
				
				if (predicate != null) {
					info = producePropertyInfo(predicate);
					info.addPropertyConstraint(p);
				}
				
				PropertyPath path = p.getPath();
				if (path instanceof SequencePath) {
					SequencePath sequence = (SequencePath) path;
					PropertyPath last = sequence.getLast();
					if (last instanceof PredicatePath) {
						URI pathPredicate = ((PredicatePath) last).getPredicate();
						
						PropertyInfo pathInfo = producePropertyInfo(pathPredicate);
						assertAlias(info, pathInfo);
					}
				}
			}
			
		}

		private void assertAlias(PropertyInfo a, PropertyInfo b) {
			
			if (a!=null && b!=null) {
				Set<PropertyInfo> aAlias = a.getAlias();
				Set<PropertyInfo> bAlias = b.getAlias();
				
				if (aAlias==null && bAlias==null) {
					Set<PropertyInfo> set = new HashSet<>();
					set.add(a);
					set.add(b);
					
					a.setAlias(set);
					b.setAlias(set);
				
				} else if (aAlias!=null && bAlias==null) {
					aAlias.add(b);
					b.setAlias(aAlias);
				} else if (aAlias==null && bAlias!=null) {
					bAlias.add(a);
					a.setAlias(bAlias);
				} else if (aAlias != bAlias) {
					// aAlias!=null && bAlias!=null
					
					Set<PropertyInfo> union = aAlias;
					union.addAll(bAlias);
					
					for (PropertyInfo info : union) {
						info.setAlias(union);
					}
					
				}
			}
			
		}

		private PropertyInfo producePropertyInfo(URI predicate) {
			
			PropertyInfo info = propertyInfo.get(predicate);
			if (info == null) {
				info = createPropertyInfo(predicate);
				
			}
			return info;
		}

		private void collectRdfProperties() {
			List<Vertex> propertyList = 
					graph.v(RDF.PROPERTY)
						.union(OWL.OBJECTPROPERTY, OWL.DATATYPEPROPERTY)
						.isIRI()
						.in(RDF.TYPE).toVertexList();
			
			for (Vertex property : propertyList) {

				URI predicate = (URI) property.getId();
				
				createPropertyInfo(predicate);
				
			}
			
		}

		private PropertyInfo createPropertyInfo(URI predicate) {
			Vertex property = graph.getVertex(predicate);
			if (property == null) {
				property = graph.vertex(predicate);
				graph.edge(predicate, RDF.TYPE, RDF.PROPERTY);
			}

			Vertex inverse = property.getVertex(OWL.INVERSEOF);
			if (inverse == null) {
				Set<Edge> inverseSet = property.inProperty(OWL.INVERSEOF);
				if (!inverseSet.isEmpty()) {
					inverse = graph.getVertex(inverseSet.iterator().next().getSubject());
				}
			}
			
			
			PropertyInfo info = new PropertyInfo(property, inverse);
			propertyInfo.put(predicate, info);
			return info;
		}

		private void computeRelationshipDegrees() {
			
			for (PropertyInfo info : propertyInfo.values()) {
				computeRelationshipDegree(info);
			}
			
		}

		private void computeRelationshipDegree(PropertyInfo info) {
			
			if (info.getRelationshipDegree() == null) {
				
				// TODO:  compute the relationship degree for the specified property,
				//        and propagate the result to all the aliases.
			}
			
			
		}
		
		
		
		
		
	}

}
