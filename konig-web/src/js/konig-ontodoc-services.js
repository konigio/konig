$(document).ready(function(){
	

/**
 * A mock implementation of the OntologyService.  Used for testing
 */
function MockOntologyService() {
	
}

MockOntologyService.prototype.getOntologyGraph = function() {
	return {
		"@context" : {
			"ks" : "http://www.konig.io/ns/core/",
			"kss" : "http://www.konig.io/shape/",
			"schema" : "http://schema.org/",
			"xsd" : "http://www.w3.org/2001/XMLSchema#",
			"sh" : "http://www.w3.org/ns/shacl#",
			"owl" : "http://www.w3.org/2002/07/owl#",
			"rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
			"scopeClass" : {
				"@id" : "sh:scopeClass",
				"@type" : "@id"
			},
			"datatype" : {
				"@id" : "sh:datatype",
				"@type" : "@id"
			},
			"description" : {
				"@id" : "sh:description",
				"@type" : "xsd:string"
			},
			"label" : {
				"@id" : "rdfs:label",
				"@type" : "xsd:string"
			},
			"predicate" : {
				"@id" : "sh:predicate",
				"@type" : "@id"
			},
			"property" : {
				"@id" : "sh:property",
				"@type" : "sh:PropertyConstraint"
			},
			"subClassOf" : {
				"@id" : "rdfs:subClassOf",
				"@type" : "@id"
			},
			"valueShape" : {
				"@id" : "sh:valueShape",
				"@type" : "@id"
			}
			
			
		},
		"@graph" : [{
			"@id" : "http://www.konig.io/ns/core/",
			"@type" : "owl:Ontology",
			"label" : "Konig Ontology"
		},{
			"@id" : "schema:Thing",
			"@type" : "owl:Class"
		},{
			"@id" : "schema:Person",
			"@type" : "owl:Class",
			"subClassOf" : "schema:Thing"
		},{
			"@id" : "kss:Person-v1",
			"@type" : "sh:Shape",
			"scopeClass" : "schema:Person",
			"property" : [{
				"predicate" : "schema:givenName",
				"datatype" : "xsd:string",
				"description" : "The person's given name.  In the US, this is the person's first name."
			},{
				"predicate" : "schema:familyName",
				"datatype" : "xsd:string",
				"description" : "The person's family name.  In the US, this is the person's last name."
			}, {
				"predicate" : "schema:address",
				"valueShape" : "kss:PostalAddress-v1",
				"description" : "The person's postal address"
			}]
		}, {
			"@id" : "kss:PostalAddress-v1",
			"@type" : "sh:Shape",
			"scopeClass" : "schema:PostalAddress",
			"property" : [{
				"predicate" : "schema:streetAddress",
				"datatype" : "xsd:string",
				"description" : "The street address.  For example, 1600 Pennsylvania Ave."
			}]
		}]
		
	};
}


if (typeof(konig)==="undefined") {
	konig={};
}

konig.ontologyService = new MockOntologyService();
	
});