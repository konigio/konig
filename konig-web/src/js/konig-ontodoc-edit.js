$(function(){
var Ontodoc = konig.ontodoc.constructor;
var ClassInfo = konig.ClassInfo;
var PropertyBlock = konig.PropertyBlock;


/*****************************************************************************/	
PropertyBlock.prototype.renameClass = function(oldIRI, newIRI) {
	for (var i=0; i<this.propertyList.length; i++) {
		var info = this.propertyList[i];
		if (info.expectedType) {
			var list = info.expectedType;
			for (var j=0; j<list.length; j++) {
				var type = list[j];
				if (type.stringValue === oldIRI.stringValue) {
					list.splice(j, 1, newIRI);
				}
			}
		}
	}
}
/*****************************************************************************/	
ClassInfo.prototype.renameClass = function(oldIRI, newIRI) {
	if (this.owlClass.stringValue === oldIRI.stringValue) {
		this.owlClass = newIRI;
		this.localName = newIRI.localName;
	}
	if (this.propertyBlock) {
		for (var i=0; i<this.propertyBlock.length; i++) {
			var block = this.propertyBlock[i];
			block.renameClass(oldIRI, newIRI);
		}
	}
}
/*****************************************************************************/	
Ontodoc.prototype.initEdit = function() {

	var self = this;
	$("#edit-button").click(function() {
		self.editMode = !self.editMode;
		$(this).toggleClass("enabled");
		$(window).trigger('hashchange');
	});

}	

Ontodoc.prototype.editClass = function(classInfo) {
	var self = this;
	$("#class-name-edit").keyup(function(){
		var value = $(this).text();
		
		var classId = classInfo.owlClass.stringValue;
		
		var indexElement = $('.ontodoc-index-entry>a[href="#' + classId + '"]');
		indexElement.text(value);
		
	}).blur(function(){
		self.doRenameClass();
	});
	

	
	
}

Ontodoc.prototype.renameClass = function(oldIRI, newIRI) {

	for (var key in this.classMap) {
		var classInfo = this.classMap[key];
		classInfo.renameClass(oldIRI, newIRI);
	}
	
	var info = this.classMap[oldIRI.stringValue];
	delete this.classMap[oldIRI.stringValue];
	this.classMap[newIRI.stringValue] = info;
	
	this.graph.replaceIRI(oldIRI, newIRI);
	
}

Ontodoc.prototype.doRenameClass = function() {

	var location = window.location;
	var hash = location.hash;
	if (hash) {
		hash = hash.substring(1);
		var oldIRI = new IRI(hash);
		var localName = $("#class-name-edit").text();
		var newIRI = new IRI(oldIRI.namespace + localName);

		var indexElement = $('.ontodoc-index-entry>a[href="#' + oldIRI.stringValue + '"]');
		indexElement.attr("href", '#' + newIRI.stringValue);
		
		
		this.renameClass(oldIRI, newIRI);
	}
}

konig.ontodoc.initEdit();
	
});