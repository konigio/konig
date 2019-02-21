package io.konig.core.extract;

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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.openrdf.model.BNode;
import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.impl.RdfUtil;
import io.konig.core.vocab.SH;
import io.konig.core.vocab.Schema;

/**
 * A utility that extracts the elements within an ontology from a broader graph.
 * @author Greg McFall
 *
 */
public class OntologyExtractor {

	/**
	 * Extract the terms within a given ontology and copy them into a target graph.
	 * @param ontology  The vertex representing the ontology to be extracted.
	 * @param target The target graph into which the elements of the ontology will be placed.
	 */
	public void extract(Vertex ontology, Graph target) throws ExtractException {
		Worker worker = new Worker(ontology, target);
		worker.run();
	}
	
	/**
	 * Get a list of the namespaces for shapes contained in a given graph.
	 * @param graph A graph containing sh:Shape nodes.
	 * @return The set of namespaces for all sh:Shape nodes in the graph.
	 */
	public Set<String> shapeNamespaces(Graph graph) {

		Set<String> set = new HashSet<>();
		List<Vertex> list = graph.v(SH.Shape).in(RDF.TYPE).toVertexList();
		for (Vertex v : list) {
			Resource id = v.getId();
			if (id instanceof URI) {
				URI uri = (URI) id;
				set.add(uri.getNamespace());
			}
		}
		return set;
	}
	
	/**
	 * Collect OWL Ontologies from a given graph, but exclude those from a supplied set.
	 * @param source The source graph from which OWL Ontologies will be collected.
	 * @param exclude The set of IRI values for ontologies to be excluded.
	 * @return The OWL Ontologies from the source graph that are not referenced in the exclude set.
	 */
	public Set<String> collectOwlOntologies(Graph source, Set<String> exclude) {
		Set<String> result = new HashSet<>();
		List<Vertex> list = source.v(OWL.ONTOLOGY).in(RDF.TYPE).isIRI().toVertexList();
		for (Vertex v : list) {
			Resource id = v.getId();
			String value = id.stringValue();
			if (!exclude.contains(value)) {
				result.add(value);
			}
		}
		
		return result;
	}
	
	public void collectShapeOntologies(Graph source, Set<String> namespaceSet, Graph target) {

		List<String> list = new ArrayList<>(namespaceSet);
		Collections.sort(list);
		
		for (String ns : list) {
			Vertex v = source.getVertex(new URIImpl(ns));
			RdfUtil.deepCopy(v, target);
		}
	}
	
	private class Worker {
		private Vertex ontology;
		private Graph source;
		private Graph target;
		private List<Vertex> classList = new ArrayList<>();
		private List<Vertex> propertyList = new ArrayList<>();
		private List<Vertex> namedIndividualList = new ArrayList<>();
		private List<Vertex> otherList = new ArrayList<>();
		private OwlReasoner reasoner;
		
		private Worker(Vertex ontology, Graph target) {
			this.ontology = ontology;
			this.target = target;
			source = ontology.getGraph();
			reasoner = new OwlReasoner(source);
		}
		
		private void run() throws ExtractException {
			copyOntologyAttributes();
			collectElements();
			copyList(classList);
			copyList(propertyList);
			copyList(namedIndividualList);
			copyList(otherList);
		}

		
		private void copyList(List<Vertex> list) {
			sortByLocalName(list);
			
			for (Vertex v : list) {
				copy(v);
			}
		}
		
		private void sortByLocalName(List<Vertex> list) {
			Collections.sort(list, new Comparator<Vertex>() {

				@Override
				public int compare(Vertex a, Vertex b) {
					String x = localName(a.getId());
					String y = localName(b.getId());
					
					return x.compareTo(y);
				}
			});
		}
		
		private String localName(Resource resource) {
			return resource instanceof URI ? ((URI)resource).getLocalName() : resource.stringValue();
		}
		
		private String namespace(Resource resource) {
			return resource instanceof URI ? ((URI)resource).getNamespace() : null;
		}

		private void collectElements() throws ExtractException {
			
			Resource id = ontology.getId();
			String namespace = namespace(id);
			if (namespace == null) {
				throw new ExtractException("The supplied ontology cannot be blank node");				
			}
			for (Vertex v : source.vertices()) {
				id = v.getId();
				if (id instanceof URI) {
					if (id.stringValue().startsWith(namespace)) {
						collectElement(v);
					}
				}
			}
			
		}

		private void collectElement(Vertex v) {
			
			if (
				v.hasProperty(RDF.TYPE, OWL.CLASS) ||
				v.hasProperty(RDF.TYPE, RDFS.CLASS)
			) {
				classList.add(v);
			} else if (
				v.hasProperty(RDF.TYPE, RDF.PROPERTY) ||
				v.hasProperty(RDF.TYPE, OWL.OBJECTPROPERTY) ||
				v.hasProperty(RDF.TYPE, OWL.DATATYPEPROPERTY) ||
				v.hasProperty(RDF.TYPE, OWL.INVERSEFUNCTIONALPROPERTY) 
			) {
				propertyList.add(v);
			} else if (reasoner.isEnumerationMember(v.getId())) {
				namedIndividualList.add(v);
			} else {
				otherList.add(v);
			}
			
		}

		private void copyOntologyAttributes() {
			
			copy(ontology);
			
		}
		
		private void copy(Vertex v) {
			
			Set<Edge> out = v.outEdgeSet();
			for (Edge e : out) {
				target.add(e);
				Value object = e.getObject();
				if (object instanceof BNode) {
					Vertex w = source.getVertex((Resource) object);
					copy(w);
				}
			}
			
		}
	}
}
