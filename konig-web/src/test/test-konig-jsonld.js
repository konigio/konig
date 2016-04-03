QUnit.skip("Expand list", function(assert){
	var jsonld = konig.jsonld;
	
	var doc = {
		"@context" : {
			schema: "http://schema.org/",
			owl: "http://www.w3.org/2002/07/owl#",
			hasKey : {
				"@id" : "owl:hasKey",
				"@type" : "@id",
				"@container" : "@list"
			}
		},
		"@id" : "schema:ContactPoint",
		"@type" : "owl:Class",
		"hasKey" : [
		      "schema:contactPointOf",
		      "schema:contactType"
		]
	};
	
	var expanded = jsonld.expand(doc);
	
	console.log(expanded);
	
	var ContactPoint = expanded[""];
	
	var hasKey = expanded["http://www.w3.org/2002/07/owl#hasKey"];


	assert.ok(hasKey, "hasKey property exists");
	
	if (hasKey) {
		var list = hasKey["@list"];
		assert.ok(list, "hasKey value contains '@list' field");
		
		assert.equal(list[0]["@id"], "http://schema.org/contactPointOf", "First list item is correct");
		assert.equal(list[1]["@id"], "http://schema.org/contactType", "Second list item is correct");
	}
	
});

QUnit.test("Flatten list", function(assert){
	var jsonld = konig.jsonld;
	
	var doc = {
		"@context" : {
			schema: "http://schema.org/",
			owl: "http://www.w3.org/2002/07/owl#",
			hasKey : {
				"@id" : "owl:hasKey",
				"@type" : "@id",
				"@container" : "@list"
			}
		},
		"@id" : "schema:ContactPoint",
		"@type" : "owl:Class",
		"hasKey" : [
		      "schema:contactPointOf",
		      "schema:contactType"
		]
	};
	
	var expanded = jsonld.expand(doc);
	var flattened = jsonld.flatten(expanded);
	
	console.log(flattened);
	
	assert.ok(true);
});


QUnit.skip("Ribosome", function(assert){

	var context = new konig.jsonld.Context({
		id : "@id",
		type: "@type",
		foaf: "http://xmlns.com/foaf/0.1/",
		ks: "http://www.konig.io/schema/",
		ke: "http://www.konig.io/entity/",
		prov: "http://www.w3.org/ns/prov#",
		owl: "http://www.w3.org/2002/07/owl#",
		rdfs: "http://www.w3.org/2000/01/rdf-schema#",
		schema: "http://schema.org/",
		skos: "http://www.w3.org/2004/02/skos/core#",
		xsd: "http://www.w3.org/2001/XMLSchema#",
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
	
	
	var doc = {
		"id" : "ke:Ribosome_shunting",
		"prefLabel" : "Ribosome shunting",
		"isPrimaryTopicOf" : {
        	 "id" : "https://en.wikipedia.org/wiki/Ribosome",
        	 "type" : "schema:WebPage",
        	 "prefLabel" : "Ribosome (Wikipedia)"
		}
	};
	
	var expand = context.expand(doc);
	var flat = context.flatten(expand);
	
	
	assert.ok(true);
});

QUnit.skip("Expand", function(assert){
	var doc = {
		"@context" : {
			'rdfs' : 'http://www.w3.org/2000/01/rdf-schema#',
			'schema' : 'http://schema.org/',
			'label' : 'rdfs:label',
			'address' : 'schema:address',
			'streetAddress' : 'schema:streetAddress',
			'alumniOf' : {
				"@id" : "schema:alumniOf",
				"@type" : "@id"
			},
			"college" : "http://example.com/college/",
			"parent" : {
				"@id" : "schema:parent",
				"@type" : "@id"
			},
			"p" : "http://example.com/person/"
		},
		"label" : "foo",
		"address" : {
			"streetAddress" : "100 Main St."
		},
		"alumniOf" : "college:VirginiaTech",
		"parent" : [
	          "p:Alice",
	          "p:Bob"
		]
	};
	
	var jsonld = konig.jsonld;
	
	var expanded = jsonld.expand(doc);
	console.log(expanded);
	
	var foo = expanded["http://www.w3.org/2000/01/rdf-schema#label"];
	
	assert.equal(foo['@value'], "foo", "label was expanded");
	
	var address = expanded["http://schema.org/address"];
	var streetAddress = address["http://schema.org/streetAddress"];
	assert.ok(streetAddress, "street address was expanded");
	assert.equal(streetAddress['@value'], "100 Main St.", "Correct street address");
	
	var alumniOf = expanded["http://schema.org/alumniOf"];
	assert.equal(alumniOf["@id"], "http://example.com/college/VirginiaTech", "IRI value expanded");
	
	var parent = expanded["http://schema.org/parent"];
	assert.equal(parent.length, 2, "Array has correct number of elements");
	var alice = parent[0];
	assert.equal(alice["@id"], "http://example.com/person/Alice", "Array expanded properly");
	
	
});



QUnit.skip("Flatten", function(assert){
	var doc = {
		"@context" : {
			'schema' : 'http://schema.org/',
			'address' : 'schema:address',
			'streetAddress' : 'schema:streetAddress'
		},
		"@id" : "http://example.com/alice",
		"address" : [{
			"streetAddress" : "100 Main St."
		}, {
			"streetAddress" : "1600 Maple Ave."
		}]
	};
	
	var jsonld = konig.jsonld;
	
	var alice = null;
	var mainSt = null;
	var mapleAve = null;
	

	var context = new jsonld.Context(doc['@context']);
	
	var expanded = context.expand(doc);
	var flat = context.flatten(expanded);
	console.log(flat);
	
	var array = flat["@graph"];
	for (var i=0; i<array.length; i++) {
		var obj = array[i];
		if (obj["@id"] === "http://example.com/alice") {
			alice = obj;
		} else {
			var addr = obj["http://schema.org/streetAddress"];
			if (addr && Array.isArray(addr)) {
				var value = addr[0]['@value'];
				if (value === "100 Main St.") {
					mainSt = obj;
				} else if (value === "1600 Maple Ave.") {
					mapleAve = obj;
				}
			}
			
		}
	}
	
	
	assert.ok(alice);
	assert.ok(mainSt);
	assert.ok(mapleAve);
	
	
});


