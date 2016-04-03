QUnit.test("Add / Remove", function(assert){
	var rdf = konig.rdf;
	// TODO: implement test
	assert.ok(true);
});

QUnit.skip("Record Typed BNode state", function(assert){
	var rdf = konig.rdf;
	var jsonld = konig.jsonld;
	var ChangeSet = konig.ChangeSet;
	var schema = konig.schema;
	var ks = konig.ks;
	
	var doc = {
		"@context" : {
			dbpedia: "http://dbpedia.org/resource/",
			owl: "http://www.w3.org/2002/07/owl#",
			schema: "http://schema.org/",
			ks: "http://www.konig.io/schema/",
			xsd: "http://www.w3.org/2001/XMLSchema#",
			ContactPoint: "schema:ContactPoint",
			contactPoint: "schema:contactPoint",
			hasKey: {
				"@id" : "owl:hasKey",
				"@type" : "@id",
				"@container" : "@list"
			},
			telephone : {
				"@id" : "schema:telephone",
				"@type" : "xsd:string"
			},
			description: {
				"@id" : "schema:description",
				"@type" : "xsd:string"
			},
			contactType: {
				"@id" : "schema:contactType",
				"@type" : "xsd:string"
			},
			contactPointOf: {
				"@id" : "ks:contactPointOf",
				"@type" : "@id"
			},
			inverseOf : {
				"@id" : "owl:inverseOf",
				"@type" : "@id"
			}
			
		},
		"@graph" : [{
			"@id" : "schema:contactPoint",
			"inverseOf" : "ks:contactPointOf"
		},{
			"@id" : "schema:ContactPoint",
			"hasKey" : ["ks:contactPointOf", "schema:contactType"]
		},{
			"@id" : "http://dbpedia.org/resource/Google",
			"contactPoint" : [{
				"@type" : "ContactPoint",
				"contactType" : "AppsSupport",
				"telephone" : "1-877-355-5787",
				"description" : "Support for Google Apps Administrators"
			}, {
				"@type" : "ContactPoint",
				"contactType" : "PlaySupport",
				"telephone" : "1-855-836-3987",
				"description" : "Support for customers who bought something on Google Play"
			}]
		}]
	};
	
	var expanded = jsonld.expand(doc);
	var flat = jsonld.flatten(expanded);
	
	var graph = rdf.graph();
	graph.loadFlattened(flat['@graph']);
	
	var google = graph.vertex("http://dbpedia.org/resource/Google");
	var playSupport = google.v().out(schema.contactPoint).has(schema.contactType, "PlaySupport").first();

	var changeSet = new ChangeSet(graph);
	changeSet.recordNode(playSupport);
	
	graph = changeSet.priorState;
	google = graph.vertex("http://dbpedia.org/resource/Google", true);
	assert.ok(google, "Google found in priorState");

	var contactPoint = google.v().inward(ks.contactPointOf).first();
	assert.ok(contactPoint, "ContactPoint found");
	assert.ok(contactPoint.v().has(ks.contactPointOf, "http://dbpedia.org/resource/Google").toList().length==1, "contactPointOf value exists");
	assert.ok(contactPoint.v().has(schema.contactType, "PlaySupport").toList().length==1, "contactType value exists");
	
	
});


