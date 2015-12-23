QUnit.skip("serialize vertex", function(assert){
	var jsonld = konig.jsonld;
	var context = konig.schemaContext;
	var schema = konig.schema;
	var rdf = konig.rdf;
	
	var graph = rdf.graph();
	
	
	
	graph.vertex("http://example.com/barack").v()
		.addProperty(schema.givenName, "Barack")
		.addProperty(schema.familyName, "Obama")
		.addRelationship(schema.address, "_:1")
		.v("_:1")
		.addProperty(schema.streetAddress, "1600 Pennsylvannia Ave NW")
		.addProperty(schema.addressLocality, "Washington")
		.addProperty(schema.addressRegion, "DC").execute();
	
	
	var barack = graph.vertex("http://example.com/barack");
	
	var json = context.serializeVertex(barack);
	
	
	assert.equal(json['@id'], "http://example.com/barack");
	assert.equal(json.givenName, "Barack");
	assert.equal(json.familyName, "Obama");
	assert.equal(json.address.streetAddress, "1600 Pennsylvannia Ave NW");
	assert.equal(json.address.addressLocality, "Washington");
	assert.equal(json.address.addressRegion, "DC");
	
	
});

QUnit.test("serialize graph", function(assert){
	var jsonld = konig.jsonld;
	var context = konig.schemaContext;
	var schema = konig.schema;
	var rdf = konig.rdf;
	
	var graph = rdf.graph();
	
	var barackId = "http://example.com/barack";
	var motherId = "http://example.com/ann";
	
	graph.vertex(barackId).v()
		.addProperty(schema.givenName, "Barack")
		.addProperty(schema.familyName, "Obama")
		.addRelationship(schema.parent, motherId)
		.addRelationship(schema.address, "_:1")
		.v("_:1")
		.addProperty(schema.streetAddress, "1600 Pennsylvannia Ave NW")
		.addProperty(schema.addressLocality, "Washington")
		.addProperty(schema.addressRegion, "DC")
		.v(motherId)
		.addProperty(schema.givenName, "Ann")
		.addProperty(schema.familyName, "Dunham")
		.execute();
	
	var json = context.serializeGraph(graph);
	
	console.log(json);
	
	var graph2 = json["@graph"];
	assert.ok(graph2, "@graph array found in result");
	
	var barack = null;
	var ann = null;
	for (var i=0; i<graph2.length; i++) {
		var n = graph2[i];
		if (n["@id"] === barackId) {
			barack = n;
		} else if (n["@id"] === motherId) {
			ann = n;
		}
	}
	
	assert.ok(barack);
	assert.ok(ann);
	
	assert.equal(barack['@id'], "http://example.com/barack");
	assert.equal(barack.givenName, "Barack");
	assert.equal(barack.familyName, "Obama");
	assert.equal(barack.parent, motherId);
	assert.equal(barack.address.streetAddress, "1600 Pennsylvannia Ave NW");
	assert.equal(barack.address.addressLocality, "Washington");
	assert.equal(barack.address.addressRegion, "DC");
	assert.equal(ann.givenName, "Ann");
	assert.equal(ann.familyName, "Dunham");
	
});


