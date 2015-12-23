$(function(){
	
var RdfResource = konig.rdf.RdfResource;	
var Context = konig.jsonld.Context;
	
/*****************************************************************************/

Context.prototype.serializeGraph = function(graph) {
	var result = {};
	if (this.contextIRI) {
		result["@context"] = this.contextIRI;
	}
	var graphKey = this.keyword("@graph");
	var array = result[graphKey] = [];
	
	var list = graph.namedIndividualsList();
	for (var i=0; i<list.length; i++) {
		array.push(this.serializeVertex(list[i]));
	}
	
	return result;
}

Context.prototype.serializeVertex = function(vertex) {
	var context = this;
	var graph = vertex.graph;
	var inverse = context.inverse();
	var idValue = vertex.id.stringValue;
	
	var json = {};
	
	if (!idValue.startsWith("_:")) {
		var idKey = context.keyword("@id");
		json[idKey ] = vertex.id.stringValue;
	}
	
	var list = vertex.outStatements();
	for (var i=0; i<list.length; i++) {
		var s = list[i];
		
		
		var term = null;
		var termKey = inverse[s.predicate.stringValue];
		if (termKey) {
			term = context.term(termKey);
		}
		
		var key = context.compactIRI(s.predicate.stringValue);
		
		var value = null;
		var object = s.object;
		if (object instanceof RdfResource) {
			if (term && term["@type"]==="@id") {
				value = object.stringValue;
			} else {
				var objectVertex = graph.vertex(object);
				value = this.serializeVertex(objectVertex);
			}
		} else {
			
			if (term && (term["@type"] || term["@language"])) {
				value = object.stringValue;
			} else {
				var language = object.language;
				var type = object.type;
				value = {
					"@value" : object.stringValue
				};
				if (language) {
					value["@language"] = language;
				}
				if (type) {
					value["@type"] = type;
				}
			}
		}
		var prior = json[key];
		if (prior) {
			
			if (!Array.isArray(prior)) {
				prior = [prior];
			}
			prior.push(value);
			
		} else {

			json[key] = value;
		}
	}
	
	return json;
}
	
	
});