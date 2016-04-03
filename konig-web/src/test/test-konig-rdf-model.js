


QUnit.test("replace IRI", function(assert){
	var rdf = konig.rdf;
	var Context = konig.jsonld.Context;
	var owl = konig.owl;
	var schema = konig.schema;
	var rdfs = konig.rdfs;
	
	var doc = {
		"@context" : {
			"owl" : "http://www.w3.org/2002/07/owl#",
			"rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
			"schema" : "http://schema.org/",
			"xsd" : "http://www.w3.org/2001/XMLSchema#",
			"Person" : "schema:Person",
			"givenName" : {
				"@id" : "schema:givenName",
				"@type" : "xsd:string"
			},
			"familyName" : {
				"@id" : "schema:familyName",
				"@type" : "xsd:string"
			},
			"domain" : {
				"@id" : "rdfs:domain",
				"@type" : "@id"
			},
			"range" : {
				"@id" : "rdfs:range",
				"@type" : "@id"
			}
		},
		"@graph" : [{
			"@id" : "schema:Person",
			"@type" : "owl:Class"
		},{
			"@id" : "schema:givenName",
			"domain" : "schema:Person",
			"range" : "xsd:string"
		},{
			"@id" : "http://example.com/alice",
			"@type" : "Person",
			"givenName" : "Alice",
			"familyName" : "Smith"
		},{
			"@id" : "http://example.com/bob",
			"@type" : "Person",
			"givenName" : "Bob",
			"familyName" : "Johnson"
		}]
	};
	
	var context = new Context(doc["@context"]);
	var expanded = context.expand(doc);
	var flat = context.flatten(expanded);
	
	var graph = rdf.graph();
	graph.loadFlattened(flat['@graph']);
	
	graph.replaceIRI("http://schema.org/Person", "http://schema.org/Human");
	
	var human = new IRI("http://schema.org/Human");
	var alice = new IRI("http://example.com/alice");
	var bob = new IRI("http://example.com/bob");
	assert.ok(graph.contains(human, rdf.type, owl.Class));
	assert.ok(graph.contains(schema.givenName, rdfs.domain, human));
	assert.ok(graph.contains(alice, rdf.type, human));
	assert.ok(graph.contains(bob, rdf.type, human));
	
	
	
	
});	

QUnit.skip("load list", function(assert){
	var rdf = konig.rdf;
	var jsonld = konig.jsonld;
	var owl = konig.owl;
	
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
	var flat = jsonld.flatten(expanded);
	
	var graph = rdf.graph();
	graph.loadFlattened(flat["@graph"]);
	
	var vertex = graph.vertex("http://schema.org/ContactPoint");
	var keyList = vertex.v().out(owl.hasKey).first();

	assert.ok(keyList instanceof Vertex, "list value is a Vertex");

	var list = keyList.elements;
	assert.ok(list, "list elements exist");
	assert.equal(list.length, 2, "list has correct size");

	assert.equal(list[0].id.stringValue, "http://schema.org/contactPointOf", "First element has correct value");
	assert.equal(list[1].id.stringValue, "http://schema.org/contactType", "Second element has correct value");
	
	
	
});

QUnit.skip("parse jsonld", function(assert){
	
	var Context = konig.jsonld.Context;
	var schema = konig.schema;
	var rdfs = konig.rdfs;
	var Vertex = konig.rdf.Vertex;
	
	var doc = {
		"@context" : {
			"schema" : "http://schema.org/",
			"owl" : "http://www.w3.org/2002/07/owl#",
			"rdfs" : "http://www.w3.org/2000/01/rdf-schema#",
			
			"subClassOf" : {
				"@id" : "rdfs:subClassOf",
				"@type" : "@id"
			}
		},
		"@graph" : [{
			"@id" : "schema:Thing",
			"@type" : "owl:Class"
		},{
			"@id" : "schema:Person",
			"@type" : "owl:Class",
			"subClassOf" : "schema:Thing"
		}]
		
	};
	

	var context = new Context(doc["@context"]);
	var expand = context.expand(doc);
	console.log(expand);
	var flat = context.flatten(expand);

	
	var graph = rdf.graph();
	graph.loadFlattened(flat["@graph"]);
	
	var thing = graph.V(schema.Person).out(rdfs.subClassOf).first();
	assert.ok(thing instanceof Vertex, "schema:Thing parsed as an IRI vertex");
	
});

