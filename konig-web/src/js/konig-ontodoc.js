$(document).ready(function(){
	

var rdf = konig.rdf;
var owl = konig.owl;
var rdfs = konig.rdfs;
var vann = konig.vann;
var dcterms = konig.dcterms;
var schema = konig.schema;
var sh = konig.sh;
var step = rdf.step;
var stringValue = rdf.stringValue;
var IRI = rdf.IRI;
var HistoryManager = konig.HistoryManager;

/*****************************************************************************/
var SHACL = {
	Shape: new IRI("http://www.w3.org/ns/shacl#Shape")
};

var OWL = {
	Ontology: new IRI("http://www.w3.org/2002/07/owl#Ontology")	
};
/*****************************************************************************/
function PropertyInfo(predicate, expectedType, minCount, maxCount, description) {
	this.predicate = predicate;
	this.propertyName = predicate.localName;
	this.expectedType = expectedType;
	this.minCount = minCount;
	this.maxCount = maxCount;
	this.description = description;
	this.or = false;
}
/*****************************************************************************/
function PropertyBlock(fromClass) {
	this.fromClass = fromClass;
	this.propertyList = [];
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
function HierarchyInfo(classInfo) {
	this.classInfo = classInfo;
	this.child = [];
	this.parent = [];
}


/*****************************************************************************/	
function OntologyInfo(ontodoc, ontologyVertex) {
	this.ontodoc = ontodoc;
	this.vertex = ontologyVertex;
	this.label = ontodoc.ontologyLabel(ontologyVertex);
	this.description = this.ontologyDescription();
	this.namespaceAddress = ontologyVertex.id.stringValue;
	this.namespacePrefix = this.ontologyPrefix();
}

OntologyInfo.prototype.ontologyPrefix = function() {
	var prefix = this.vertex.v().out(vann.preferredNamespacePrefix).first();
	if (prefix) {
		return prefix.stringValue;
	}
	
	// TODO: Refactor to support a list of contexts
	var inverse = this.ontodoc.context.inverse();
	var term = inverse[this.vertex.id.stringValue];
	return term ? term : "";
}

OntologyInfo.prototype.ontologyDescription = function() {
	// TODO: handle multiple languages
	var description = this.vertex.v().out(schema.description, dcterms.description, rdfs.comment).first();
	return description ? description.stringValue : null;
}

/*****************************************************************************/	
function ClassInfo(owlClass, canonicalShape) {
	this.owlClass = owlClass;
	this.localName = owlClass.localName;
	this.canonicalShape =  canonicalShape;
	this.propertyBlock = null;
}
/*****************************************************************************/	
function Ontodoc(ontologyService) {
	
	this.actionHistory = new HistoryManager();
	this.editMode = false;
	
	this.ontologyService = ontologyService || konig.ontologyService;
	this.layout = $('body').layout(
		{
			applyDefaultStyles:true,
			north: {
				resizable: true,
				closable: false
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
	
	
	this.buildClassMap();
	this.analyzeClasses();
	this.analyzeProperties();
	this.inferOntologies();
	this.renderOntologyList();
	this.renderClassList();

	this.classTemplate = $('#ontodoc-class-template').html();
	this.ontologyTemplate = $('#ontodoc-ontology-template').html();
	this.classEditTemplate = $('#ontodoc-class-edit-template').html();
	
	var self = this;
	$(window).bind("hashchange", function(event){
		self.onHashChange();
	});
	$(window).trigger('hashchange');
	
	
	
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
			var typeList = vertex.v().out(rdf.type).toList();
			for (var i=0; i<typeList.length; i++) {
				var type = typeList[i];
				if (type.equals(owl.Ontology)) {
					this.renderOntology(vertex);
				} else if (type.equals(owl.Class)) {
					this.renderClass(hash);
				}
			}
		}
	}
}

Ontodoc.prototype.getClassInfo = function(owlClassIRI) {
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

Ontodoc.prototype.analyzeClasses = function() {
	
	var owlClassList = this.graph.V(owl.Class).inward(rdf.type).toList();
	for (var i=0; i<owlClassList.length; i++) {
		var owlClass = owlClassList[i];
		this.getOrCreateClassInfo(owlClass);
	}
	var shapeList = this.graph.V(sh.Shape).inward(rdf.type).toList();
	for (var i=0; i<shapeList.length; i++) {
		var shape = shapeList[i];
		var scopeClass = shape.v().out(sh.scopeClass).first();
		if (scopeClass) {
			this.getOrCreateClassInfo(scopeClass);
		}
	}
	
}

Ontodoc.prototype.buildClassMap = function() {
	this.classMap = {};
	this.shapeMap = {};
	var graph = this.ontologyGraph['@graph'];
	for (var i=0; i<graph.length; i++) {
		var shape = graph[i];

		var shapeId = this.context.expandIRI(shape["@id"]);
		
		if (this.context.hasType(shape, SHACL.Shape)) {
			this.shapeMap[shapeId] = shape;
			
		}
		if (shape.scopeClass) {
			var iri = new IRI(this.context.expandIRI(shape.scopeClass));
			var info = this.classMap[iri.stringValue];
			if (info) {
				console.log("TODO: Handle multiple shapes for a given owl class.");
			} else {
				var classInfo = this.getOrCreateClassInfo(iri);
				classInfo.canonicalShape = shape;
			}
		}
	}
	
}

Ontodoc.prototype.classInfoList = function() {
	var list = [];

	for (var key in this.classMap) {
		list.push(this.classMap[key]);
	}
	return list;
}

Ontodoc.prototype.analyzeProperties = function() {
	
	var list = this.classInfoList();
	
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

Ontodoc.prototype.buildProperties = function(classInfo) {
	
	// Get the list of shapes that reference the specified OWL class.
	var shapeList = this.graph.V(classInfo.owlClass).inward(sh.scopeClass).toList();
	for (var i=0; i<shapeList.length; i++) {
		this.addPropertiesFromShape(classInfo, shapeList[i]);
	}
}

Ontodoc.prototype.addPropertiesFromShape = function(classInfo, shape) {
	var owlClass = classInfo.owlClass;
	if (!classInfo.localProperties) {
		classInfo.localProperties = new PropertyBlock(owlClass);
	}
	if (!classInfo.propertyBlock) {
		classInfo.propertyBlock = [];
	}
	var propertyBlock = classInfo.localProperties;
	classInfo.propertyBlock.push(propertyBlock);
	
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
			var directType = property.v().out(sh.directType).first();
			var valueShape = property.v().out(sh.valueShape).first();
			var minCount = property.v().out(sh.minCount).first();
			var maxCount = property.v().out(sh.maxCount).first();
			// TODO: support description in multiple languages
			var description = property.v().out(sh.description).first();
			if (datatype) {
				expectedType.push(datatype.id);
			} else if (objectType) {
				expectedType.push(objectType.id);
			} else if (directType) {
				expectedType.push(directType.id);
			} else if (valueShape) {
				var scopeClass = valueShape.v().out(sh.scopeClass).first();
				if (!scopeClass) {
					// TODO: surface this warning in the user interface
					console.log("WARNING: Scope class not found for valueShape of  " + predicate.id.localName + " on " + shape.id.stringValue);
					scopeClass = this.graph.vertex(owl.Thing);
				}
				this.getOrCreateClassInfo(scopeClass);
				expectedType.push(scopeClass.id);
			}

			var propertyInfo = new PropertyInfo(
				predicate.id, expectedType, minCount, maxCount, description
			);
			propertyBlock.propertyList.push(propertyInfo);
		}
		
	}
}

// TODO: delete this function since it is now obsolete
Ontodoc.prototype.createPropertyBlocks = function(classInfo) {
	if (!classInfo.propertyBlock) {
		classInfo.propertyBlock = [];
		var shape = classInfo.canonicalShape;
		if (!shape) {
			return;
		}
		var list = shape.property;
		if (list) {
			var block = new PropertyBlock(classInfo);
			classInfo.propertyBlock.push(block);
			
			for (var i=0; i<list.length; i++) {
				var p = list[i];
				var predicate = new IRI(this.context.expandIRI(p.predicate));
				var expectedType = [];
				if (p.datatype) {
					var datatype = new IRI(this.context.expandIRI(p.datatype));
					expectedType.push(datatype);
				} else if (p.objectType) {
					expectedType.push(this.getOrCreateClassInfo(p.objectType));
				} else if (p.directType) {
					expectedType.push(this.getOrCreateClassInfo(p.directType));
				} else if (p.valueShape) {
					var valueShapeId = this.context.expandIRI(p.valueShape);
					var valueShape = this.shapeMap[valueShapeId];
					if (!valueShape) {
						console.log("value shape not found: " + valueShapeId);
					} else {
						var scopeClassId = valueShape.scopeClass;
						if (!scopeClassId) {
							console.log("TODO: handle Shape without scopeClass");
						} else {
							scopeClassId = this.context.expandIRI(scopeClassId);
							var valueClassInfo = this.classMap[scopeClassId];
							if (!valueClassInfo) {
								expectedType.push(new IRI(scopeClassId));
							} else {
								expectedType.push(valueClassInfo);
							}
						}
					}
				}
				
				var propertyInfo = new PropertyInfo(
					predicate, expectedType, p.minCount, p.maxCount, p.description
				);
				block.propertyList.push(propertyInfo);
				
			}
		}
	}
}

Ontodoc.prototype.getOrCreateClassInfo = function(owlClass) {
	
	var iri = 
		(owlClass instanceof IRI) ? owlClass :
		(owlClass instanceof Vertex) ? owlClass.id :
		(typeof(owlClass)==="string") ? new IRI(this.context.expandIRI(owlClass)) :
		null;
		
	if (iri == null) {
		throw new Error("Invalid argument: " + owlClass);
	}
	
	var info = this.classMap[iri.stringValue];
	if (!info) {
		info = new ClassInfo(iri);
		this.classMap[iri.stringValue] = info;
		if (this.extraClasses) {
			this.extraClass.push(info);
		}
		var vertex = this.graph.vertex(iri);
		if (!vertex.v().hasType(owl.Class).first()) {
			vertex.v().addType(owl.Class).execute();
		}
	}
	
	return info;
}

Ontodoc.prototype.renderOntology = function(ontologyId) {
	var vertex = this.graph.vertex(ontologyId);
	var info = new OntologyInfo(this, vertex);
	
	var rendered = Mustache.render(this.ontologyTemplate, info);
	$(".ontodoc-main-content").empty().append(rendered);
	
}

Ontodoc.prototype.renderClass = function(owlClassIRI) {
	
	var classInfo = this.classMap[owlClassIRI];
	if (classInfo) {
		var rendered = this.editMode ?
			Mustache.render(this.classEditTemplate, classInfo) :
			Mustache.render(this.classTemplate, classInfo);
		
		$(".ontodoc-main-content").empty().append(rendered);
		this.renderClassBreadcrumbs(owlClassIRI);
		
		var self = this;
		$(".prop-ect a[resource]").each(function(index, element){
			var e = $(this);
			var className = e.attr("resource");
			e.click(self.clickClassName(className));
		});
		
		if (this.editMode) {
			this.editClass(classInfo);
		}
	}
	
	
}



Ontodoc.prototype.renderClassBreadcrumbs = function(owlClassIRI) {
	
	var breadcrumbs = $('.ontodoc-class .breadcrumbs');
	
	var pathList = this.graph
		.V(owlClassIRI).until(step().hasNot(rdfs.subClassOf))
		.repeat(step().out(rdfs.subClassOf)).path().execute();
	
	if (pathList.length == 1) {
		var path = pathList[0];
		for (var i=path.length-1; i>=0; i--) {
			var vertex = path[i];
			
			var anchor = Mustache.render('<a href="#{{href}}">{{localName}}</a>', {
				localName: vertex.id.localName,
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
	for (var key in this.classMap) {
		var info = this.classMap[key];
		var owlClass = this.graph.vertex(info.owlClass);
		
		var namespace = this.graph.vertex(owlClass.id.namespace);
		
		if (namespace.v().hasType(owl.Ontology).toList().length==0) {
			namespace.v().addType(owl.Ontology).execute();
		}
		
	}
}

Ontodoc.prototype.renderOntologyList = function() {
	var container = $('#ontodoc-ontology-list');
	var ontoList = this.graph.V(owl.Ontology).inward(rdf.type).toList();
	for (var i=0; i<ontoList.length; i++) {
		var onto = ontoList[i];
		var label = this.ontologyLabel(onto);
		
		var anchor = Mustache.render(
			'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
			href: onto.id.stringValue,
			label: label
		});
		
		
		container.append(anchor);
		
	}
}

Ontodoc.prototype.ontologyLabel = function(ontology) {
	
	var label = ontology.v().out(rdfs.label).first();
	if (label) {
		return label.stringValue;
	}
	
	return ontology.id.stringValue;
	
}

Ontodoc.prototype.renderClassList = function() {
	
	
	var list = [];
	for (var key in this.classMap) {
		var info = this.classMap[key];
		list.push(info);
	}
	// TODO: sort the list alphabetically
	
	var container = $("#ontodoc-class-list");
	for (var i=0; i<list.length; i++) {
		var info = list[i];
		
		
		var anchor = Mustache.render(
				'<div class="ontodoc-index-entry"><a href="#{{{href}}}">{{label}}</a></div>', {
				href: info.owlClass.stringValue,
				label: info.owlClass.localName
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

konig.ontodoc = new Ontodoc();
konig.ClassInfo = ClassInfo;
konig.PropertyBlock = PropertyBlock;
	
	
});