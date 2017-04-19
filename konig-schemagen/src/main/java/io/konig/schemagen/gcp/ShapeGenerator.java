package io.konig.schemagen.gcp;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.Edge;
import io.konig.core.KonigException;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;

public class ShapeGenerator {
	private static final int MAX_DEPTH = 10;
	
	private OwlReasoner reasoner;
	
	public ShapeGenerator(OwlReasoner reasoner) {
		this.reasoner = reasoner;
	}

	public Shape generateShape(List<Vertex> individuals) {
		
		Shape shape = new Shape();
		TypeInfo typeInfo = new TypeInfo();
		for (Vertex v : individuals) {
			Resource id = v.getId();
			NodeKind kind = (id instanceof URI) ? NodeKind.IRI : NodeKind.BlankNode;
			kind = NodeKind.or(shape.getNodeKind(), kind);
			shape.setNodeKind(kind);
			
			setTargetClass(shape, v);
			collectTypes(v, typeInfo);
			setProperties(shape, v, 0);
		}
		addTypeProperty(shape, typeInfo);
		
		if (OWL.THING.equals(shape.getTargetClass())) {
			shape.setTargetClass(null);
		}
		
		
		return shape;
	}

	

	private void addTypeProperty(Shape shape, TypeInfo typeInfo) {
		Set<URI> typeSet = typeInfo.typeSet;
		Set<URI> disjointTypes = reasoner.disjointTypes(typeSet);
		if (disjointTypes.size()>1) {
			PropertyConstraint p = new PropertyConstraint(RDF.TYPE);
			shape.add(p);
			if (typeInfo.maxCount==1) {
				p.setMaxCount(1);
			}
			p.setValueClass(OWL.CLASS);
			p.setNodeKind(NodeKind.IRI);
		}
		
	}

	private void collectTypes(Vertex v, TypeInfo typeInfo) {
		
		Set<URI> specificTypes = reasoner.specificTypes(v.getId());
		typeInfo.addTypes(specificTypes);
		
		
	}

	private void setTargetClass(Shape shape, Vertex v) {
		Resource targetClass = shape.getTargetClass();
		if (!OWL.THING.equals(targetClass)) {
			Set<Edge> set = v.outProperty(RDF.TYPE);
			for (Edge e : set) {
				Value object = e.getObject();
				if (object instanceof URI) {
					URI uri = (URI) object;
					if (targetClass == null) {
						targetClass = uri;
					} else {
						targetClass = reasoner.leastCommonSuperClass(targetClass, uri);
					}
				}
			}
			if (targetClass instanceof URI) {
				shape.setTargetClass((URI) targetClass);
			}
		}
		
		
	}

	private void setProperties(Shape shape, Vertex v, int depth) {
		if (depth > MAX_DEPTH) {
			throw new KonigException("Max depth exceeded");
		}
		depth++;
		Set<Entry<URI, Set<Edge>>> out = v.outEdges();
		
		for (Entry<URI, Set<Edge>> entry : out) {
			URI predicate = entry.getKey();
			if (RDF.TYPE.equals(predicate)) {
				continue;
			}
			Set<Edge> edges = entry.getValue();
			
			boolean newProperty = false;
			PropertyConstraint p = shape.getPropertyConstraint(predicate);
			if (p == null) {
				newProperty = true;
				p = new PropertyConstraint(predicate);
				shape.add(p);
			}
			
			if (newProperty) {
				if (edges.size()==1) {
					p.setMaxCount(1);
				}
			} else {
				if (edges.size()>1) {
					p.setMaxCount(null);
				}
			}
			
			for (Edge e : edges) {
				Value value = e.getObject();
				if (value instanceof Literal) {
					Literal literal = (Literal) value;
					URI datatype = literal.getDatatype();
					if (datatype == null) {
						datatype = XMLSchema.STRING;
					}
					URI priorDatatype = p.getDatatype();
					if (priorDatatype!=null && !priorDatatype.equals(datatype)) {
						datatype = RDFS.LITERAL;
					}
					p.setDatatype(datatype);
				} else {
					Resource objectId = (Resource) value;
					Vertex object = v.getGraph().getVertex(objectId);
					if (object == null) {
						throw new KonigException("Object not found: " + objectId);
					}
					
					URI valueClass = mostSpecificType(predicate, object, p.getValueClass());
					p.setValueClass(valueClass);
					
					NodeKind objectKind = objectId instanceof URI ? NodeKind.IRI : NodeKind.BlankNode;
					NodeKind priorKind = p.getNodeKind();
					
					objectKind = NodeKind.or(objectKind, priorKind);
					p.setNodeKind(objectKind);
					
					if (objectId instanceof BNode) {
						setShape(p, object, depth);
					}
				}
			}
		}
		
	}

	private void setShape(PropertyConstraint p, Vertex object, int depth) {
		
		Shape shape = p.getShape();
		if (shape == null) {
			shape = new Shape();
			p.setShape(shape);
		}
		setProperties(shape, object, depth);
	}

	private URI mostSpecificType(URI predicate, Vertex object, Resource prior) {
		
		URI mostSpecificType = (prior instanceof URI) ? (URI) prior : null;
		Set<Edge> typeSet = object.outProperty(RDF.TYPE);
		for (Edge e : typeSet) {
			Value value = e.getObject();
			if (value instanceof URI) {
				URI uri = (URI) value;
				if (mostSpecificType == null) {
					mostSpecificType = uri;
				} else {
					if (reasoner.isSubClassOf(uri, mostSpecificType)) {
						mostSpecificType = uri;
					} else if (!reasoner.isSubClassOf(mostSpecificType, uri)) {
						StringBuilder msg = new StringBuilder();
						msg.append("Multiple types not supported. Predicate <");
						msg.append(predicate.stringValue());
						msg.append("> has a range that includes <");
						msg.append(mostSpecificType.stringValue());
						msg.append("> and <");
						msg.append(uri.stringValue());
						msg.append(">");
						
						throw new KonigException(msg.toString());
					}
				}
			}
		}
		
		return mostSpecificType;
	}
	
	static class TypeInfo {
		Set<URI> typeSet = new HashSet<>();
		int maxCount = 0;
		public void addTypes(Set<URI> specificTypes) {
			typeSet.addAll(specificTypes);
			maxCount = Math.max(maxCount, specificTypes.size());
		}
	}

}