QUnit.skip("until-repeat loop", function(assert){
	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;
	var rdfs = konig.rdfs;
	var step = rdf.step;

	graph.statement(schema.CollegeOrUniversity, rdfs.subClassOf, schema.EducationalOrganization);
	graph.statement(schema.EducationalOrganization, rdfs.subClassOf, schema.Organization);
	graph.statement(schema.Organization, rdfs.subClassOf, schema.Thing);
	
	
	var list = graph.V(schema.CollegeOrUniversity)
		.until(step().hasNot(rdfs.subClassOf))
		.repeat(step().out(rdfs.subClassOf))
		.path()
		.execute();
	
	console.log(list);
	assert.ok(true);
});

QUnit.skip("path traversal", function(assert){
	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;

	graph.V("http://example.com/alice")
		.addRelationship(schema.knows, "http://example.com/bob")
		.out(schema.knows)
		.addRelationship(schema.knows, "http://example.com/carl")
		.out(schema.knows)
		.addRelationship(schema.knows, "http://example.com/denise")
		.execute();
	
	var list = graph.V("http://example.com/alice")
		.out(schema.knows)
		.out(schema.knows)
		.out(schema.knows)
		.path()
		.execute();
	
	assert.equal(list.length, 1, "One path emitted");
	
	var path = list[0];
	assert.equal(path[0].id.stringValue, "http://example.com/alice", "path[0] is correct");
	assert.equal(path[1].id.stringValue, "http://example.com/bob", "path[1] is correct");
	assert.equal(path[2].id.stringValue, "http://example.com/carl", "path[2] is correct");
	assert.equal(path[3].id.stringValue, "http://example.com/denise", "path[3] is correct");
});

QUnit.skip("Create Statement", function(assert){
	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;

	graph.statement(schema.Person, rdf.type, owl.Class);
	graph.statement("http://example.com/alice", rdf.type, schema.Person);
	graph.statement("http://example.com/bob", rdf.type, schema.Person);
	
	var person = graph.vertex(schema.Person);
	var inward = person.inward(rdf.type);
	assert.equal(inward.length, 2);
});

QUnit.skip("inward traversal", function(assert){
	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;
	
	graph.statement(schema.Person, rdf.type, owl.Class);
	graph.statement("http://example.com/alice", rdf.type, schema.Person);
	graph.statement("http://example.com/bob", rdf.type, schema.Person);
	
	var list = graph.V(schema.Person).inward(rdf.type).execute();
	
	var alice = null
	var bob = null;
	for (var i=0; i<list.length; i++) {
		var v = list[i];
		if (v.id.stringValue === "http://example.com/alice") {
			alice = v;
		} 
		if (v.id.stringValue === "http://example.com/bob") {
			bob = v;
		}
	}
	
	
	assert.equal(list.length, 2, "Traversal has one item");
	assert.ok(alice, "Traversal contains Alice");
	assert.ok(bob, "Traversal contains Bob");
});

QUnit.skip("has traversal", function(assert){

	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;

	graph.statement("http://example.com/alice", rdf.type, schema.Person);
	graph.statement("http://example.com/alice", schema.givenName, "Alice");
	graph.statement("http://example.com/alice", schema.familyName, "Smith");
	graph.statement("http://example.com/bob", rdf.type, schema.Person);
	graph.statement("http://example.com/bob", schema.givenName, "Bob");
	graph.statement("http://example.com/bob", schema.familyName, "Jones");
	
	var list = graph.V(schema.Person).inward(rdf.type).has(schema.givenName, "Alice").execute();
	
	assert.equal(list.length, 1, "Result has correct size");
	assert.equal(list[0].id.stringValue, "http://example.com/alice", "Result has correct value");
	
	
	
});

QUnit.skip("add relationship", function(assert){

	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;

	graph.V("http://example.com/alice")
		.addRelationship(schema.knows, "http://example.com/bob")
		.addRelationship(schema.knows, "http://example.com/carl").execute();
	
	
	assert.equal(graph.V("http://example.com/alice").has(schema.knows, "http://example.com/bob").execute().length, 1);
	assert.equal(graph.V("http://example.com/alice").has(schema.knows, "http://example.com/carl").execute().length, 1);	
	
});

