$(document).ready(function(){
	

function JsonldContext(context, id) {
	this.contextIRI = id;
	this.context = context || {};
	this.inverted = null;
	this.keywords = this.getKeywords(this.context);
}

JsonldContext.prototype.compactIRI = function(iriStringValue) {
	var key = iriStringValue;
	var inverse = this.inverse();
	var term = inverse[key];
	if (term) {
		key = term;
	} else {
		var slash = iriStringValue.lastIndexOf('/');
		var hash = iriStringValue.lastIndexOf('#');
		var delim = Math.max(slash, hash);
		if (delim >0) {
			var namespace = iriStringValue.substring(0, delim+1);
			prefix = inverse[namespace];
			if (prefix) {
				var localName = iriStringValue.substring(delim+1);
				key = prefix + ":" + localName;
			}
		}
	}
	return key;
}

JsonldContext.prototype.inverse = function() {
	if (!this.inverted) {
		var sink = this.inverted = {};
		for (var key in this.context) {
			var value = this.expandIRI(key);
			sink[value] = key;
		}
	}
	return this.inverted;
}

JsonldContext.prototype.id = function(object) {
	var key = this.keywords["@id"];
	return object[key];
}

JsonldContext.prototype.literalValue = function(object) {
	var key = this.keywords["@value"];
	return object[key];
}

JsonldContext.prototype.keyword = function(keyword) {
	return this.keywords[keyword];
}

JsonldContext.prototype.term = function(key) {
	return this.context[key];
}

JsonldContext.prototype.termObject = function(key) {
	var term = this.context[key];
	return typeof(term) === "string" ? {"@id": term} : term;
}

JsonldContext.prototype.getKeywords = function(context) {
	var alias = {
		"@id" : "@id",
		"@value" : "@value",
		"@type" : "@type",
		"@language" : "@language",
		"@container" : "@container",
		"@list" : "@list",
		"@set" : "@set",
		"@reverse" : "@reverse",
		"@base" : "@base",
		"@vocab" : "@vocab",
		"@graph" : "@graph"
	};
	
	for (var key in context) {
		var value = context[key];
		if (
			(value === "@id") ||
			(value === "@value") ||
			(value === "@type") ||
			(value === "@language") ||
			(value === "@container") ||
			(value === "@list") ||
			(value === "@set") ||
			(value === "@reverse") ||
			(value === "@index") ||
			(value === "@base") ||
			(value === "@vocab") ||
			(value === "@graph")
		) {
			alias[value] = key;
		}
	}
	
	return alias;
}

JsonldContext.prototype.flatten = function(object) {
	var flattener = new Flattener(this);
	return flattener.flatten(object);
}



JsonldContext.prototype.expandObject = function(object) {
	var result = {};
	for (var key in object) {
		if (key === "@context") {
			continue;
		}
		
		var expandedKey = this.expandIRI(key);
		var value = object[key];
		
		var term = this.term(key);
		
		result[expandedKey] = this.expandValue(expandedKey, term, value);
		
		
	}
	return result;
}

JsonldContext.prototype.expand = JsonldContext.prototype.expandObject;


JsonldContext.prototype.expandValue = function(key, term, value) {
	switch (typeof(value)) {
		
	case "string" :

		if (key==='@id' || key==='@type') {
			return this.expandIRI(value);
		}
		if (typeof(term) === "object") {
			var valueType = term["@type"];
			if (valueType === "@id") {
				return {
					"@id" : this.expandIRI(value)
				};
			} else if (valueType) {
				var expandedType = this.expandIRI(valueType);
				return {
					"@type" : expandedType,
					"@value" : value
				};
			}
			return {
				"@value" : value
			};
		}
		
		return {
			"@value" : value
		};
		
		break;
		
	case "object" :
		if (Array.isArray(value)) {
			return this.expandArray(key, term, value);
		}
		
		
		var valueTerm = this.keyword('@value');
		var literalValue = value[valueTerm];
		if (literalValue) {
			var typeTerm  = this.keyword("@type");
			var typeValue = value[typeTerm];
			if (typeValue) {
				return {
					"@type" : typeValue,
					"@value" : literalValue
				};
			}
			var languageTerm = this.keyword("@language");
			var languageValue = value[languageTerm];
			if (languageValue) {
				return {
					"@language" : languageValue,
					"@value" : literalValue
				};
			}
			return {
				"@value" : literalValue
			};
			
		}

		return this.expandObject(value);
	
	}
	return value;
}

JsonldContext.prototype.expandArray = function(key, term, array) {

	var list = [];
	var result = list;
	
	if (term && term["@container"] === "@list") {
		result = { "@list" : list}
	}
	
	
	for (var i=0; i<array.length; i++) {
		var value = array[i];
		list.push( this.expandValue(key, term, value) );
	}
	
	return result;
}

/**
 * Check if an object has a specified type
 * @param object The object being checked for a specific type.
 * @param {string | IRI} typeIRI The type to be matched against the object's type.
 * @returns {Boolean}
 */
JsonldContext.prototype.hasType = function(object, typeIRI) {
	var typeKey = this.keyword("@type");
	var typeValue = object[typeKey];
	if (typeValue) {
		if (typeIRI.stringValue) {
			typeIRI = typeIRI.stringValue;
		}
		if (Array.isArray(typeValue)) {
			for (var i=0; i<typeValue.length; i++) {
				var value = this.expandIRI(typeValue[i]);
				if (value === typeIRI) {
					return true;
				}
			}
		} else if (this.expandIRI(typeValue)===typeIRI){
			return true;
		}
	}
	return false;
}

JsonldContext.prototype.expandIRI = function(key) {
	var term = this.term(key);
	if (typeof(term) === "string") {
		return this.expandIRI(term);
	}
	if (typeof(term) === "object" ) {
		var id = term["@id"];
		if (id) {
			return this.expandIRI(id);
		}
	}
	
	var colon = key.indexOf(':');
	if (colon > 0) {
		var prefix = key.substring(0, colon);
		var namespace = this.expandIRI(prefix);
		if (namespace !== prefix) {
			return namespace + key.substring(colon+1);
		}
	}
	
	return key;
}



/*****************************************************************************/
function Flattener(context) {
	this.context = context;
	
}

Flattener.prototype.flatten = function(doc) {

	this.graph = [];
	this.result = {
		"@graph" : this.graph
	}
	this.bnodeMap = {};
	this.resourceMap = {};
	
	var graphArray = doc["@graph"];
	if (graphArray) {
		this.flattenArray(graphArray, this.graph);
	} else {
		this.flattenObject(doc);
	}
	
	
	return this.result;
}

Flattener.prototype.flattenObject = function(object, target) {
	var context = this.context;
	
	var idValue = object['@id'];
	if (!idValue) {
		// Anonymous BNode
		idValue = "_:" + (Flattener.counter++);
	} else if (idValue.startsWith("_:")){

		var bnodeId = this.bnodeMap[idValue];
		if (!bnodeId) {
			bnodeId = "_:" + (Flattener.counter++);
			this.bnodeMap[idValue] = bnodeId;
		}
		idValue = bnodeId;
	}
	
	var prior = this.resourceMap[idValue];
	var result = prior || target || {};
	this.resourceMap[idValue] = result;
	result['@id'] = idValue;
	
	if (!prior) {
		this.graph.push(result);
	}
	
	var reverseObject = null;
	for (var key in object) {
		var value = object[key];
		
		var array = result[key];
		if (!array) {
			result[key] = array = [];
		}
		
		if (key === '@reverse') {
			reverseObject = value;
			continue;
		}
		
		if (key === '@id') {
			continue;
		
		} else if (typeof(value) === "object"){
			if (Array.isArray(value)) {
				this.flattenArray(value, array);
			} else {
				
							
				
				var literalValue = context.literalValue(value);
				if (literalValue) {
					array.push( this.copyLiteral(value) );
				} else {
					array.push( {
						"@id" : this.flattenObject(value)
					});
				}
			}
		} else if (typeof(value) === "string") {
			array.push( value );
		}
	}
	
	if (reverseObject) {
		this.flattenReverse(idValue, reverseObject);
	}
	return idValue;
}

Flattener.prototype.flattenReverse = function(objectId, reverse) {

	for (var key in reverse) {
		var value = reverse[key];
		var entity = {};
		switch (typeof(value)) {
		case "string" :
			entity['@id'] = value;
			break;
			
		case "object" :
			this.flattenObject(value, entity);
			break;
		}
		entity[key] = objectId;
		this.graph.push(entity);
	}
}

Flattener.prototype.flattenArray = function(array, sink) {
	var result = sink || [];
	for (var i=0; i<array.length; i++) {
		var value = array[i];
		
		switch (typeof(value)) {
		
		case "string" :
			result.push(value);
			break;
			
		case "object" :
			if (Array.isArray(value)) {
				result.push(this.flattenArray(value));
			} else {
				var literalValue = this.context.literalValue(value);
				if (literalValue) {
					result.push(this.copyLiteral(value));
				} else {
					var idValue = this.flattenObject(value);
					if (result != this.graph) {
						result.push({"@id" : this.flattenObject(value)});
					}
				}
			}
		}
	}
	return result;
	
}

Flattener.prototype.copyLiteral = function(object) {
	var result = {};
	for (var key in object) {
		result[key] = object[key];
	}
	return result;
}

Flattener.counter = 1;
/*****************************************************************************/
	
function jsonld() {}

jsonld.prototype.Context = JsonldContext;

jsonld.prototype.expand = function(doc, contextInfo) {
	
	if (!contextInfo) {
		contextInfo = new JsonldContext(doc['@context']);
	}
	
	return contextInfo.expandObject(doc);
	
}

jsonld.prototype.flatten = function(doc, contextInfo) {
	if (!contextInfo) {
		contextInfo = new JsonldContext(doc['@context']);
	}
	return contextInfo.flatten(doc);
}

	
if (typeof(konig) === "undefined") {
	konig = {};
}

konig.jsonld = new jsonld();
	
});