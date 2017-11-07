package io.konig.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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


import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.openrdf.model.vocabulary.XMLSchema;

import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;
import io.konig.core.vocab.XSD;

public class OwlReasoner {
	
	private Graph graph;
	private Map<String, EquivalenceClass> equivalentClassMap;
	private Map<String, DatatypeRestriction> datatypeMap;
	private boolean inferredClassesFromSubclass;
	
	public OwlReasoner(Graph graph) {
		this.graph = graph;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	public List<Vertex> owlClassList() {
		return graph.v(OWL.CLASS).in(RDF.TYPE).toVertexList();
	}
	
	public List<Vertex> ontologyList() {
		return graph.v(OWL.ONTOLOGY).in(RDF.TYPE).toVertexList();
	}
	
	public List<Vertex> subClasses(Vertex v) {
		return v.asTraversal().in(RDFS.SUBCLASSOF).toVertexList();
	}
	
	public Set<URI> subClasses(URI classId) {
		return graph.v(classId).in(RDFS.SUBCLASSOF).toUriSet();
	}
	
	public Set<Vertex> allSubClasses(Vertex v) {
		Set<Vertex> set = new HashSet<>();
		addSubClasses(set, subClasses(v));
		return set;
	}
	
	private void addSubClasses(Set<Vertex> set, List<Vertex> subClasses) {
		for (Vertex v : subClasses) {
			if (!set.contains(v)) {
				set.add(v);
				addSubClasses(set, subClasses(v));
			}
		}
		
	}

	public Set<URI> namedSubClasses(Resource typeId) {
		Vertex v = graph.getVertex(typeId);
		if (v == null) {
			return new HashSet<URI>();
		}
		return v.asTraversal().in(RDFS.SUBCLASSOF).toUriSet();
	}
	
	public Set<URI> allNamedSubClasses(Resource typeId) {
		Vertex v = graph.getVertex(typeId);
		if (v == null) {
			return new HashSet<URI>();
		}
		Set<URI> result = new HashSet<>();
		Set<Vertex> vertices = allSubClasses(v);
		for (Vertex u : vertices) {
			if (u.getId() instanceof URI) {
				result.add((URI) u.getId());
			}
		}
		return result;
		
	}
	
	

	/**
	 * Ensure that every member within a set of equivalent classes contains 
	 * an owl:equivalentClass property whose value is the preferred class.
	 */
	private void buildEquivalentClasses() {
		if (equivalentClassMap == null) {
			inferClassFromSubclassOf();
			List<Vertex> list = graph.v(OWL.CLASS)
				.union(RDFS.CLASS).union(RDFS.DATATYPE).union(Schema.DataType).union(RDFS.LITERAL)
				.distinct().in(RDF.TYPE).distinct().toVertexList();
			equivalentClassMap = new HashMap<>();
			
			assembleEquivalenceClasses(list, equivalentClassMap);
		}
	}
	
	public void inferRdfPropertiesFromPropertyConstraints(Graph sink) {
		if (sink == null) {
			sink = graph;
		}
		
		List<Vertex> shapeList = graph.v(SH.Shape).in(RDF.TYPE).toVertexList();
		
		for (Vertex shape : shapeList) {
			URI targetClass = shape.getURI(SH.targetClass);
			List<Vertex> constraintList = shape.asTraversal().out(SH.property).toVertexList();
		
			for (Vertex p : constraintList) {
				Vertex property = p.getVertex(SH.path);
				URI predicate = p.getURI(SH.path);
				if (predicate != null) {
					
					URI propertyType = property.getURI(RDF.TYPE);
					URI range = property.getURI(RDFS.RANGE);
					URI domain = property.getURI(RDFS.RANGE);
					if (domain != null) {
						targetClass = null;
					}
					
					URI datatype =  p.getURI(SH.datatype);
					if (datatype != null) {
						URI type = propertyType==null ? OWL.DATATYPEPROPERTY : null;
						edge(sink, predicate, RDF.TYPE, type);
						edge(sink, predicate, Schema.domainIncludes, targetClass);
						edge(sink, predicate, Schema.rangeIncludes, datatype);
					} else {
						URI valueClass = range==null ? p.getURI(SH.valueClass) : null;
						URI type = propertyType==null ? OWL.OBJECTPROPERTY : null;
						if (valueClass != null) {
							edge(sink, predicate, RDF.TYPE, type);
							edge(sink, predicate, Schema.domainIncludes, targetClass);
							edge(sink, predicate, Schema.rangeIncludes, valueClass);
						} else {
							Vertex valueShape = p.getVertex(SH.shape);
							if (valueShape != null) {
								valueClass = valueShape.getURI(SH.targetClass);
								if (valueClass != null) {
									edge(sink, predicate, RDF.TYPE, type);
									edge(sink, predicate, Schema.domainIncludes, targetClass);
									edge(sink, predicate, Schema.rangeIncludes, valueClass);
								} else {
									edge(sink, predicate, RDF.TYPE, type);
									edge(sink, predicate, Schema.domainIncludes, targetClass);
								}
							} else {
								edge(sink, predicate, RDF.TYPE, type);
								edge(sink, predicate, Schema.domainIncludes, targetClass);
							}
						}
					}
				}
			}
		}
		
	}
	
	private void edge(Graph sink, Resource subject, URI predicate, Value object) {
		if (sink!=null && subject!=null && predicate!=null && object!=null) {
			sink.edge(subject, predicate, object);
		}
	}


	private void assembleEquivalenceClasses(List<Vertex> list, Map<String, EquivalenceClass> map) {
		for (Vertex owlClass : list) {
			analyzeEquivalentClasses(owlClass, map);
		}
	}

	/**
	 * Compute the transitive closure of the equivalent classes of the specified owlClass
	 * @param owlClass  The class from which the transitive closure will be computed
	 * @param map A map from the id for an owlClass to the EquivalenceClass to which it belongs.
	 */
	private void analyzeEquivalentClasses(Vertex owlClass, Map<String, EquivalenceClass> map) {
		
		String key = owlClass.getId().stringValue();
		EquivalenceClass e = map.get(key);
		if (e == null) {
			e = new EquivalenceClass();
			map.put(key, e);
			
			addMembers(e, owlClass, map);
		}
		
	}


	private void addMembers(EquivalenceClass e, Vertex owlClass, Map<String, EquivalenceClass> map) {
		
		Set<Vertex> set = e.getMembers();
		set.add(owlClass);
		List<Vertex> list = owlClass.asTraversal().out(OWL.EQUIVALENTCLASS).distinct().toVertexList();
		for (Vertex v : list) {
			String otherKey = v.getId().stringValue();
			EquivalenceClass other = map.get(otherKey);
			
			if (other == null) {
				map.put(otherKey, e);
				set.add(v);
				addMembers(e, v, map);
				
			} else if (other != e) {
				map.put(otherKey, e);
				e.addAll(other);
			}
		}
		
	}


	private static class EquivalenceClass {
		private Set<Vertex> members = new HashSet<>();
		private Vertex preferredEntity;
		private boolean computedPreferredEntity;
		
		public Vertex getPreferredEntity(URI preferredType) throws AmbiguousPreferredClassException {
			if (!computedPreferredEntity) {
				computedPreferredEntity = true;
				
				for (Vertex v : members) {
					if (v.hasProperty(RDF.TYPE, preferredType)) {
						if (preferredEntity == null) {
							preferredEntity = v;
						} else {
							throw new AmbiguousPreferredClassException(members);
						}
					}
				}
				
			}
			
			return preferredEntity;
		}
		
		
		public Set<Vertex> getMembers() {
			return members;
		}
		
		public void addAll(EquivalenceClass other) {
			members.addAll(other.getMembers());
		}
		
		
		
	}


	/**
	 * Identify the preferred class from the set of classes equivalent to a given class.
	 * @param owlClass The given OWL class that may or may not have equivalent classes.
	 * @return If there are classes equivalent to the given owlClass, then return the
	 * member from the set of equivalent classes that contains the kol:isPreferredClass 
	 * with the value 'true'.
	 * @throws AmbiguousPreferredClassException 
	 */
	public Vertex preferredClass(Vertex owlClass) throws AmbiguousPreferredClassException {
		buildEquivalentClasses();
		EquivalenceClass e = equivalentClassMap.get(owlClass.getId().stringValue());
		if (e != null) {
			Vertex result =  e.getPreferredEntity(Konig.PreferredClass);
			if (result != null) {
				owlClass = result;
			}
		}
		
		return owlClass;
	}
	
	public boolean isDatatype(Resource id) {
		if (id instanceof URI) {
			URI uri = (URI) id;
			if (XMLSchema.NAMESPACE.equals(uri.getNamespace())) {
				return true;
			}
		}
		Vertex v = graph.getVertex(id);
		if (v != null) {
			boolean truth = !v.asTraversal().hasValue(RDF.TYPE, RDFS.DATATYPE).toVertexList().isEmpty();
			if (truth) {
				return true;
			}
			// TODO: apply other kinds of inference
		}
		return false;
	}
	
	public Set<URI> valueType(Vertex predicate) {
		
		Set<URI> set = new HashSet<>();
		set.addAll(predicate.asTraversal().out(RDFS.RANGE).toUriSet());
		set.addAll(predicate.asTraversal().in(SH.path).out(SH.datatype).toUriSet());
		set.addAll(predicate.asTraversal().in(SH.path).out(SH.valueClass).toUriSet());
		set.addAll(predicate.asTraversal().in(SH.path).out(SH.shape).out(SH.targetClass).toUriSet());
		
		
		return set;
	}

	/**
	 * Compute the least common super datatype between two given datatypes.
	 * @param aType The first class
	 * @param bType The second class
	 * @return The least common super class between the two classes.
	 */
	public Resource leastCommonSuperDatatype(Resource aType, Resource bType) {
		Vertex a = graph.vertex(aType);
		Vertex b = graph.vertex(bType);
		if (RdfUtil.isSubClassOf(a, bType)) {
			return bType;
		}
		if (RdfUtil.isSubClassOf(b, aType)) {
			return aType;
		}
		
		Set<String> set = superClasses(a);
		List<Vertex> stack = b.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
		for (int i=0; i<stack.size(); i++) {
			Vertex w = stack.get(i);
			Resource id = w.getId();
			String key = id.stringValue();
			if (set.contains(key)) {
				return (URI) id;
			}
			
		}
		
		
		return RDFS.DATATYPE;
	}
	
	/**
	 * Compute the least common super class between two given classes.
	 * @param aClass The first class
	 * @param bClass The second class
	 * @return The least common super class between the two classes.
	 */
	public Resource leastCommonSuperClass(Resource aClass, Resource bClass) {
		
		if (aClass==null) {
			return bClass;
		}
		if (bClass==null) {
			return aClass;
		}
		
		if (aClass.equals(bClass)) {
			return aClass;
		}
		Vertex a = graph.vertex(aClass);
		Vertex b = graph.vertex(bClass);
		if (RdfUtil.isSubClassOf(a, bClass)) {
			return bClass;
		}
		if (RdfUtil.isSubClassOf(b, aClass)) {
			return aClass;
		}
		
		Set<String> set = superClasses(a);
		List<Vertex> stack = b.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
		for (int i=0; i<stack.size(); i++) {
			Vertex w = stack.get(i);
			Resource id = w.getId();
			String key = id.stringValue();
			if (set.contains(key)) {
				return (URI) id;
			}
		}
		
		
		return OWL.THING;
	}
	
	
	
	private Set<String> superClasses(Vertex a) {
		Set<String> set = new HashSet<>();
		List<Vertex> stack = new ArrayList<>();
		stack.addAll(equivalentClasses(a.getId()));
		
		for (int i=0; i<stack.size(); i++) {
			Vertex w = stack.get(i);
			List<Vertex> next = w.asTraversal().out(RDFS.SUBCLASSOF).toVertexList();
			for (Vertex v : next) {
				Set<Vertex> eq = equivalentClasses(v.getId());
				for (Vertex u : eq) {
					if (!stack.contains(u)) {
						stack.add(u);
						if (u.getId() instanceof URI) {
							set.add(u.getId().stringValue());
						}
					}
				}
			}
		}
		
		return set;
	}
	
	/**
	 * Check whether one type is a subClassOf another.
	 * @param a The identifier for one OWL Class
	 * @param b The identifier for another OWL Class.
	 * @return True if a and b are equal or a is a subClassOf b (or one of b's ancestors).
	 */
	public boolean isSubClassOf(Resource a, Resource b) {
		if (a.equals(b)) {
			return true;
		}
		Vertex va = graph.getVertex(a);
		if (va == null) {
			return false;
		}
		return RdfUtil.isSubClassOf(va, b);
	}

	public Set<Vertex> equivalentClasses(Resource owlClass) {
		buildEquivalentClasses();

		EquivalenceClass e = equivalentClassMap.get(owlClass.stringValue());
		if (e == null) {
			Vertex v = graph.vertex(owlClass);
			Set<Vertex> result = new HashSet<>();
			result.add(v);
			return result;
		}
		return e.getMembers();
	}
	
	public Vertex preferredClass(Resource owlClass) throws AmbiguousPreferredClassException {
		return preferredClass(graph.vertex(owlClass));
	}

	public URI preferredClassAsURI(URI owlClass) throws AmbiguousPreferredClassException {
		return (URI) preferredClass(graph.vertex(owlClass)).getId();
	}
	
	public void inferTypeOfSuperClass(Vertex thing) {
		Set<URI> typeSet = thing.asTraversal().out(RDF.TYPE).toUriSet();
		
		Set<URI> superSet = new HashSet<>();
		for (URI uri : typeSet) {
			getTransitiveClosure(uri, RDFS.SUBCLASSOF, superSet);
		}
		
		for (URI superClass : superSet) {
			graph.edge(thing.getId(), RDF.TYPE, superClass);
		}
		
	}
	
	public void getTransitiveClosure(Resource source, URI predicate, Set<URI> sink) {
		Vertex v = graph.getVertex(source);
		if (v != null) {
			Set<Edge> out = v.outProperty(predicate);
			for (Edge e : out) {
				Value object = e.getObject();
				if (object instanceof URI) {
					URI uri = (URI) object;
					if (!sink.contains(uri)) {
						sink.add(uri);
						getTransitiveClosure(uri, predicate, sink);
					}
				}
			}
		}
	}
	
	public URI mostSpecificType(Iterable<URI> collection, URI filter) {
		URI best = null;
		for (URI candidate : collection) {
			
			if (filter != null && !candidate.equals(filter) && !isSubClassOf(candidate, filter)) {
				continue;
			}
			
			if (best == null) {
				best = candidate;
			} else if (isSubClassOf(candidate, best)) {
				best = candidate;
			} 
		}
		
		return best;
	}
	
	public void inferClassFromSubclassOf() {
		if (!inferredClassesFromSubclass) {
			inferredClassesFromSubclass = true;
			List<Edge> list = new ArrayList<>(graph);
			for (Edge e : list) {
				URI predicate = e.getPredicate();
				if (predicate.equals(RDFS.SUBCLASSOF)) {
					graph.edge(e.getSubject(), RDF.TYPE, OWL.CLASS);
					graph.edge((Resource) e.getObject(), RDF.TYPE, OWL.CLASS);
				}
			}
		}
	}
	
	/**
	 * Test whether a given Resource is an instance of a given OWL Class.
	 * This method checks the rdf:type relationship, and infers based on rdfs:subClassOf relationships.
	 * 
	 * @param subject The Resource whose type is to be tested.
	 * @param owlClass The OWL Class to be matched
	 * @return True if the subject is an instance of the specified owlClass and false otherwise.
	 */
	public boolean instanceOf(Resource subject, URI owlClass) {
		Vertex v = graph.getVertex(subject);
		if (v != null) {
			Set<URI> typeSet = v.asTraversal().out(RDF.TYPE).toUriSet();
			for (URI type : typeSet) {
				if (type.equals(owlClass)) {
					return true;
				}
			}
			
			Set<URI> superTypeSet = new HashSet<>();
			for (URI type : typeSet) {
				getTransitiveClosure(type, RDFS.SUBCLASSOF, superTypeSet);
			}
			
			for (URI superType : superTypeSet) {
				if (superType.equals(owlClass)) {
					return true;
				}
			}
		}
		return false;
	}
	
	public boolean isRealNumber(URI owlClass) {
		return 
				XMLSchema.DECIMAL.equals(owlClass) ||
				XMLSchema.DOUBLE.equals(owlClass) ||
				XMLSchema.FLOAT.equals(owlClass) ||
				Schema.Float.equals(owlClass) ||
				Schema.Number.equals(owlClass);
	}
	
	public boolean isBooleanType(URI owlClass) {
		return
				XMLSchema.BOOLEAN.equals(owlClass) ||
				Schema.Boolean.equals(owlClass);
	}
	
	public boolean isPlainLiteral(URI owlClass) {
		return
				XMLSchema.STRING.equals(owlClass) ||
				Schema.Text.equals(owlClass);
	}
	
	public boolean isInverseFunctionalProperty(URI owlClass) {
		return instanceOf(owlClass, OWL.INVERSEFUNCTIONALPROPERTY);
	}
	
	public boolean isNumericDatatype(URI owlClass) {
		return isIntegerDatatype(owlClass) || isRealNumber(owlClass);
	}
	
	public boolean isIntegerDatatype(URI owlClass) {
		return
				XMLSchema.BYTE.equals(owlClass) ||
				XMLSchema.INT.equals(owlClass) ||
				XMLSchema.INTEGER.equals(owlClass) ||
				XMLSchema.LONG.equals(owlClass) ||
				XMLSchema.NEGATIVE_INTEGER.equals(owlClass) ||
				XMLSchema.NON_NEGATIVE_INTEGER.equals(owlClass) ||
				XMLSchema.NON_POSITIVE_INTEGER.equals(owlClass) ||
				XMLSchema.SHORT.equals(owlClass) ||
				XMLSchema.UNSIGNED_BYTE.equals(owlClass) ||
				XMLSchema.UNSIGNED_INT.equals(owlClass) ||
				XMLSchema.UNSIGNED_LONG.equals(owlClass) ||
				XMLSchema.UNSIGNED_SHORT.equals(owlClass) ||
				Schema.Integer.equals(owlClass);
			
	}
	
	public boolean isEnumerationClass(Resource owlClass) {
		return isSubClassOf(owlClass, Schema.Enumeration);
	}
	
	/**
	 * Check whether a given individual is an instance of a given OWL Class.
	 * @param individual The individual to be checked.
	 * @param owlClass The target OWL Class.
	 * @return True if the individual is an instance of the OWL Class, and false otherwise.
	 */
	public boolean isTypeOf(Resource individual, Resource owlClass) {
		Vertex v = graph.getVertex(individual);
		if (v != null) {
			List<Vertex> typeList = v.asTraversal().out(RDF.TYPE).toVertexList();
			for (Vertex type : typeList) {
				if (type.getId().equals(owlClass)) {
					return true;
				}
			}
			
			for (Vertex type : typeList) {
				Resource typeId = type.getId();
				if (isSubClassOf(typeId, owlClass)) {
					return true;
				}
			}
		}
		return false;
	}

	public DatatypeRestriction datatypeRestriction(URI datatype) {
		getDatatypeMap();
		DatatypeRestriction result = datatypeMap.get(datatype.stringValue());
		if (result == null) {
			result = createDatatypeRestriction(datatype);
			datatypeMap.put(datatype.stringValue(), result);
		}
		return result;
	}
	
	private DatatypeRestriction createDatatypeRestriction(URI datatype) {
		
		Vertex v = graph.vertex(datatype);
		
		DatatypeRestriction r = new DatatypeRestriction();

		r.setOnDatatype(asURI(v.getValue(OwlVocab.onDatatype)));
		
		Vertex withRestrictions = v.vertexValue(OwlVocab.withRestrictions);
		if (withRestrictions != null) {
			List<Value> list = withRestrictions.asList();
			
			for (Value value : list) {
				if (value instanceof Resource) {
					Vertex w = graph.vertex((Resource)value);

					Double maxExclusive = w.doubleValue(XSD.maxExclusive);
					Double maxInclusive = w.doubleValue(XSD.maxInclusive);
					Integer maxLength = w.integerValue(XSD.maxLength);
					Double minExclusive = w.doubleValue(XSD.minExclusive);
					Double minInclusive = w.doubleValue(XSD.minInclusive);
					String pattern = w.stringValue(XSD.pattern);
					if (maxExclusive != null) {
						r.setMaxExclusive(maxExclusive);
					}
					if (maxInclusive != null) {
						r.setMaxInclusive(maxInclusive);
					}
					if (maxLength != null) {
						r.setMaxLength(maxLength);
					}
					if (minExclusive != null) {
						r.setMinExclusive(minExclusive);
					}
					if (minInclusive != null) {
						r.setMinInclusive(minInclusive);
					}
					if (pattern != null) {
						r.setPattern(pattern);
					}
				}
			}
		}
		
		
		return r;
	}

	

	private URI asURI(Value value) {
		return value instanceof URI ? (URI)value : null;
	}

	private Map<String, DatatypeRestriction> getDatatypeMap() {
		if (datatypeMap == null) {
			datatypeMap = new HashMap<>();
		}
		return datatypeMap;
	}

	public Set<URI> superClasses(URI targetClass) {
		Vertex v = graph.getVertex(targetClass);
		if (v == null) {
			return Collections.emptySet();
		}
		return v.asTraversal().out(RDFS.SUBCLASSOF).toUriSet();
	}
	
	public Set<URI> disjointTypes(Collection<URI> types) {
		Set<URI> result = new HashSet<>();
		
		outerLoop :
		for (URI a : types) {
			Iterator<URI> sequence = result.iterator();
			
			while (sequence.hasNext()) {
				URI b = sequence.next();
				
				
				if (isSubClassOf(a, b)) {
					sequence.remove();
					break;
				}
				if (isSubClassOf(b, a)) {
					continue outerLoop;
				}
			}
			
			result.add(a);
			
		}
		
		return result;
	}
	
	/**
	 * Compute the specific types of a given individual.
	 * The returned set does not include any class that is a superclass of some other member of the set.
	 */
	public Set<URI> specificTypes(Resource individual) {
		Set<URI> result = null;
		Vertex v = graph.getVertex(individual);
		if (v != null) {
			Set<Edge> edges = v.outProperty(RDF.TYPE);
			Set<URI> typeSet = new HashSet<>();
			for (Edge e : edges) {
				Value value = e.getObject();
				if (value instanceof URI) {
					typeSet.add((URI)value);
				}
			}
			result = disjointTypes(typeSet);
		} else {
			result = new HashSet<>();
		}
		
		return result;
	}

	public boolean isEnumerationMember(Resource id) {
		return isTypeOf(id, Schema.Enumeration);
	}

	public boolean isProperty(URI target) {
		return 
			isTypeOf(target, RDF.PROPERTY) ||
			isTypeOf(target, OWL.FUNCTIONALPROPERTY) ||
			isTypeOf(target, OWL.DATATYPEPROPERTY) ||
			isTypeOf(target, OWL.OBJECTPROPERTY) || 
			isTypeOf(target, OWL.ANNOTATIONPROPERTY) ||
			isTypeOf(target, OWL.DEPRECATEDPROPERTY) ||
			isTypeOf(target, OWL.EQUIVALENTPROPERTY) ||
			isTypeOf(target, OWL.INVERSEFUNCTIONALPROPERTY) ||
			isTypeOf(target, OWL.ONTOLOGYPROPERTY) ||
			isTypeOf(target, OWL.SYMMETRICPROPERTY) || 
			isTypeOf(target, OWL.TRANSITIVEPROPERTY);
	}
}
