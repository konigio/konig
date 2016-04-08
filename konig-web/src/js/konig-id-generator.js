$(function() {
	
// requires uuid.js, radix64.js	
	
function zeroPad(value) {
	while (value.length < 3) {
		value = '0' + value;
	}
	return value;
}	
function hex64(hex) {
	var value = parseInt(hex, 16);
	var result = enc64(value);
//	console.log(hex, value, result);
	return zeroPad(result);
}	

function binary(data, start, end) {
	var result = parseInt(data.substring(start, end), 16).toString(2);
	while (result.length < 32) {
		result = '0' + result;
	}
	return result;
}
	
function IdGenerator() {}	

var ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-_";

IdGenerator.prototype.nextGUID = function() {
	var id = uuid.v1().replace(/-/g,"");
	
	// Split into 4 strings, each containing a 32-bit hex number.
	// Convert to binary representation.
	
	var a = binary(id, 0, 8);
	var b = binary(id, 8, 16);
	var c = binary(id, 16, 24);
	var d = binary(id, 24, 32);
	
	var data = a + b + c + d;
	
	var result = "";
	var i = 128;
	while (i>0) {
		
		var start = i-6;
		if (start<0) {
			start = 0;
		}
		var index = parseInt(data.substring(start, i), 2);
		result = ALPHABET[index] + result;
		i -= 6;
	}
	
	console.log(result);
	return result;
}

IdGenerator.prototype.nextIRI = function() {
	return "http://www.konig.io/entity/" + this.nextGUID();
}

if (typeof(konig) == "undefined") {
	konig = {}
}

konig.idGenerator = new IdGenerator();



});