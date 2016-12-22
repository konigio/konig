package io.konig.schemagen.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Graph;
import io.konig.core.NamespaceManager;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.shacl.NodeKind;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.Shape;
import io.konig.shacl.ShapeManager;

public class DomainManager {
	
	private Map<String, Vertex> shapeMap = new HashMap<>();
	private Map<String, DomainClass> classMap = new HashMap<>();
	private Map<String, DomainProperty> propertyMap = new HashMap<>();
	private Graph ontology;
	private ShapeManager shapeManager;
	private NamespaceManager nsManager;
	
	
	
	public DomainManager(ShapeManager shapeManager, NamespaceManager nsManager) {
		this.shapeManager = shapeManager;
		this.nsManager = nsManager;
	}

	public DomainClass addOwlClass(Vertex v) {
		String key = v.getId().stringValue();
		DomainClass c = classMap.get(key);
		if (c == null) {
			c = new DomainClass(v);
			classMap.put(key, c);
		}
		return c;
	}
	
	public DomainProperty addProperty(Vertex property) {
		String key = property.getId().stringValue();
		DomainProperty p = propertyMap.get(key);
		if (p == null) {
			p = new DomainProperty(property);
			propertyMap.put(key, p);
		}
		return p;
	}
	
	public void load(Graph ontology) {
		this.ontology = ontology;
		loadClasses(ontology);
		loadShapes();
		loadProperties(ontology, OWL.OBJECTPROPERTY);
		buildClassHierarchy();
		
	}
	

	private void loadShapes() {
		for (Shape shape : shapeManager.listShapes()) {
			List<PropertyConstraint> propertyList = shape.getProperty();
			for (PropertyConstraint p : propertyList) {
				URI predicate = p.getPredicate();
				DomainProperty d = domainProperty(predicate);
				d.addShapeProperty(shape, p);
				analyze(p);
			}
		}
		
	}



	private void analyze(PropertyConstraint p) {
		owlClass(p.getValueClass());
		owlClass(p.getDirectValueType());
		owlClass(p.getShape());
	}

	private void owlClass(Shape shape) {
		if (shape != null) {
			owlClass(shape.getTargetClass());
		}
		
	}

	private void owlClass(Value valueClass) {
		if (valueClass instanceof URI) {
			Vertex v = ontology.vertex((URI)valueClass);
			addOwlClass(v);
		}
		
	}

	private DomainProperty domainProperty(URI predicate) {
		DomainProperty p = propertyMap.get(predicate.stringValue());
		if (p == null) {
			p = addProperty(ontology.vertex(predicate));
		}
		return p;
	}

