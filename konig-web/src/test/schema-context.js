$(function(){
	
var Context = konig.jsonld.Context;

konig.schemaContext = new Context({
	"schema" : "http://schema.org/",
	"xsd" : "http://www.w3.org/2001/XMLSchema#",
	"Person" : "schema:Person",
	"PostalAddress" : "schema:PostalAddress",
	"address" : "schema:address",
	"addressLocality" : {
		"@id" : "schema:addressLocality",
		"@type" : "xsd:string"
	},
	"addressRegion" : {
		"@id" : "schema:addressRegion",
		"@type" : "xsd:string"
	},
	"contactType" : {
		"@id" : "schema:contactType",
		"@type" : "xsd:string"
	},
	"givenName" : {
		"@id" : "schema:givenName",
		"@type" : "xsd:string"
	},
	"familyName" : {
		"@id" : "schema:familyName",
		"@type" : "xsd:string"
	},
	"parent" : {
		"@id" : "schema:parent",
		"@type" : "@id"
	},
	"postalCode" : {
		"@id" : "schema:postalCode",
		"@type" : "xsd:string"
	},
	"streetAddress" : {
		"@id" : "schema:streetAddress",
		"@type" : "xsd:string"
	}
});
	
	
});