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
$(document).ready(function(){
	

var rdf = konig.rdf;
var owl = konig.owl;
var rdfs = konig.rdfs;
var vann = konig.vann;
var dcterms = konig.dcterms;
var dc = konig.dc;
var schema = konig.schema;
var sh = konig.sh;
var kol = konig.kol;
var xsd = rdf.xsd;
var step = rdf.step;
var stringValue = rdf.stringValue;
var IRI = rdf.IRI;
var HistoryManager = konig.HistoryManager;
var BNode = rdf.BNode;
var __ = rdf.__;

/*****************************************************************************/
var SHACL = {
	Shape: new IRI("http://www.w3.org/ns/shacl#Shape")
};

var OWL = {
	Ontology: new IRI("http://www.w3.org/2002/07/owl#Ontology")	
};
/*****************************************************************************/
function PropertyOverview(propertyVertex) {
	this.propertyVertex = propertyVertex;
	this.init();
}

PropertyOverview.prototype.init = function() {
	var comment = this.propertyVertex.v().out(rdfs.comment, dcterms.description, dc.description).first();
	if (comment) {
		this.comment = comment.stringValue;
	}
	this.analyzeRange();
	this.analyzeDomain();
	this.analyzeInverse();
}

PropertyOverview.prototype.analyzeInverse = function() {

	this.inverseOf = this.propertyVertex.v().union(
		__.out(owl.inverseOf),
		__.inward(owl.inverseOf)
	).unique().toList();
	
	console.log(this.inverseOf);
}

PropertyOverview.prototype.analyzeRange = function() {
	var list = this.rangeList = [];
	var range = this.propertyVertex.v().out(rdfs.range).first();
	if (range) {
		// TODO: check if range is a union class
		list.push(range.id);
	} 
}

PropertyOverview.prototype.analyzeDomain = function() {
	var list = this.domainList = [];
	var domain = this.propertyVertex.v().out(rdfs.domain).first();
	if (domain) {
		// TODO: check if domain is a union class
		list.push(domain.id);
	}
}

/*****************************************************************************/
function PropertyManager(graph) {
	this.graph = graph;
	this.propertyMap = {};
}

PropertyManager.prototype.getPropertyOverview = function(propertyId) {
	var propertyVertex = this.graph.vertex(propertyId);
	
	var key = propertyVertex.id.stringValue;
	
	var overview = this.propertyMap[key];
	if (!overview) {
		// TODO: compute range and domain
		overview = new PropertyOverview(propertyVertex);
		this.propertyMap[key] = overview;
	}
	
	return overview;
}
/*****************************************************************************/
function PropertyInfo(predicate, expectedType, minCount, maxCount, description) {
	this.predicate = predicate;
	this.propertyName = predicate.localName;
	this.expectedType = expectedType;
	this.minCount = minCount;
	this.maxCount = maxCount;
	this.description = description;
	this.or = false;
	
	if (expectedType.length) {
		expectedType[expectedType.length-1].last = true;
	}
}

PropertyInfo.addType = function(list, typeVertex) {
	var type = typeVertex.id;
	
	if (type instanceof IRI) {

		var value = type.stringValue;
		for (var i=0; i<list.length; i++) {
			var item = list[i];
			if (item.stringValue === value) {
				return;
			}
		}
		list.push({
			stringValue: type.stringValue,
			localName: type.localName
		});
		
	} else {
		var unionVertex = typeVertex.v().out(owl.unionOf).first();
		if (unionVertex) {
			var union = unionVertex.toList();
			for (var i=0; i<union.length; i++) {
				PropertyInfo.addType(list, union[i]);
			}
		}
	}
}

/*****************************************************************************/
function PropertyBlock(fromClassVertex) {
	this.fromClassVertex = fromClassVertex;
	this.blockSize = 0;
	this.propertyList = [];
}

PropertyBlock.prototype.propertyInfoForPredicate = function(predicate) {
	var idValue = rdf.node(predicate).stringValue;
	for (var i=0; i<this.propertyList.length; i++) {
		var info = this.propertyList[i];
		if (info.predicate.stringValue === idValue) {
			return info;
		}
	}
	return null;
}
/*****************************************************************************/
function RenameClass(ontodoc, oldIRI, newIRI) {
	this.ontodoc = ontodoc;
	this.oldIRI = oldIRI;
	this.newIRI = newIRI;
}

RenameClass.prototype.redo = function() {
	this.ontodoc.renameClass(oldIRI, newIRI);
}
/*****************************************************************************/	
function HierarchyInfo(shapeInfo) {
	this.shapeInfo = shapeInfo;
	this.child = [];
	this.parent = [];
}


/*****************************************************************************/	
function OntologyInfo(manager, ontologyVertex) {
	this.manager = manager;
	this.vertex = ontologyVertex;
	this.label = manager.ontologyLabel(ontologyVertex);
	this.description = this.ontologyDescription();
	this.namespaceAddress = ontologyVertex.id.stringValue;
	this.namespacePrefix = this.ontologyPrefix();
	this.classList = [];
	this.propertyList = [];
}

OntologyInfo.prototype.ontologyPrefix = function() {
	var prefix = this.vertex.v().out(vann.preferredNamespacePrefix).first();
	if (prefix) {
		return prefix.stringValue;
	}
	
	// TODO: Refactor to support a list of contexts
	var inverse = this.manager.context.inverse();
	var term = inverse[this.vertex.id.stringValue];
	return term ? term : "";
}

OntologyInfo.prototype.ontologyDescription = function() {
	// TODO: handle multiple languages
	var description = this.vertex.v().out(schema.description, dcterms.description, rdfs.comment).first();
	return description ? description.stringValue : null;
}

/*****************************************************************************/
function OntologyManager(context, graph) {
	this.context = context;
	this.graph = graph;
	this.ontologyMap = {};
	this.ontologyList = [];
	this.init();
}

OntologyManager.prototype.init = function() {

	var ontoList = this.graph.V(owl.Ontology).inward(rdf.type).toList();
	for (var i=0; i<ontoList.length; i++) {
		var ontoVertex = ontoList[i];
		
		var info = new OntologyInfo(this, ontoVertex);
		this.ontologyMap[ontoVertex.id.stringValue] = info;
		this.ontologyList.push(info);
	}
	this.ontologyList.sort(function(a,b){
		return a.label.localeCompare(b.label);
	});
	this.analyzeProperties();
}

OntologyManager.prototype.analyzeProperties = function() {
	var map = {};
	
	var list = this.graph.V(
		owl.ObjectProperty, rdf.Property, owl.DatatypeProperty, owl.FunctionalProperty, owl.InverseFunctionalProperty, 
		owl.SymmetricProperty, owl.TransitiveProperty).inward(rdf.type).toList();
	
	list.sort(function(a,b){
		return a.id.localName.localeCompare(b.id.localName);
	});
	
	for (var i=0; i<list.length; i++) {
		var propertyId = list[i].id;
		if (!map[propertyId.stringValue]) {
			map[propertyId.stringValue] = propertyId;

			var ontologyInfo = this.getOntologyInfo(propertyId.namespace);
			ontologyInfo.propertyList.push(propertyId);

		}
	}
	
}

OntologyManager.prototype.getOntologyInfo = function(ontologyId) {
	var ontologyVertex = this.graph.vertex(ontologyId);
	var key = ontologyVertex.id.stringValue;
	var info = this.ontologyMap[key];
	if (!info) {
		info = new OntologyInfo(this, ontologyVertex);
		this.ontologyMap[key] = info;
	}
	return info;
}


OntologyManager.prototype.ontologyLabel = function(ontology) {
	
	var label = ontology.v().out(rdfs.label, dcterms.title, dc.title).first();
	if (label) {
		return label.stringValue;
	}
	
	return ontology.id.stringValue;
	
}

/*****************************************************************************/
function ClassManager(ontologyManager, graph) {
	this.ontologyManager = ontologyManager;
	this.graph = graph;
	this.classMap = {};
	this.classList = [];
	this.shapeMap = {};
	this.init();
}

ClassManager.prototype.init = function() {
	var classList = this.graph.V(rdfs.Class, owl.Class).inwardTransitiveClosure(rdfs.subClassOf).inward(rdf.type)
		.nodeKind(sh.IRI).toList();
	for (var i=0; i<classList.length; i++) {
		var owlClass = classList[i];
		var classInfo = this.getOrCreateClassInfo(owlClass);
	}
	

	this.classList.sort(function(a,b){
		return a.classVertex.id.localName.localeCompare(b.classVertex.id.localName);
	});
	
	this.assignUniqueNames();
}



ClassManager.prototype.assignUniqueNames = function() {
	var list = this.classList;
	var prevLocalName = null;
	for (var i=0; i<list.length; i++) {
		var classInfo = list[i];
		var localName = classInfo.classVertex.id.localName;
		classInfo.uniqueName = localName;
		if (
			(localName === prevLocalName) ||
			(
				(i<list.length-1) && 
				(list[i+1].classVertex.id.localName === localName)
			)
		) {
			var namespace = classInfo.classVertex.id.namespace;
			var ontoInfo = this.ontologyManager.getOntologyInfo(namespace);
			var prefix = ontoInfo.namespacePrefix;
			if (prefix) {
				classInfo.uniqueName = prefix + ":" + localName;
			}
		} 
		
		prevLocalName = localName;
		
	}
}

ClassManager.prototype.getOrCreateClassInfo = function(owlClass) {
	var classId = rdf.node(owlClass);
	var classInfo = this.classMap[classId.stringValue];
	if (classInfo == null) {
		classInfo = new ClassInfo(owlClass, this);
		this.classMap[classId.stringValue] = classInfo;
		this.classList.push(classInfo);
		// TODO: remove the next line to support lazy creation of shapeInfo
//		classInfo.getLogicalShape();
	}
	return classInfo;
}

ClassManager.prototype.getOrCreateShapeInfo = function(shape) {
	var shapeId = rdf.node(shape);
	var shapeInfo = this.shapeMap[shapeId.stringValue];
	if (!shapeInfo) {
		shape = this.graph.vertex(shape);
		
		var scopeClass = shape.v().out(sh.scopeClass).first();
		if (!scopeClass) {
			console.log("scopeClass not defined for shape: " + shapeId.stringValue);
			return null;
		}
		
		var classInfo = this.getOrCreateClassInfo(scopeClass);
		
		shapeInfo = new ShapeInfo(classInfo, shape);
		this.shapeMap[shapeId.stringValue] = shapeInfo;
	}
	
	return shapeInfo;
}


ClassManager.prototype.logicalShapeName = function(owlClass) {
	var uri = rdf.node(owlClass);
	var prefixNode = this.graph.V(uri.namespace).out(vann.preferredNamespacePrefix).first(); 
	var prefix = prefixNode ? prefixNode.stringValue : "default";
	
	return "http://www.konig.io/shape/logical." + prefix + "." + uri.localName;
	
}

/*****************************************************************************/
function ClassInfo(owlClass, classManager) {
	var graph = classManager.graph;
	this.classVertex = graph.vertex(owlClass);
	this.classManager = classManager;
	this.analyzeComment();
}

ClassInfo.prototype.getSubClassList = function() {
	if (!this.subClassList) {
		this.subClassList = this.classVertex.v().inward(rdfs.subClassOf).toList();
	}
	
	return this.subClassList;
}

ClassInfo.prototype.analyzeComment = function() {
	var comment = this.classVertex.v().out(rdfs.comment, dcterms.description, dc.description).first();
	if (comment) {
		this.comment = comment.stringValue;
	}
}

ClassInfo.prototype.getMediaTypeList = function() {
	if (!this.mediaTypeList) {
		var sink = this.mediaTypeList = [];
		var shapeList = this.classVertex.v().inward(sh.scopeClass).toList();
		
		for (var i=0; i<shapeList.length; i++) {
			var shape = shapeList[i];
			var name = shape.v().out(kol.mediaTypeBaseName).first();
			if (!name) {
				// TODO: Generate media type name
				console.log("Failed to find mediaTypeBaseName for shape " + shape.id.stringValue);
			} else {
				sink.push({ mediaTypeName: name.stringValue});
			}
		}
		
	}
	
	return this.mediaTypeList;
}

ClassInfo.prototype.getLogicalShape = function() {
	if (!this.logicalShape) {
		var logicalShape = this.classVertex.v().inward(sh.scopeClass).hasType(kol.LogicalShape).first();
		if (!logicalShape) {

			// Get any old shape and treat it as the logical shape.
			// This is a temporary hack.
			// If there are other shapes we ought to merge the property constraints
			// into the OWL description
			
			// BEGIN HACK
//			logicalShape = this.classVertex.v().inward(sh.scopeClass).first();
//			if (logicalShape) {
//				console.log("Found a pre-defined shape");
//				this.logicalShape = this.classManager.getOrCreateShapeInfo(logicalShape);
//				return this.logicalShape;
//			}
//			console.log("No pre-defined Shape was found for class " + this.classVertex.id.stringValue);
			// END HACK
			
			var graph = this.classManager.graph;

			var owlClass = this.classVertex;
			var shapeName = this.classManager.logicalShapeName(owlClass);
			logicalShape = graph.vertex(shapeName);
			graph.statement(logicalShape, sh.scopeClass, owlClass);

			var isLiteral = owlClass.v().hasTransitive(rdfs.subClassOf, rdfs.Literal).first();
			if (!isLiteral) {
				this.addDirectProperties(owlClass, logicalShape);
				var classMap = {};
				this.addSuperProperties(owlClass, logicalShape, classMap);
				this.addShapeProperties(owlClass, logicalShape);
			}
		}
		this.logicalShape = this.classManager.getOrCreateShapeInfo(logicalShape);
	}
	return this.logicalShape;
}

ClassInfo.prototype.addShapeProperties = function(owlClass, logicalVertex) {
	var graph = this.classManager.graph;
	
	// Get the list of shapes that have owlClass as the scopeClass
	var shapeList = owlClass.v().inward(sh.scopeClass).toList();
	
	console.log(logicalVertex.toJson());
	
	for (var i=0; i<shapeList.length; i++) {
		var shape = shapeList[i];
		var propertyList = shape.v().out(sh.property).toList();
		for (var j=0; j<propertyList.length; j++) {
			var property = propertyList[j];
			var predicate = property.propertyValue(sh.predicate);
			console.log(predicate.stringValue);
			// Do we have an existing PropertyConstraint for the given predicate?
			var prior = logicalVertex.v().out(sh.property).has(sh.predicate, predicate).first();
			if (!prior) {
				// There is no existing PropertyConstraint for the given predicate.
				// Clone the one that we found, and add it to the logical shape.
				
				var clone = property.shallowClone();
				graph.statement(logicalVertex, sh.property, clone);
			} else {
				console.log("TODO: merge multiple property constraints");
			}
		}
	}
	
}


ClassInfo.prototype.addSuperProperties = function(owlClass, shape, classMap) {
	var manager = this.classManager;
	var graph = manager.graph;
	
	var superList = owlClass.v().out(rdfs.subClassOf).toList();
	if (superList.length > 0) {
		var andConstraint = graph.vertex();
		graph.statement(shape, sh.constraint, andConstraint);
	
		var listVertex = graph.vertex();
		graph.statement(andConstraint, sh.and, listVertex);

		var sink = listVertex.toList();
		
		for (var i=0; i<superList.length; i++) {
			var supertype = superList[i];
			if (!classMap[supertype.id.stringValue]) {
				classMap[supertype.id.stringValue] = supertype;
				
				var superInfo = manager.getOrCreateClassInfo(supertype);
				var logicalShape = superInfo.getLogicalShape();
				sink.push(logicalShape.rawShape);
			}
		}
	}
}

ClassInfo.prototype.addDirectProperties = function(owlClass, shape) {
	
	var graph = this.classManager.graph;
	var propertyList = owlClass.v().inward(rdfs.domain, schema.domainIncludes).unique().toList();
	
	
	for (var i=0; i<propertyList.length; i++) {
		this.addPropertyConstraint(shape, propertyList[i]);
	}
	
	
}

var appendUnique = function(list, element) {
	if (element) {
		var node = rdf.node(element);
		for (var i=0; i<list.length; i++) {
			var n = rdf.node(list[i]);
			if (n.stringValue === node.stringValue) {
				return;
			}
		}
		list.push(element);		
	}
}

ClassInfo.prototype.addPropertyConstraint = function(shape, property) {
	var predicate = rdf.node(property);
	var graph = this.classManager.graph;
	
	var result = shape.v().out(sh.property).has(sh.predicate, predicate).first();
	if (!result) {
		result = graph.vertex();
		graph.statement(shape, sh.property, result);
		graph.statement(result, sh.predicate, predicate);
		
		if (property.v().hasType(owl.FunctionalProperty).first()) {
			graph.statement(result, sh.maxCount, 1);
		}
		
		var description = Ontodoc.shaclDescription(property);
		if (description) {
			graph.statement(result, sh.description, description);
		}
		
		var propertyType = this.propertyType(property);
		var range = this.rangeValue(property);
			
		if (propertyType.equals(owl.DatatypeProperty) || range.v().instanceOf(rdfs.Datatype)) {
			graph.statement(result, sh.datatype, range);
		} else {
			graph.statement(result, sh.valueClass, range);
		}
	}
		
		
	
}

ClassInfo.prototype.rangeValue = function(property) {
	var rangeIncludes = property.v().out(schema.rangeIncludes).toList();
	appendUnique(rangeIncludes, property.v().out(rdfs.range).first());
	
	if (rangeIncludes.length == 0) {
		return owl.Thing;
	}
	
	if (rangeIncludes.length == 1) {
		return rangeIncludes[0];
	}

	var graph = this.classManager.graph;
	
	var unionClass = graph.vertex();
	var listVertex = graph.vertex();
	graph.statement(unionClass, rdf.type, owl.Class);
	graph.statement(unionClass, owl.unionOf, listVertex);
	listVertex.elements = rangeIncludes;
	
	return unionClass;
}

ClassInfo.prototype.propertyType = function(property) {
	if (property.v().hasType(owl.ObjectProperty).first()) {
		return owl.ObjectProperty;
	}
	
	if (property.v().hasType(owl.DatatypeProperty).first()) {
		return owl.DatatypeProperty;
	}
	return rdf.Property;
}


/*****************************************************************************/	
function ShapeInfo(classInfo, rawShape) {
	this.classInfo = classInfo;
	this.rawShape = rawShape;
	this.init();
}

ShapeInfo.prototype.init = function() {

	this.propertyBlock = [];

	this.addDirectProperties();
	this.addSuperProperties(this.rawShape, true);
}

ShapeInfo.prototype.addSuperProperties = function(shape, isDirect) {
	if (!shape) return;
	var constraint = shape.v().out(sh.constraint).first();
	if (constraint) {
		var and = constraint.v().out(sh.and).first();
		if (and) {
			this.analyzeAnd(and, isDirect);
		} else {
			var or = contraint.v().out(sh.or).first();
			if (or) {
				this.analyzeOr(or, isDirect);
			} else {
				var not = constraint.v().out(sh.not).first();
				if (not) {
					this.analyzeNot(not, isDirect);
				} else {
					// TODO: analyze custom constraint
				}
			}
		}
	}
	
}

ShapeInfo.prototype.propertyBlockForClass = function(owlClassVertex) {
	var classId = rdf.node(owlClassVertex);
	var list = this.propertyBlock;
	for (var i=0; i<list.length; i++) {
		var block = list[i];
		if (block.fromClassVertex === owlClassVertex) {
			return block;
		}
	}
	
	var block = new PropertyBlock(owlClassVertex);
	list.push(block);
	
	return block;
	
}

ShapeInfo.prototype.analyzeAnd = function(and, isDirect) {
	var classManager = this.classInfo.classManager;
	var andList = and.toList();
	for (var i=0; i<andList.length; i++) {
		var shape = andList[i];
		
		if (shape.id instanceof IRI) {
			shapeInfo = classManager.getOrCreateShapeInfo(shape.id);
			this.copyPropertyBlocks(shapeInfo);
			
			
		} else {
			console.log("TODO: add local properties from and clause")
		}
	}
}

ShapeInfo.prototype.copyPropertyBlocks = function(shapeInfo) {
	var blockList = shapeInfo.propertyBlock;
	var directBlock = this.directProperties;
	for (var i=0; i<blockList.length; i++) {
		var block = blockList[i];
		var list = block.propertyList;
		if (list.length > 0) {
			var sinkBlock = this.propertyBlockForClass(block.fromClassVertex);
			var sink = sinkBlock.propertyList;
			
			for (var j=0; j<list.length; j++) {
				
				var propertyInfo = list[j];
				
				if (directBlock) {
					var prior = directBlock.propertyInfoForPredicate(propertyInfo.predicate);
					if (prior) {
						continue;
					}
				}
				sink.push(propertyInfo);
			}
			sinkBlock.blockSize = sink.length;
		}
	}
}

ShapeInfo.prototype.analyzeOr = function(or, isDirect) {
	console.log("TODO: implement analyzeOr");
}

ShapeInfo.prototype.analyzeNot = function(not, isDirect) {
	console.log("TODO: implement analyzeNot");
}


ShapeInfo.prototype.addDirectProperties = function() {
	var classManager = this.classInfo.classManager;
	var owlClass = this.classInfo.classVertex;
	var block = new PropertyBlock(owlClass);
	var sink = block.propertyList;
	
	var source = this.rawShape.v().out(sh.property).toList();
	for (var i=0; i<source.length; i++) {
		var property = source[i];
		var description = Ontodoc.shaclDescription(property);
		var predicate = property.v().out(sh.predicate).first();
		var expectedType = [];
		var datatype = property.v().out(sh.datatype).first();
		var objectType = property.v().out(sh.objectType).first();
		var directValueType = property.v().out(sh.directValueType).first();
		var valueClass = property.v().out(sh.valueClass).first();
		var valueShape = property.v().out(sh.valueShape).first();
		var minCount = property.v().out(sh.minCount).first();
		var maxCount = property.v().out(sh.maxCount).first();
		if (datatype) {
			PropertyInfo.addType(expectedType, datatype);
		} else if (objectType) {
			PropertyInfo.addType(expectedType, objectType);
		} else if (directValueType) {
			PropertyInfo.addType(expectedType, directValueType);
		} else if (valueClass) {
			PropertyInfo.addType(expectedType, valueClass);
		} else if (valueShape) {
			var scopeClass = valueShape.v().out(sh.scopeClass).first();
			if (!scopeClass) {
				// TODO: surface this warning in the user interface
				console.log("WARNING: Scope class not found for valueShape of  " + predicate.id.localName + " on " + shape.id.stringValue);
				scopeClass = this.graph.vertex(owl.Thing);
			}
			classManager.getOrCreateShapeInfo(scopeClass);
			PropertyInfo.addType(expectedType, scopeClass);
		}

		var propertyInfo = new PropertyInfo(
			predicate.id, expectedType, minCount, maxCount, description
		);
		
		sink.push(propertyInfo);
	}
	if (sink.length > 0) {
		this.directProperties = block;
		
		this.propertyBlock.push(block);
		block.blockSize = sink.length;
	}
	
}

/*****************************************************************************/	
function Ontodoc(ontologyService) {
	
	this.actionHistory = new HistoryManager();
	this.editMode = false;
	this.multipleShapesPerClass = false;
	this.ontologyService = ontologyService || konig.ontologyService;
	this.layout = $('body').layout(
		{
			applyDefaultStyles:true,
			north: {
				resizable: true,
				closable: false
			},
			west: {
				size: "250"
			}
		}
	);
	$('#navigation').layout({
		applyDefaultStyles: true,
		north: {
			size: "200"
		}
	});
	
	this.ontologyGraph = this.ontologyService.getOntologyGraph();
	
	this.context = new konig.jsonld.Context(this.ontologyGraph['@context']);
	
	this.buildGraph();
	this.inferClasses();
	
	this.ontologyManager = new OntologyManager(this.context, this.graph);
	
	this.classManager = new ClassManager(this.ontologyManager, this.graph);
	this.propertyManager = new PropertyManager(this.graph);
	
//	this.buildClassMap();
//	this.analyzeProperties();
//	this.analyzeSuperProperties();
	this.inferOntologies();
	this.renderOntologyList();
	this.renderClassList();

	this.classTemplate = $('#ontodoc-class-template').html();
	this.ontologyTemplate = $('#ontodoc-ontology-template').html();
	this.classEditTemplate = $('#ontodoc-class-edit-template').html();
	this.propertyTemplate = $('#ontodoc-property-template').html();
	
	var self = this;
	$(window).bind("hashchange", function(event){
		self.onHashChange();
	});
	$(window).trigger('hashchange');
	
	
	
}

/**
 * Given statements of the form:
 * <pre>
 *    ?x sh:scopeClass ?y
 * </pre>
 * 
 * infer
 * <pre>
 *    ?y rdf:type owl:Class
 * </pre>
 */
Ontodoc.prototype.inferClasses = function() {
	var graph = this.graph;
	var shapeList = graph.V(sh.Shape).inward(rdf.type).toList();
	for (var i=0; i<shapeList.length; i++) {
		var shape = shapeList[i];
		var scopeClass = shape.propertyValue(sh.scopeClass);
		if (scopeClass) {
			graph.statement(scopeClass, rdf.type, owl.Class);
		}
	}
	
}


Ontodoc.shaclDescription = function(vertex) {
	// TODO: support multiple languages
	
	var description = vertex.v().out(sh.description).first();
	if (description == null) {
		description = vertex.v().out(rdfs.comment).first();
	}
	
	return description;
}

Ontodoc.prototype.analyzeSuperProperties = function() {

	var list = this.shapeInfoList();
	for (var i=0; i<list.length; i++) {
		var shapeInfo = list[i];
		var map = {};
		this.mapProperties(shapeInfo, map);
		this.addSuperProperties(shapeInfo, map);
	}
}

Ontodoc.prototype.mapProperties = function(shapeInfo, map) {
	var list = shapeInfo.propertyBlock;
	map[shapeInfo.owlClass.stringValue] = shapeInfo;
	if (list) {
		for (var i=0; i<list.length; i++) {
			var block = list[i];
			for (var j=0; j<block.propertyList.length; j++) {
				var propertyInfo = block.propertyList[j];
				map[propertyInfo.predicate.stringValue] = propertyInfo;
			}
		}
	}
}

Ontodoc.prototype.addSuperProperties = function(shapeInfo, map) {

}

Ontodoc.prototype.onHashChange = function() {
	var location = window.location;
	var hash = location.hash;
	if (hash) {
		hash = hash.substring(1);
		var vertex = this.graph.vertex(hash, true);
		if (!vertex) {
			// TODO: fetch the resource via an ajax call.
		} else {
			
			if (vertex.instanceOf(owl.Ontology)) {
				this.renderOntology(vertex);
			} else if (vertex.instanceOf(owl.Class) || vertex.instanceOf(rdfs.Class)) {
				this.renderClass(hash);
			} else if (vertex.instanceOf(rdf.Property)) {
				this.renderProperty(vertex);
			}
			
//			var typeList = vertex.v().out(rdf.type).toList();
//			for (var i=0; i<typeList.length; i++) {
//				var type = typeList[i];
//				if (type.equals(owl.Ontology)) {
//					this.renderOntology(vertex);
//				} else if (type.equals(owl.Class)) {
//					this.renderClass(hash);
//				}
//			}
		}
	}
}

Ontodoc.prototype.getShapeInfo = function(owlClassIRI) {
	var key = stringValue(owlClassIRI);
	return this.classMap[key];
}

Ontodoc.prototype.buildHierarchy = function(owlClassIRI) {
	var path =
		this.graph.V(owlClassIRI).until(hasNot(rdfs.subClassOf)).repeat(outV(rdfs.subClassOf)).path();
}

Ontodoc.prototype.buildGraph = function() {

	this.graph = rdf.graph();
	var doc = this.ontologyGraph;
	var context = this.context;
	
	var expand = context.expand(doc);
	var flat = context.flatten(expand);
	this.graph.loadFlattened(flat['@graph']);
}




Ontodoc.prototype.isLiteralClass = function(owlClass) {
	if (!owlClass) {
		return false;
	}
	var uri = rdf.node(owlClass);
	if (xsd.NAMESPACE === uri.namespace) {
		return true;
	}
	// TODO: Implement more robust test for Literal class
	return false;
}


Ontodoc.prototype.shapeInfoList = function() {
	var list = [];

	for (var key in this.classMap) {
		list.push(this.classMap[key]);
	}
	return list;
}

Ontodoc.prototype.analyzeProperties = function() {
	
	var list = this.shapeInfoList();
	
	while (list.length > 0) {
		this.extraClasses = [];
		for (var i=0; i<list.length; i++) {
			var info = list[i];
			this.buildProperties(info);
		}
		
		list = this.extraClasses;
	}
	
	delete this.extraClasses;
}

Ontodoc.prototype.buildProperties = function(shapeInfo) {
	
	// Get the list of shapes that reference the specified OWL class.
	var shapeList = this.graph.V(shapeInfo.owlClass).inward(sh.scopeClass).toList();
	for (var i=0; i<shapeList.length; i++) {
		this.addPropertiesFromShape(shapeInfo, shapeList[i]);
	}
}

Ontodoc.prototype.addPropertiesFromShape = function(shapeInfo, shape) {
	var owlClass = shapeInfo.owlClass;
	if (!shapeInfo.localProperties) {
		shapeInfo.localProperties = new PropertyBlock(owlClass);
	}
	if (!shapeInfo.propertyBlock) {
		shapeInfo.propertyBlock = [];
	}
	var propertyBlock = shapeInfo.localProperties;
	shapeInfo.propertyBlock.push(propertyBlock);
	
	var propertyList = shape.v().out(sh.property).toList();
	for (var i=0; i<propertyList.length; i++) {
		
		var property = propertyList[i];
		var predicate = property.v().out(sh.predicate).first();
		
		if (predicate) {
			// TODO: check to see if a PropertyInfo already exists for the specified predicate.
			// Don't create a new one if it already exists.
			
			// TODO: Store the Shape specific details separately from the localProperties.
			
			var expectedType = [];
			var datatype = property.v().out(sh.datatype).first();
			var objectType = property.v().out(sh.objectType).first();
			var directValueType = property.v().out(sh.directValueType).first();
			var valueClass = property.v().out(sh.valueClass).first();
			var valueShape = property.v().out(sh.valueShape).first();
			var minCount = property.v().out(sh.minCount).first();
			var maxCount = property.v().out(sh.maxCount).first();
			// TODO: support description in multiple languages
			var description = property.v().out(sh.description).first();
			if (datatype) {
				PropertyInfo.addType(expectedType, datatype);
			} else if (objectType) {
				PropertyInfo.addType(expectedType, objectType);
			} else if (directValueType) {
				PropertyInfo.addType(expectedType, directValueType);
			} else if (valueClass) {
				PropertyInfo.addType(expectedType, valueClass);
			} else if (valueShape) {
				var scopeClass = valueShape.v().out(sh.scopeClass).first();
				if (!scopeClass) {
					// TODO: surface this warning in the user interface
					console.log("WARNING: Scope class not found for valueShape of  " + predicate.id.localName + " on " + shape.id.stringValue);
					scopeClass = this.graph.vertex(owl.Thing);
				}
				this.getOrCreateShapeInfo(scopeClass);
				PropertyInfo.addType(expectedType, scopeClass);
			}

			var propertyInfo = new PropertyInfo(
				predicate.id, expectedType, minCount, maxCount, description
			);
			propertyBlock.propertyList.push(propertyInfo);
		}
		
	}
}


Ontodoc.prototype.renderProperty = function(propertyVertex) {
	var overview = this.propertyManager.getPropertyOverview(propertyVertex);
	
	var rendered = Mustache.render(this.propertyTemplate, overview);
	$(".ontodoc-main-content").empty().append(rendered);
}

Ontodoc.prototype.renderOntology = function(ontologyId) {
	
	var info = this.ontologyManager.getOntologyInfo(ontologyId);
	
	var rendered = Mustache.render(this.ontologyTemplate, info);
	$(".ontodoc-main-content").empty().append(rendered);
	
}

Ontodoc.prototype.renderClass = function(owlClassIRI) {
	
	var classInfo = this.classManager.classMap[owlClassIRI];
	if (classInfo) {
	
		var shapeInfo = classInfo.getLogicalShape();
		
		shapeInfo.subClassList = classInfo.getSubClassList();
		
		if (!shapeInfo.comment) {
			shapeInfo.comment = classInfo.comment;
		}
		
		if (this.multipleShapesPerClass) {
			shapeInfo.mediaTypeList = classInfo.getMediaTypeList();
		}
		
		var rendered = this.editMode ?
			Mustache.render(this.classEditTemplate, shapeInfo) :
			Mustache.render(this.classTemplate, shapeInfo);
		
		$(".ontodoc-main-content").empty().append(rendered);
		this.renderClassBreadcrumbs(owlClassIRI);
		
		var self = this;
		$(".prop-ect a[resource]").each(function(index, element){
			var e = $(this);
			var className = e.attr("resource");
			e.click(self.clickClassName(className));
		});
		
		if (this.editMode) {
			this.editClass(shapeInfo);
		}
	}
	
	$(".ontodoc-main-content")[0].scrollTop = 0;
	
}



Ontodoc.prototype.renderClassBreadcrumbs = function(owlClassIRI) {
	var classManager = this.classManager;
	var breadcrumbs = $('.ontodoc-class .breadcrumbs');
	
	var pathList = this.graph
		.V(owlClassIRI).until(step().hasNot(rdfs.subClassOf))
		.repeat(step().out(rdfs.subClassOf)).path().execute();
	
	if (pathList.length == 1) {
		var path = pathList[0];
		for (var i=path.length-1; i>=0; i--) {
			var vertex = path[i];
			
			var info = classManager.getOrCreateClassInfo(vertex);
			var anchor = Mustache.render('<a href="#{{href}}" title="{{href}}">{{localName}}</a>', {
				localName: info.uniqueName,
				href: vertex.id.stringValue
			});
			
			if (i!=path.length-1) {
				breadcrumbs.append("<span> &gt; </span>");
			}
			breadcrumbs.append(anchor);
		}
	}
	
}

Ontodoc.prototype.inferOntologies = function() {
	var classMap = this.classManager.classMap;
	
	for (var key in classMap) {
		var info = classMap[key];
		var owlClass = info.classVertex;
		
		var namespace = this.graph.vertex(owlClass.id.namespace);
		
		if (namespace.v().hasType(owl.Ontology).toList().length==0) {
			namespace.v().addType(owl.Ontology).execute();
		}
		
	}
}

Ontodoc.prototype.renderOntologyList = function() {
	var container = $('#ontodoc-ontology-list');
	
	var infoList = this.ontologyManager.ontologyList;
	for (var i=0; i<infoList.length; i++) {

		var info = infoList[i];
		var anchor = Mustache.render(
			'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
			href: info.vertex.id.stringValue,
			label: info.label
		});
		container.append(anchor);
	}
	
//	var ontoList = this.graph.V(owl.Ontology).inward(rdf.type).toList();
//	for (var i=0; i<ontoList.length; i++) {
//		var onto = ontoList[i];
//		var label = this.ontologyLabel(onto);
//		
//		var anchor = Mustache.render(
//			'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
//			href: onto.id.stringValue,
//			label: label
//		});
//		
//		
//		container.append(anchor);
//		
//	}
}


Ontodoc.prototype.renderClassList = function() {
	
	var list = this.classManager.classList;
	
	var container = $("#ontodoc-class-list");
	for (var i=0; i<list.length; i++) {
		var classInfo = list[i];
		var classId = classInfo.classVertex.id;
		var ontologyInfo = this.ontologyManager.getOntologyInfo(classId.namespace);
		ontologyInfo.classList.push(classId);
		
		var anchor = Mustache.render(
				'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
				href: classId.stringValue,
				label: classInfo.uniqueName
			});
		
		container.append(anchor);
		
	}
}

Ontodoc.prototype.clickClassName = function(owlClass) {
	var self = this;
	return function() {
		self.renderClass(owlClass);
	}
}

konig.Ontodoc = Ontodoc;
konig.buildOntodoc = function(ontologyService) {
	konig.ontodoc = new Ontodoc(ontologyService);
}
konig.ShapeInfo = ShapeInfo;
konig.PropertyBlock = PropertyBlock;
	
	
});
