package io.konig.core.vocab;

import java.util.HashSet;
import java.util.Set;

/*
 * #%L
 * Konig Core
 * %%
 * Copyright (C) 2015 - 2018 Gregory McFall
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


import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.model.vocabulary.OWL;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.model.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.konig.core.OwlReasoner;
import io.konig.core.Vertex;
import io.konig.shacl.PropertyConstraint;
import io.konig.shacl.ShapePropertyPair;
import io.konig.shacl.ShapeReasoner;
import io.konig.shacl.ShapeReasoner.PropertyInfo;

public class XOWL {
	private static Logger logger = LoggerFactory.getLogger(XOWL.class);
	public static final URI Stable = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusStable");
	public static final URI Experimental = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusExperimental");
	public static final URI Invalid = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusInvalid");
	public static final URI Superseded = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusSuperseded");
	public static final URI Retired = new URIImpl("http://schema.pearson.com/ns/xowl/termStatus/statusRetired");
	
	/**
	 * Classify all properties as either owl:DatatypeProperty or owl:ObjectProperty
	 * @param reasoner
	 */
	public static void classifyProperties(OwlReasoner owlReasoner, ShapeReasoner shapeReasoner) {
		
		Set<Vertex> vertices = owlReasoner.allRdfAndOwlProperties();
		
		Set<URI> properties = new HashSet<>();
		for (Vertex v : vertices) {
			if (v.getId() instanceof URI) {
				properties.add((URI)v.getId());
			}
		}
		
		properties.addAll(shapeReasoner.predicates());
		
		for (URI p : properties) {
			Vertex v = owlReasoner.getGraph().vertex(p);
			if (v.hasProperty(RDF.TYPE, OWL.DATATYPEPROPERTY) ||v.hasProperty(RDF.TYPE, OWL.OBJECTPROPERTY)) {
				continue;
			}
			boolean isDatatype = false;
			boolean isObject = false;
			
			PropertyInfo info = shapeReasoner.getPropertyInfo(p);
			if (info != null) {
				for (ShapePropertyPair pair : info.getUsage()) {
					PropertyConstraint constraint = pair.getPropertyConstraint();
					if (!isDatatype) {
						isDatatype = constraint.getDatatype()!=null;
					}
					if (!isObject) {
						isObject = constraint.getValueClass()!=null || constraint.getShape()!=null;
					}
				}
			}

			URI range = v.getURI(RDFS.RANGE);
			if (range != null) {
				if (owlReasoner.isSubclassOfLiteral(range)) {
					isDatatype = true;
				} else {
					isObject = true;
				}
			}
			
			if (isDatatype && isObject) {
				logger.warn("Cannot classify property: " + p.stringValue());
				continue;
			}
			
			if (isDatatype) {
				owlReasoner.getGraph().edge(p, RDF.TYPE, OWL.DATATYPEPROPERTY);
				logger.info("classifyProperty: <{}> rdf:type owl:DatatypeProperty", p.stringValue());
			} else {
				owlReasoner.getGraph().edge(p, RDF.TYPE, OWL.OBJECTPROPERTY);
				logger.info("classifyProperty: <{}> rdf:type owl:ObjectProperty", p.stringValue());
			}

			
			
			
		}
		
		
				
		
	}
}