	private void buildClassHierarchy() {
		List<DomainClass> list = new ArrayList<>(classMap.values());
		for (int i=0; i<list.size(); i++) {
			DomainClass c = list.get(i);
			List<Vertex> superList = c.getClassVertex().asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex v : superList) {
				Resource id = v.getId();
				if (id instanceof URI) {
					c.addSuperClass((URI)id);
					DomainClass s = classMap.get(id.stringValue());
					if (s == null) {
						addOwlClass(v);
					}
				}
			}
		}
		
	}

	private void loadProperties(Graph ontology, URI type) {
		
		List<Vertex> list = ontology.v(type).in(RDF.TYPE).toVertexList();
		for (Vertex v : list) {
			
			DomainProperty p = addProperty(v);
			
			Value domain = v.getValue(RDFS.DOMAIN);
			Value range = v.getValue(RDFS.RANGE);
			
			if (domain instanceof URI) {
				owlClass(domain);
				p.setDomain((URI) domain);
			}
			
			if (range instanceof URI) {
				owlClass(range);
				p.setRange((URI)range);
			}
		}
		
	}

	/**
	 * Export the domain model to a given graph
	 * @param sink The graph into which the domain model will be exported.
	 */
	public void export(Graph sink) {
		exportClasses(sink);
		exportProperties(sink);
	}
	
	public void exportPrototype(Graph sink) {
		exportClassPrototypes(sink);
		exportPropertyPrototypes(sink);
	}

	private void exportPropertyPrototypes(Graph sink) {
		List<DomainProperty> list = new ArrayList<>(propertyMap.values());
		for (DomainProperty p : list) {


			URI predicate = (URI) sink.vertex(p.getVertex().getId()).getId();
			
			URI domain = p.getDomain();
			URI range = p.getRange();
			
			if (domain!=null && range!=null) {
				sink.edge(domain, (URI) predicate, range);
			}
			
			
			exportPrototypeRestrictions(p, sink);
		}
		
	}

	private void exportPrototypeRestrictions(DomainProperty p, Graph sink) {
		List<ShapeProperty> list = p.getShapeProperyList();
		outer : for (ShapeProperty a : list) {
			URI domain = domainOf(a);
			
			if (domain != null) {
				Vertex v = ontology.vertex(domain);
				URI range = rangeOf(a);
				if (range==null || range.equals(p.getRange())) {
					continue;
				}
				for (ShapeProperty b : list) {
					if (b != a) {
						URI bRange = rangeOf(b);
						if (range.equals(bRange)) {
							URI bDomain = domainOf(b);
							if (bDomain != null && RdfUtil.isSubClassOf(v, bDomain)) {
								continue outer;
							}
						}
					}
				}
				URI predicate = a.getConstraint().getPredicate();
				
				sink.edge(domain, predicate, range);
			}
		}
		
	}

	private void exportClassPrototypes(Graph sink) {

		List<DomainClass> list = new ArrayList<>(classMap.values());
		for (DomainClass c : list) {
			
			Resource resource = c.getClassVertex().getId();
			if (resource instanceof URI) {
				URI subject = (URI) resource;
				String curie = RdfUtil.curie(nsManager, subject);
				sink.edge(subject, RDFS.LABEL, literal(curie));
				
				Set<URI> superList = c.getSuperClass();
				for (URI superClass : superList) {
					sink.edge(subject, RDFS.SUBCLASSOF, superClass);
				}
			}
			
			
			
		}
		
	}

	private Value literal(String curie) {
		return new LiteralImpl(curie);
	}

	private void exportProperties(Graph sink) {
		
		List<DomainProperty> list = new ArrayList<>(propertyMap.values());
		for (DomainProperty p : list) {

			if (p.isDatatypeProperty()) {
				continue;
			}

			URI property = (URI) sink.vertex(p.getVertex().getId()).getId();
			
			sink.edge(property, RDF.TYPE, OWL.OBJECTPROPERTY);
			
			URI domain = p.derivedDomain(ontology);
			if (domain != null) {
				sink.edge(property, RDFS.DOMAIN, domain);
			}
			
			URI range = p.derivedRange(ontology);
			if (range != null) {
				sink.edge(property, RDFS.RANGE, range);
			}
			
			exportRestrictions(p, sink);
		}
		
	}


	private void exportRestrictions(DomainProperty p, Graph sink) {
		
		List<ShapeProperty> list = p.getShapeProperyList();
		outer : for (ShapeProperty a : list) {
			URI domain = domainOf(a);
			
			if (domain != null) {
				Vertex v = ontology.vertex(domain);
				URI range = rangeOf(a);
				if (range==null || range.equals(p.getRange())) {
					continue;
				}
				for (ShapeProperty b : list) {
					if (b != a) {
						URI bRange = rangeOf(b);
						if (range.equals(bRange)) {
							URI bDomain = domainOf(b);
							if (bDomain != null && RdfUtil.isSubClassOf(v, bDomain)) {
								continue outer;
							}
						}
					}
				}
				URI predicate = a.getConstraint().getPredicate();
				Resource restriction = sink.vertex().getId();
				
				sink.edge(domain, RDFS.SUBCLASSOF, restriction);
				sink.edge(restriction, RDF.TYPE, OWL.RESTRICTION);
				sink.edge(restriction, OWL.ONPROPERTY, predicate);
				sink.edge(restriction, OWL.ALLVALUESFROM, range);
			}
		}
		
	}

	private URI domainOf(ShapeProperty a) {
		return a.getShape().getTargetClass();
	}

	private URI rangeOf(ShapeProperty s) {
		return rangeOf(s, ontology);
	}
	
	static URI rangeOf(ShapeProperty s, Graph ontology) {
		PropertyConstraint constraint = s.getConstraint();
		Resource valueClass = constraint.getValueClass();
		
		URI predicate = constraint.getPredicate();
		if (predicate.equals(Konig.id)) {
			return null;
		}
		
		if (RDF.TYPE.equals(predicate)) {
			return null;
		}
		
		Value range = ontology.vertex(predicate).getValue(RDFS.RANGE);
		
		if (valueClass instanceof URI) {
			return valueClass.equals(range) ? null : (URI) valueClass;
		}
		
		URI directType = constraint.getDirectValueType();
		if (directType != null) {
			return directType.equals(range) ? null : directType;
		}
		
		Shape valueShape = constraint.getShape();
		if (valueShape != null) {
			URI targetClass = valueShape.getTargetClass();
			if (targetClass != null) {
				return targetClass.equals(range) ? null : targetClass;
			}
		}
		
		if (range != null) {
			return null;
		}
		
		if (constraint.getNodeKind() == NodeKind.IRI || constraint.getNodeKind()==NodeKind.BlankNode) {
			return OWL.THING;
		}
		
		return null;
	}

	private void exportClasses(Graph sink) {
		List<DomainClass> list = new ArrayList<>(classMap.values());
		for (DomainClass c : list) {
			Resource subject = c.getClassVertex().getId();
			sink.edge(subject, RDF.TYPE, OWL.CLASS);
			
			Set<URI> superList = c.getSuperClass();
			for (URI superClass : superList) {
				sink.edge(subject, RDFS.SUBCLASSOF, superClass);
			}
			
		}
	}


	

	private void loadClasses(Graph ontology) {

		List<Vertex> classList = ontology.v(OWL.CLASS).in(RDF.TYPE).toVertexList();
		for (Vertex v : classList) {
			addOwlClass(v);
		}
		
	}

}
