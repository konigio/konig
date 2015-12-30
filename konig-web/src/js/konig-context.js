/*
 * #%L
 * konig-web
 * %%
 * Copyright (C) 2015 Gregory McFall
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
$(document).ready(function() {
	
	konig.defaultContext = new konig.jsonld.Context({
		id : "@id",
		type: "@type",
		foaf: "http://xmlns.com/foaf/0.1/",
		ks: "http://www.konig.io/schema/",
		ke: "http://www.konig.io/entity/",
		prov: "http://www.w3.org/ns/prov#",
		owl: "http://www.w3.org/2002/07/owl#",
		rdfs: "http://www.w3.org/2000/01/rdf-schema#",
		schema: "http://schema.org/",
		sh : "http://www.w3.org/ns/shacl#",
		skos: "http://www.w3.org/2004/02/skos/core#",
		xsd: "http://www.w3.org/2001/XMLSchema#",
		dcterms: "http://purl.org/dc/terms/",
		vann: "http://purl.org/vocab/vann/",
		about: {
			"@id" : "schema:about",
			"@type" : "@id"
		},
		isPrimaryTopicOf: {
			"@id" : "foaf:isPrimaryTopicOf",
			"@type" : "@id"
		},
		prefLabel : "skos:prefLabel"
	});
	
	konig.vann = {
		preferredNamespacePrefix : new IRI("http://purl.org/vocab/vann/preferredNamespacePrefix")
	};
	konig.dcterms = {
		description: new IRI("http://purl.org/dc/terms/description")
	};
	
	konig.skos = {
		NAMESPACE: "http://www.w3.org/2004/02/skos/core#",
		prefLabel: new IRI("http://www.w3.org/2004/02/skos/core#prefLabel"),
		altLabel: new IRI("http://www.w3.org/2004/02/skos/core#prefLabel")
	};
	
	konig.foaf = {
		NAMESPACE: "http://xmlns.com/foaf/0.1/",
		isPrimaryTopicOf : new IRI("http://xmlns.com/foaf/0.1/isPrimaryTopicOf")
	};
	
	konig.ks = {
		NAMESPACE: "http://www.konig.io/schema/",
		LoadGraph : new IRI("http://www.konig.io/schema/LoadGraph"),
		Workspace : new IRI("http://www.konig.io/schema/Workspace"),
		contactPointOf: new IRI("http://www.konig.io/schema/contactPointOf")
	};
	
	konig.ke = {
		NAMESPACE: "http://www.konig.io/entity/",
		ENTITY: new IRI("http://www.konig.io/entity/Entity")
	};
	
	konig.prov = {
		wasUsedBy: new IRI("http://www.w3.org/ns/prov#wasUsedBy")	
	};
	
	konig.sh = {
		Shape: 	new IRI("http://www.w3.org/ns/shacl#Shape"),
		scopeClass: new IRI("http://www.w3.org/ns/shacl#scopeClass"),
		description: new IRI("http://www.w3.org/ns/shacl#description"),
		property: new IRI("http://www.w3.org/ns/shacl#property"),
		predicate: new IRI("http://www.w3.org/ns/shacl#predicate"),
		datatype: new IRI("http://www.w3.org/ns/shacl#datatype"),
		objectType: new IRI("http://www.w3.org/ns/shacl#class"),
		directType: new IRI("http://www.w3.org/ns/shacl#directType"),
		valueShape: new IRI("http://www.w3.org/ns/shacl#valueShape")
	};
	
	konig.as = {
		NAMESPACE: "http://www.w3.org/ns/activitystreams#",
		object: "http://www.w3.org/ns/activitystreams#object"
	};
	konig.owl = {
		Class : new IRI('http://www.w3.org/2002/07/owl#Class'),
		Ontology : new IRI('http://www.w3.org/2002/07/owl#Ontology'),
		Thing : new IRI('http://www.w3.org/2002/07/owl#Thing'),
		hasKey : new IRI('http://www.w3.org/2002/07/owl#hasKey'),
		inverseOf: new IRI('http://www.w3.org/2002/07/owl#inverseOf')
	};
	konig.rdfs = {
		comment : new IRI('http://www.w3.org/2000/01/rdf-schema#comment'),
		label : new IRI('http://www.w3.org/2000/01/rdf-schema#label'),
		subClassOf : new IRI('http://www.w3.org/2000/01/rdf-schema#subClassOf'),
		domain : new IRI('http://www.w3.org/2000/01/rdf-schema#domain'),
		range : new IRI('http://www.w3.org/2000/01/rdf-schema#range')
	};
	konig.schema = {
		CollegeOrUniversity : new IRI("http://schema.org/CollegeOrUniversity"),
		EducationalOrganization : new IRI("http://schema.org/EducationalOrganization"),
		Organization : new IRI("http://schema.org/Organization"),
		Person : new IRI("http://schema.org/Person"),
		Thing : new IRI("http://schema.org/Thing"),

		address : new IRI("http://schema.org/address"),
		addressLocality : new IRI("http://schema.org/addressLocality"),
		addressRegion : new IRI("http://schema.org/addressRegion"),
		contactPoint: new IRI("http://schema.org/contactPoint"),
		contactType: new IRI("http://schema.org/contactType"),
		description: new IRI("http://schema.org/description"),
		givenName : new IRI("http://schema.org/givenName"),
		familyName : new IRI("http://schema.org/familyName"),
		knows : new IRI("http://schema.org/knows"),
		member : new IRI("http://schema.org/member"),
		memberOf : new IRI("http://schema.org/memberOf"),
		parent : new IRI("http://schema.org/parent"),
		streetAddress : new IRI("http://schema.org/streetAddress")
	}
	
});