QUnit.skip("out traversal", function(assert){

	var rdf = konig.rdf;
	var graph = rdf.graph();
	var schema = konig.schema;
	var owl = konig.owl;

	graph.V("http://example.com/alice")
		.addRelationship(schema.knows, "http://example.com/bob")
		.v("http://example.com/bob")
		.addRelationship(schema.knows, "http://example.com/carl").execute();
	
	var list = graph.V("http://example.com/alice").out(schema.knows).out(schema.knows).execute();
	assert.equal(list.length, 1);
	assert.equal(list[0].id.stringValue, "http://example.com/carl");
	
});


QUnit.skip("graph.V", function(assert){
	var rdf = konig.rdf;
	var graph = rdf.graph();
	
	var list = graph.V("http://example.com/alice").execute();
	assert.equal(list.length, 1, "Traversal has one item");
	assert.ok(list[0] instanceof rdf.Vertex, "Traversal contains a Vertex");
	assert.equal(list[0].id.stringValue, "http://example.com/alice", "Vertex has correct stringValue");
});




QUnit.skip("Load", function(assert){
	

	var foaf = konig.foaf;
	var Vertex = rdf.Vertex;
	var Literal = rdf.Literal;
	
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
		"id" : "ke:Ribosome",
		"prefLabel" : "Ribosome",
		"isPrimaryTopicOf" : {
        	 "id" : "https://en.wikipedia.org/wiki/Ribosome",
        	 "type" : "schema:WebPage",
        	 "prefLabel" : "Ribosome (Wikipedia)"
		}
	};
	
	var expand = context.expand(doc);
	var flat = context.flatten(expand);
	
	var graph = rdf.graph();
	console.log(flat);
	graph.loadFlattened(flat["@graph"]);
	
	var wiki = graph.V("http://www.konig.io/entity/Ribosome").out(foaf.isPrimaryTopicOf).execute()[0];
	
	assert.ok(wiki instanceof Vertex, "IRI reference loaded properly");
	assert.equal(wiki.id.stringValue, "https://en.wikipedia.org/wiki/Ribosome");
	

	
});


QUnit.skip("rdfa", function(assert){
	var e = document.getElementById("item1");
	var resource = rdf.rdfaResource(e);
	var type = rdf.rdfaType(e);
	
	assert.equal(resource, "http://data.konig.io/item/1", "rdfaResource");
	assert.equal(type, "http://www.w3.org/2002/07/owl#Thing", "rdfaType");
	
	
	
});

QUnit.skip("Vertex", function(assert){

	var alice = "http://example.com/alice";
	var likes = "http://example.com/likes";
	var bob = "http://example.com/bob";
	var carl = "http://example.com/carl";
	var parent = "http://example.com/parent";
	
	var graph = rdf.graph();
	var s1 = graph.statement(alice, likes, bob);
	
	var vertex = graph.vertex(alice);
	var aliceLikes = vertex.propertyValue(likes);
	assert.ok(aliceLikes, "vertex.propertyValue returned a non-null value");
	
	assert.equal(aliceLikes.stringValue, bob, "vertex.propertyValue returned the correct value");
	

	var s2 = graph.statement(alice, parent, carl);
	
	var aliceParent = vertex.propertyValue(parent);
	assert.ok(aliceParent, "New statement added to graph is reflected in vertex");
	assert.equal(aliceParent.stringValue, carl);
	

	graph.remove(s2);
	graph.remove(s1);
	
	assert.ok(vertex.isEmpty(), "Vertex is empty after statements removed from graph");
	
	
	
});

