var frisby = require('frisby');

frisby.create('Post person context')
	.post('http://localhost:8888/context/', {
		"@context" : {
			"schema" : "http://schema.org/",
			"Person" : "schema:Person"
		},
		"@id" : "http://www.konig.io/context/v1.test.person"
	}, {json: true})
	.inspectHeaders()
	.expectStatus(201)
	.expectHeader('content-location', 'http://localhost:8888/context/v1.test.person')
	.toss();

frisby.create('Get person context')
	.get('http://localhost:8888/context/v1.test.person')
	.expectJSON({
		"@context" : {
		"schema" : "http://schema.org/",
		"Person" : "schema:Person"
		},
		"@id" : "http://www.konig.io/context/v1.test.person"
	})
	.inspectHeaders()
	.expectHeader('content-type', 'application/ld+json')
	.expectStatus(200)
	.expectJSONTypes({
		versionNumber: 1
	})
	.toss();
	
