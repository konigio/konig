
/*
QUnit.test("test hex64", function(assert){

	var expected = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";
	
	function hex64(hex) {
		var value = parseInt(hex, 16);
		return enc64(value);
	}	
	
	for (var i=1; i<64; i++) {
		var hex = i.toString(16);
		var value = hex64(hex);
		assert.equal(value, expected[i]);
	}
	
});
*/

QUnit.test("test nextIRI", function(assert){
//	var value = 4220064229;
//	
//	console.log(value.toString(2));
//	console.log( (value&63));
//	
//	value = value >>> 6;
//	console.log(value.toString(2));
	var generator = konig.idGenerator;
	
	for (var i=0; i<20; i++) {
		generator.nextIRI();
	}
	var iri = generator.nextIRI();
	assert.ok(iri.indexOf("undefined")<0);
	assert.ok(true);
});
  
