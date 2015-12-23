QUnit.test("Inverse Property", function(assert){
	var rdf = konig.rdf;
	var jsonld = konig.jsonld;
	var schema = konig.schema;
	var doc = {
		"@context" : {
			dbpedia: "http://dbpedia.org/resource/",
			schema : "http://schema.org/",
			owl: "http://www.w3.org/2002/07/owl#",
			member: {
				"@id" : "schema:member",
				"@type" : "@id"
			},
			memberOf: {
				"@id" : "schema:memberOf",
				"@type" : "@id"
			},
			inverseOf: {
				"@id" : "owl:inverseOf",
				"@type" : "@id"
			}
			
		},
		"@graph" : [{
			"@id" : "dbpedia:Larry_Page",
			"memberOf" : "dbpedia:Google"
		},{
			"@id" : "dbpedia:Sergey_Brin",
			"memberOf" : "dbpedia:Google"
		},{
			"@id" : "schema:member",
			"inverseOf" : "schema:memberOf"
		}]
	};
	
	var expanded = jsonld.expand(doc);
	var flat = jsonld.flatten(expanded);
	var graph = rdf.graph();
	graph.loadFlattened(flat["@graph"]);
	
	
	var reasoner = new konig.OwlReasoner(graph, graph);
	
	var list = reasoner.select("http://dbpedia.org/resource/Google", schema.member, null);
	
	assert.equal(list.length, 2, "Correct number of statements");
	
	var larry = null;
	var servey = null;
	console.log(list);
	for (var i=0; i<list.length; i++) {
		var s = list[i];
		var object = s.object;
		if (object.equals("http://dbpedia.org/resource/Larry_Page")) {
			larry = object;
		} else if (object.equals("http://dbpedia.org/resource/Sergey_Brin")) {
			sergey = object;
		}
	}
	
	assert.ok(larry, "First inverse value found");
	assert.ok(sergey, "Second inverse value found");
	
	
});