package io.konig.schema;

import java.util.Set;

import org.openrdf.model.Resource;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.vocabulary.RDF;

import io.konig.core.Edge;
import io.konig.core.Graph;
import io.konig.core.NamespaceInfo;
import io.konig.core.NamespaceInfoManager;
import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.core.vocab.Konig;
import io.konig.core.vocab.OwlVocab;
import io.konig.core.vocab.Schema;

public class EnumerationReasoner {
	
	public void annotateEnumerationNamespaces(Graph graph, NamespaceInfoManager nim) {
		OwlReasoner reasoner = new OwlReasoner(graph);
		
		outer: for (NamespaceInfo ns : nim.listNamespaces()) {
			inner: for (URI term : ns.getTerms()) {
				Vertex v = graph.getVertex(term);
				if (v != null) {
					Resource id = v.getId();
					
					if (id instanceof URI) {

						if (reasoner.isSubClassOf(id, Schema.Enumeration)) {
							continue inner;
						}
						
						if (id.stringValue().equals(ns.getNamespaceIri())) {
							continue inner;
						}
	
						Set<Edge> typeSet = v.outProperty(RDF.TYPE);
	
						for (Edge e : typeSet) {
							Value value = e.getObject();
							if (value instanceof URI) {
								URI typeId = (URI) value;
								if (OwlVocab.NamedIndividual.equals(typeId)
										|| reasoner.isSubClassOf(typeId, Schema.Enumeration)) {
									continue inner;
								}
							}
						}
	
						// The current term is not a named individual or a subClass of schema:Enumeration
						continue outer;
					}
				}
			}

			// Every term in the namespace is either a subClass of
			// schema:Enumeration or a named individual.
			ns.getType().add(Konig.EnumNamespace);

		}
	}

}
