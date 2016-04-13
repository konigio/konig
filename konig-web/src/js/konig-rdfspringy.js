$(function(){
	
function LocalNameLabeler() {
}	

LocalNameLabeler.prototype.label = function(vertex) {
	return vertex.id.localName || vertex.id.stringValue;
}

function buildGraph(memory, springy, vertex, property, labeler) {
	
	var key = vertex.id.stringValue;
	var subject = memory[key];
	if (!subject) {
		var labelValue = labeler.label(vertex);
		subject = springy.newNode({label: labelValue});
		memory[key] = subject;
		
		var list = vertex.v().out(property).toList();
		for (var i=0; i<list.length; i++) {
			var objectVertex = list[i];
			var object = buildGraph(memory, springy, list[i], property, labeler);
			springy.newEdge(subject, object);
		}
	}
	return subject;
}

function canvasWidth(nodeMap) {
	// Assume default font size of 16 pixels per em
	var pixelsPerEm = 16;
	
	// Assume spacing of at least 30 pixels between nodes
	var spacing = 30;
	var count = 0;
	var size = 0;
	for (var key in nodeMap) {
		count++;
		var node = nodeMap[key];
		var label = node.data.label;
		size += label.length;
	}
	
	var canvasWidth = Math.min(size*pixelsPerEm + count*spacing, 800);
	return canvasWidth;
}

$.fn.rdfspringy = function(options) {
	var vertex = options.vertex;
	var property = options.property;
	var labeler = options.labeler || new LocalNameLabeler();
	var springy  = new Springy.Graph();
	
	var map = {};
	buildGraph(map, springy, vertex, property, labeler);
	var widthValue = canvasWidth(map);
	var heightValue = Math.min(2*widthValue/3, 300);
	var canvas = this[0];
	canvas.setAttribute('width', widthValue);
	canvas.setAttribute('height', heightValue);
	
	return this.springy({graph: springy});
}
	
});