QUnit.skip("ChangeSet", function(assert){
	var alice = "http://example.com/alice";
	var likes = "http://example.com/likes";
	var bob = "http://example.com/bob";
	var carl = "http://example.com/carl";
	var parent = "http://example.com/parent";
	
	var graph = rdf.graph();
	var s1 = graph.statement(alice, likes, bob);
	var s2 = graph.statement(alice, parent, carl);
	
	var set = new ChangeSet();
	set.addStatement(s2);
	
	set.undo(graph);
	
	var list = graph.select(alice, null, null);
	assert.equal(list.length, 1, "Wrong number of statements");
	
});


  QUnit.skip( "Vertex.getPropertyValue", function(assert) {
	  var done = assert.async();
	  var me = "http://example.com/gmcfall";
	  var graph = rdf.graph();
	  var doc = {
				'@context' : {
					'concept' : 'http://example.org/page/2/concept/',
					'term' : 'http://example.org/page/2/term/',
					'rdfs' : 'http://www.w3.org/2000/01/rdf-schema#',
				},
				'@graph' : [{
					'@id' : 'concept:Esophagus',
					'rdfs:label' : 'Esophagus',
					'term:endsAt' : {'@id' : 'concept:Stomach'},
				}, {
					'@id' : 'concept:Stomach',
					'term:endsAt' : {'@id' : 'concept:SmallIntestine'},
					'term:secretes' : {'@id' : 'concept:Protease'}
				}, {
					'@id' : 'concept:SmallIntestine',
					'rdfs:label' : 'Small Intestine',
					'term:endsAt' : {'@id' : 'concept:LargeIntestine'}
				}, {
					'@id' : 'concept:Protease',
					'@type' : 'concept:Enzyme',
					'rdfs:label' : 'Small Intestine',
					'term:endsAt' : {'@id' : 'concept:LargeIntestine'}
				}, {
					'@id' : 'concept:LargeIntestine',
					'rdfs:label' : 'Large Intestine',
					'term:lastPartOf' : {'@id' : 'concept:DigestiveSystem'},
					'term:hasPart' : [{
						'@id' : 'concept:Cecum'
					},{
						'@id' : 'concept:Colon'	
					},{
						'@id' : 'concept:Rectum'	
					}]
				}, {
					'@id' : 'concept:DigestiveSystem',
					'rdfs:label' : 'Digestive System'
				}]
			};
	  
	  graph.name = "MyGraph";
	  graph.load(doc, function(err, g) {
		  
		  var vertex = graph.vertex('http://example.org/page/2/concept/Stomach');
		  var value = vertex.propertyValue('http://example.org/page/2/term/secretes');
		  assert.ok(value, 'Property was found');
		  assert.equal(value.stringValue, 'http://example.org/page/2/concept/Protease', 'Property has correct value');

		  done();
	  });
	  
	  
  });
  
  QUnit.skip( "Object property serialization", function(assert) {
	  var done = assert.async();
	  var me = "http://example.com/gmcfall";
	  var graph = rdf.graph();
	  var doc = {
		  '@context' : {
			  'schema' : 'http://schema.org/',
			  'givenName' : 'schema:givenName',
			  'familyName' : 'schema:familyName'
		  },
		  '@id' : me,
		  givenName : 'Greg',
		  familyName : 'McFall'	  
	  };
	  
	  graph.name = "MyGraph";
	  graph.load(doc, function(err, g) {

		  var result = graph.select(me);
		  assert.equal(result.length, 2);
		  
		  assert.ok(graph != null, 'dummy test');
		  done();
	  });
	  
	  
  });
  

  QUnit.skip( "Type handling", function(assert) {
	  var done = assert.async();
	  var me = "http://example.com/gmcfall";
	  var graph = rdf.graph();
	  var doc = {
		  '@context' : {
			  'schema' : 'http://schema.org/',
			  'givenName' : 'schema:givenName',
			  'familyName' : 'schema:familyName'
		  },
		  '@id' : me,
		  '@type' : 'schema:Person'
	  };
	  
	  graph.name = "MyGraph";
	  graph.load(doc, function(err, g) {

		  var vertex = graph.vertex(me);
		  var type = vertex.propertyValue('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
		  assert.ok(type instanceof rdf.IRI);
		  done();
	  });
	  
	  
  });
  


QUnit.skip( "Load Flattened", function(assert) {
	 
	  var doc = {
			"@context" : {
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

	  var context = new konig.jsonld.Context(doc["@context"]);
	  var ex = context.expand(doc);
	  var flat = context.flatten(ex);

//	  console.log(flat["@graph"]);
	  
	  var graph = rdf.graph();
	  graph.loadFlattened(flat['@graph']);
	  
	  console.log(graph.vertex("http://www.konig.io/shape/Person-v1").toJson());
	  
	  assert.ok(true);
	  
	  
	  
	  
  });
  
