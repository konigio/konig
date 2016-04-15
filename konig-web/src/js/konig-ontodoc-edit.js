/*
 * #%L
 * konig-web
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
$(function(){
var Ontodoc = konig.Ontodoc;
var ShapeInfo = konig.ShapeInfo;
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
ShapeInfo.prototype.renameClass = function(oldIRI, newIRI) {
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

Ontodoc.prototype.editClass = function(shapeInfo) {
	var self = this;
	$("#class-name-edit").keyup(function(){
		var value = $(this).text();
		
		var classId = shapeInfo.owlClass.stringValue;
		
		var indexElement = $('.ontodoc-index-entry>a[href="#' + classId + '"]');
		indexElement.text(value);
		
	}).blur(function(){
		self.doRenameClass();
	});
	

	
	
}

Ontodoc.prototype.renameClass = function(oldIRI, newIRI) {

	for (var key in this.classMap) {
		var shapeInfo = this.classMap[key];
		shapeInfo.renameClass(oldIRI, newIRI);
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

	
});
