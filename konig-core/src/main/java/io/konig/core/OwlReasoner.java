package io.konig.core;

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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlSchema;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.vocab.KOL;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.XSD;

public class OwlReasoner {
	
	private Graph graph;
	private Map<String, EquivalenceClass> equivalentClassMap;
	private Map<String, DatatypeRestriction> datatypeMap;
	
	public OwlReasoner(Graph graph) {
		this.graph = graph;
	}
	
	public Graph getGraph() {
		return graph;
	}
	
	/**
	 * Ensure that every member within a set of equivalent classes contains 
	 * an owl:equivalentClass property whose value is the preferred class.
	 */
	private void buildEquivalentClasses() {
		if (equivalentClassMap == null) {
			List<Vertex> list = graph.v(OWL.CLASS).union(RDFS.CLASS).distinct().in(RDF.TYPE).distinct().toVertexList();
			equivalentClassMap = new HashMap<>();
			
			assembleEquivalenceClasses(list, equivalentClassMap);
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
			Vertex result =  e.getPreferredEntity(KOL.PreferredClass);
			if (result != null) {
				owlClass = result;
			}
		}
		
		return owlClass;
	}
	
	public Set<Vertex> equivalentClasses(Resource owlClass) {
		buildEquivalentClasses();

		EquivalenceClass e = equivalentClassMap.get(owlClass.stringValue());
		return e.getMembers();
	}
	
	public Vertex preferredClass(Resource owlClass) throws AmbiguousPreferredClassException {
		return preferredClass(graph.vertex(owlClass));
	}

	public URI preferredClassAsURI(URI owlClass) throws AmbiguousPreferredClassException {
		return (URI) preferredClass(graph.vertex(owlClass)).getId();
	}
	
	public void inferClassFromSubclassOf() {
		List<Vertex> list = graph.v(OWL.CLASS).in(RDF.TYPE).out(RDFS.SUBCLASSOF).toVertexList();
		for (Vertex v : list) {
			Resource subject = v.getId();
			if (subject instanceof URI) {
				graph.edge(subject, RDF.TYPE, OWL.CLASS);
			}
		}
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
}
