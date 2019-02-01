package io.konig.core.showl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.impl.BNodeImpl;

import io.konig.core.OwlReasoner;
import io.konig.core.vocab.Konig;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class ShowlTargetClassReasoner {

	private static int nodeCount = 0;
	public void inferTargetClasses(ShapeManager shapeManager, OwlReasoner reasoner) {
		Worker worker = new Worker(shapeManager, reasoner);
		worker.run();
	}
	
	private static class Worker {
		private ShapeManager shapeManager;
		private OwlReasoner reasoner;
		private Set<Node> classlessNodes=new HashSet<>();
		private Map<Resource,Node> nodeMap = new HashMap<>();
		
		public Worker(ShapeManager shapeManager, OwlReasoner reasoner) {
			this.shapeManager = shapeManager;
			this.reasoner = reasoner;
		}
		
		public void run() {
			buildGraph();
		}
		
		private void buildGraph() {
			for (Shape shape : shapeManager.listShapes()) {
				Node node = node(shape);
				buildEdges(node);
				
			}
			
		}
		
		private void buildEdges(Node node) {
			
			
			
		}

		private BNode createBNode() {
			String id = "stcr" + (++nodeCount);
			return new BNodeImpl(id);
		}

		private Node node(Shape shape) {
			Resource shapeId = shape.getId();
			if (shapeId == null) {
				shapeId = createBNode();
				shape.setId(shapeId);
			}
			Node node = nodeMap.get(shapeId);
			if (node == null) {
				node = new Node(shape);
				nodeMap.put(shapeId, node);
			}
			return node;
		}

		private URI targetClass(Node node, Set<Node> filter) {
			URI targetClass = node.getTargetClass();
			if (targetClass == null) {
				targetClass = inferTargetClass(node, filter);
			}
			return targetClass;
		}
		
		private URI inferTargetClass(Node node, Set<Node> filter) {
			if (filter.contains(node)) {
				return null;
			}
			filter.add(node);
			
			Set<URI> candidates = new HashSet<>();
			
			for (Edge edge : node.getOutbound()) {
				Set<URI> domainIncludes = edge.getPredicate().domainIncludes(this, filter);
				
				outer : for (URI domain : domainIncludes) {
					for (URI candidate : candidates) {
						if (reasoner.isSubClassOf(candidate, domain)) {
							// Ignore domain because it is a superclass of an existing
							// candidate.
							continue outer;
						}
					}
					
					candidates.add(domain);
				}
				
			}
			
			URI result = null;
			if (candidates.size()==1) {
				result = candidates.iterator().next();
				node.setTargetClass(result);
			}
			
			return result;
			
		}


		private URI targetClass(Shape shape) {
			URI targetClass = shape.getTargetClass();
			if (targetClass == null) {
				targetClass = Konig.Undefined;
			}
			return targetClass;
		}

		
		
		
	}
	
	static class Node {
		private Resource id;
		private URI targetClass;
		private Shape shape;
		private Set<Edge> outbound = new HashSet<>();		
		private Set<Edge> inbound = new HashSet<>();
		
		

		public Node(Shape shape) {
			this.shape = shape;
			id = shape.getId();
			targetClass = shape.getTargetClass();
		}
		
		@Override
		public int hashCode() {
			return id.hashCode();
		}

		public URI getTargetClass() {
			return targetClass;
		}

		public Shape getShape() {
			return shape;
		}

		public Set<Edge> getOutbound() {
			return outbound;
		}

		public Set<Edge> getInbound() {
			return inbound;
		}
		
		public void setTargetClass(URI targetClass) {
			this.targetClass = targetClass;
			if (shape != null) {
				shape.setTargetClass(targetClass);
			}
		}
		
		
	}
	
	static class Edge {
		private Node subject;
		private Property predicate;	
		private Node object;

		public Node getSubject() {
			return subject;
		}

		public Property getPredicate() {
			return predicate;
		}

		public Node getObject() {
			return object;
		}
		
	}
	
	static class Property {
		private URI id;
		private URI domain;
		private URI range;
		private Set<Edge> edges;
		
		public Set<URI> domainIncludes(Worker worker, Set<Node> filter) {
			Set<URI> result = new HashSet<>();
			if (domain != null) {
				result.add(domain);
			} else {
				for (Edge e : edges) {
					URI targetClass = worker.targetClass(e.getSubject(), filter);
					if (targetClass != null) {
						result.add(targetClass);
					}
				}
			}
			return result;
		}
		
		
	}
	
	
	

}
