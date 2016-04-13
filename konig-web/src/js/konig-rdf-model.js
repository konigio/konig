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

	if (!Function.prototype.construct) {
		Function.prototype.construct = function(aArgs) {
			  var fConstructor = this, fNewConstr = function() { fConstructor.apply(this, aArgs); };
			  fNewConstr.prototype = fConstructor.prototype;
			  return new fNewConstr();
			};
	}

RdfNode = function() {}
RdfNode.prototype.equals = function(other) {
	if (other instanceof RdfNode) {
		return this.key() === other.key();
	}
	return false;
}
/*****************************************************************************/
RdfResource = function() {}
RdfResource.prototype = Object.create(RdfNode.prototype);
RdfResource.prototype.key = function() {
	return this.id.stringValue;
}

RdfResource.prototype.serialize = function() {
	return this.stringValue;
}

RdfResource.prototype.equals = function(other) {
	if (other instanceof Vertex) {
		other = other.id;
	}
	
	if (other instanceof RdfNode) {
		return this.key() === other.key();
	}
	if (typeof(other)==='string') {
		return other === this.stringValue;
	}
	return false;
}

RdfResource.prototype.toString = function() {
	return this.stringValue;
}

/*****************************************************************************/
IRI = function(stringValue) {
	this.stringValue = stringValue;
	var delim = 
		Math.max(stringValue.lastIndexOf('#'),stringValue.lastIndexOf('/'));
	
	if (delim >= 0) {
		this.localName = stringValue.substring(delim+1);
		this.namespace = stringValue.substring(0, delim+1);
	}
}

IRI.create = function(value) {
	if (value instanceof IRI) {
		return value;
	}
	return new IRI(value);
}

IRI.prototype = Object.create(RdfResource.prototype);

IRI.prototype.toString = function() {
	return '<' + this.stringValue + '>';
}

IRI.prototype.sameValue = IRI.prototype.equals;

IRI.prototype.isResource = function() {
	return true;
}
IRI.prototype.key = function() {
	return this.stringValue;
}



OWL = {
	CLASS : new IRI('http://www.w3.org/2002/07/owl#Class'),
	NAMED_INDIVIDUAL : new IRI("http://www.w3.org/2002/07/owl#NamedIndividual"),
	THING : new IRI("http://www.w3.org/2002/07/owl#Thing")
	
};
rdfs = {
	LABEL : new IRI('http://www.w3.org/2000/01/rdf-schema#label'),
	subClassOf : new IRI('http://www.w3.org/2000/01/rdf-schema#subClassOf')
}

// Deprecated.  Use rdf.type and rdf.Property instead
RDF = {
	TYPE : new IRI('http://www.w3.org/1999/02/22-rdf-syntax-ns#type'),
	PROPERTY: new IRI('http://www.w3.org/1999/02/22-rdf-syntax-ns#Property')
};

xsd = {
	NAMESPACE: "http://www.w3.org/2001/XMLSchema#",
	int : new IRI('http://www.w3.org/2001/XMLSchema#int'),
	integer : new IRI('http://www.w3.org/2001/XMLSchema#integer'),
	double : new IRI('http://www.w3.org/2001/XMLSchema#double')
}


/*****************************************************************************/

BNode = function(stringValue) {
	this.namespace = '_:';
	var colon = stringValue.indexOf(':');
	if (colon === -1) {
		this.id = stringValue;
		this.stringValue = '_:' + stringValue;
	} else {
		this.stringValue = stringValue;
		this.id = stringValue.substring(colon+1);
	}
}
BNode.prototype = Object.create(RdfResource.prototype);

BNode.prototype.isResource = function() {
	return true;
}

BNode.prototype.stringValue = function() {
	return this.stringValue;
}
BNode.prototype.key = function() {
	return this.stringValue;
}

BNode.prototype.equals = IRI.prototype.equals;
BNode.prototype.sameValue = IRI.prototype.equals;

Literal = function(stringValue) {
	this.type = null;
	this.language = null;
	this.stringValue = stringValue;
}
Literal.prototype = Object.create(RdfNode.prototype);

Literal.prototype.isResource = function() {
	return false;
}

Literal.prototype.serialize = function() {
	if (this.type) {
		
		if (this.type.equals(xsd.double)) {
			return Number.parseFloat(this.stringValue);
		}
		
		return {
			"@type" : this.type.serialize(),
			"@value" : this.stringValue
		};
	}
	if (this.language) {
		return {
			"@language" : this.language,
			"@value" : this.stringValue
		};
	}
	
	return {
		"@value" : this.stringValue
	};
}

Literal.prototype.toString = function() {
	if (xsd.integer===this.type || xsd.double===this.type) {
		return this.stringValue;
	}
	if (this.language) {
		return '"' + this.stringValue + '"@' + this.language;
	}
	if (this.type) {
		return '"' + this.stringValue + '"^^' + this.type.stringValue;
	}
	return '"' + this.stringValue + '"';
}

Literal.prototype.sameValue = function(other) {
	return this.stringValue === other ||
		this.stringValue === other.stringValue;
}

Literal.prototype.equals = function(other) {
	
	if (typeof(other) === "string") {
		return this.stringValue === other;
	}
	
	var result = this.stringValue === other.stringValue;
	if (result && this.type) {
		result = other.type && this.type.equals(other.type);
	}
	if (result && this.language) {
		result = other.language && this.language === other.language;
	}
	return result;
}

Literal.prototype.key = function() {
	if (this.language) {
		return this.stringValue + '|' + this.language;
	} else if (this.type) {
		return this.stringValue + '|' + this.type.key();
	}
	return this.stringValue;
}

PropertySet = function() {
	this.statements = {};
}

PropertySet.prototype.replaceIRI = function(graph, oldId, newId, memory) {
	
	var oldValue = oldId.stringValue;

	var sink = {};
	for (var key in this.statements) {
		var s = this.statements[key];
		
		var subject = s.subject;
		var predicate = s.predicate;
		var object = s.object;
		
		if (subject.stringValue === oldValue) {
			subject = newId;
			if (object instanceof RdfResource && !memory[object.stringValue]) {
				var vertex = graph.vertex(object);
				vertex.replaceIRI(oldId, newId, memory);
			}
		}
		if (object.stringValue === oldValue) {
			object = newId;
			if (!memory[subject.stringValue]) {
				var vertex = graph.vertex(subject);
				vertex.replaceIRI(oldId, newId, memory);
			}
		}
		
		var statement = new Statement(subject, predicate, object);
		sink[statement.key()] = statement;
	}
	this.statements = sink;
}

PropertySet.prototype.add = function(statement) {
	var key = statement.key();
	this.statements[key] = statement;
}

PropertySet.prototype.remove = function(statement) {
	var key = statement.key();
	delete this.statements[key];
}

PropertySet.prototype.isEmpty = function() {
	for (var key in this.statements) {
		return false;
	}
	return true;
}

PropertySet.prototype.asList = function() {
	var list = [];
	for (var key in this.statements) {
		var s = this.statements[key];
		list.push(s);
	}
	return list;
}

PropertySet.prototype.selectObject = function(subject, list) {
	list = list || [];
	
	for (var key in this.statements) {
		var s = this.statements[key];
		
		if (!subject.equals(s.subject)) {
			continue;
		}
		list.push(s.object);
	}
	
	return list;
}

PropertySet.prototype.select = function(subject, object, list, limit) {
	if (!list) {
		list = [];
	}
	for (var key in this.statements) {
		var s = this.statements[key];
		
		if (subject && !s.subject.equals(subject)) {
			continue;
		}
		if (object && !s.object.equals(object)) {
			continue;
		}
		list.push(s);
		if (limit && list.length>=limit) {
			break;
		}
	}
	
	return list;
}

PropertySet.prototype.selectOne = function() {
	for (var key in this.statements) {
		return this.statements[key];
	}
	return null;
}
	

Vertex = function(id, graph) {
	this.id = id;
	this.graph = graph;
	this.statementMap = {};
}

Vertex.prototype.toList = function() {
	if (!this.elements) {
		this.elements = [];
	}
	return this.elements;
}

Vertex.prototype.instanceOf = function(type) {
	return this.v().instanceOf(type).first() ? true : false;
}

Vertex.prototype.push = function(vertex) {
	if (!this.elements) {
		this.elements = [];
	}
	this.elements.push(vertex);
}

Vertex.prototype.replaceIRI = function(oldId, newId, defaultMemory) {
	var memory = defaultMemory || {};
	if (oldId.stringValue == this.id.stringValue) {
		this.id = newId;
	}
	memory[this.id.stringValue] = this.id;
	
	for (var key in this.statementMap) {
		var propertySet = this.statementMap[key];
		propertySet.replaceIRI(this.graph, oldId, newId, memory);
	}
}

Vertex.prototype.equals = function(other) {
	return this.id.equals(other);
}

Vertex.prototype.toJson = function() {
	if (this.elements) {
		
		var out = [];
		for (var i=0; i<this.elements.length; i++) {
			var e = this.elements[i];
			if (e instanceof Vertex) {
				out.push(e.toJson());
			} else if (e.stringValue) {
				out.push(e.stringValue);
			}
		}
		
		return out;
		
	}
	var json = {id: this.id};
	
	var list = this.outStatements();
	for (var i=0; i<list.length; i++) {
		var s = list[i];
		var predicate = s.predicate;
		var object = s.object;
		if (object instanceof IRI) {
			object = object.stringValue;
		} else if (object instanceof BNode) {
			object = this.graph.vertex(object).toJson();
		} else if (object instanceof Literal) {
			object = object.stringValue;
		}
		var prior = json[predicate.localName];
		if (prior && Array.isArray(prior)) {
			prior.push(object);
		} else if (prior) {
			var array = [prior];
			array.push(object);
			json[predicate.localName] = array;
			
		} else {
			json[predicate.localName] = object;
		}
	}
	return json;
}

Vertex.prototype.v = function() {
	var source = new Traverser(null, null, this.graph);
	source.add(this);
	return new Traversal(source);
}

Vertex.prototype.select = function(subject, predicate, object, limit) {

	var propertyKey = 
		typeof(predicate)==="string" ? predicate :
		(predicate instanceof IRI) ? predicate.stringValue :
		(predicate instanceof Vertex) ? predicate.id.stringValue :
		null;
	
	var result = [];
	
	var set = this.statementMap[propertyKey];
	if (set) {
		set.select(subject, object, result, limit);
	}
	
	
	return result;

}

Vertex.prototype.has = function(property, value, traversal) {

	var valueString =
		typeof(value)==="string" ? value :
		(value instanceof RdfNode) ? value.stringValue :
		(value instanceof Vertex) ? value.id.stringValue :
		null;
	
	var propertyKey = 
		typeof(property)==="string" ? property :
		(property instanceof IRI) ? property.stringValue :
		(property instanceof Vertex) ? property.id.stringValue :
		null;
	
	var result = traversal || new Traversal();

	var set = this.statementMap[propertyKey];
	if (set) {
		for (var key in set.statements) {
			var s = set.statements[key];
			
			if (!this.id.equals(s.subject)) {
				continue;
			}
			if (valueString === s.object.stringValue) {
				result.push(this);
				break;
			}
		}
	}
	
	return result;
}

/**
 * Get the list of statements with the specified predicate and this Vertex as the object.
 * @param property The predicate used to filter the inward statements.  May be null.
 * @returns {Array} The list of statements with this Vertex as the object and the specified predicate.  If the predicate is null, then
 * all inward statements are returned.
 */
Vertex.prototype.inward = function(property) {

	var propertyKey = 
		typeof(property)==="string" ? property :
		(property instanceof IRI) ? property.stringValue :
		(property instanceof Vertex) ? property.id.stringValue :
		null;
	
	if (!propertyKey) {
		return this.inStatements();
		
	} else {
		var set = this.statementMap[propertyKey];
		if (set) {
			return set.select(null, this.id);
		}
	}
	
	return [];
}


Vertex.prototype.add = function(statement) {
	
	if (!statement.predicate) {
		console.log("invalid statement", statement);
	}
	var predicateKey = statement.predicate.key();
	var propertySet = this.statementMap[predicateKey];
	if (!propertySet) {
		propertySet = new PropertySet();
		this.statementMap[predicateKey] = propertySet;
	}
	propertySet.add(statement);
	

//	if (statement.subject.equals(this.id)) {
//
//		console.log("============= SUBJECT ===================");
//		console.log(this.id.toString());
//		console.log(statement.toString());
//		var list = this.outStatements();
//		for (var i=0; i<list.length; i++) {
//			console.log(list[i].toString());
//		}
//		console.log(" ");
//	} else {
//
//		console.log("============= OBJECT ===================");
//		console.log(this.id.toString());
//		console.log(statement.toString());
//	}
}

Vertex.prototype.remove = function(statement) {
	var key = statement.predicate.key();
	var propertySet = this.statementMap[key];
	if (propertySet) {
		propertySet.remove(statement);
		if (propertySet.isEmpty()) {
			delete this.statementMap[key];
		}
	}
}

Vertex.prototype.isEmpty = function() {
	for (var key in this.statementMap) {
		return false;
	}
	return true;
}

Vertex.prototype.shallowClone = function(id) {
	var graph = this.graph;
	
	var clone = graph.vertex(id);
	for (var key in this.statementMap) {
		var set = this.statementMap[key];
		var edgeList = [];
		set.select(this.id, null, edgeList);
		for (var i=0; i<edgeList.length; i++) {
			var edge = edgeList[i];
			var subject = edge.subject;
			graph.statement(clone, edge.predicate, edge.object);
		}
	}
	return clone;
}

Vertex.prototype.outStatements = function() {
	var list = [];
	for (var key in this.statementMap) {
		var set = this.statementMap[key];
		set.select(this.id, null, list);
	}
	return list;
}

Vertex.prototype.inStatements = function() {
	var list = [];
	for (var key in this.statementMap) {
		var set = this.statementMap[key];
		set.select(null, this.id, list);
	}
	return list;
}

Vertex.prototype.propertyValue = function(predicate) {
	
	if (predicate instanceof IRI) {
		predicate = predicate.stringValue;
	}
	
	var propertySet = this.statementMap[predicate];
	if (propertySet) {
		var s = propertySet.selectOne();
		if (s) {
			return s.object;
		}
	}
	return null;
}
/*****************************************************************************/	
function Path(item, prev) {
	this.item = item; 
	this.prev = prev; 
	this.next = [];
	if (prev && prev.next) {
		prev.next.push(this);
	}
}

/*****************************************************************************/
function Traverser(prev, step, graph) {
	this.prev = prev;
	this.step = step;
	this.graph = graph || (prev ? prev.graph : null);
	this.itemList = [];
	this.pathList = null;
	if (prev) {
		prev.next = this;
		if (prev.pathList) {
			this.pathList = [];
		}
	}
}

Traverser.prototype.clone = function() {
	var clone = new Traverser();
	clone.prev = this.prev;
	clone.step = this.step;
	clone.graph = this.graph;
	clone.itemList = this.itemList.slice(0);
	clone.pathList = (this.pathList) ? this.pathList.slice(0) : null;
	return clone;
}

Traverser.prototype.root = function() {
	var root = this;
	while (root.prev) {
		root = root.prev;
	}
	return root;
}

Traverser.prototype.addFilter = function(step) {
	if (!this.filterList) {
		this.filterList = [];
	}
	this.filterList.push(step);
}

Traverser.prototype.path = function(index) {
	return this.pathList ? this.pathList[index] : null;
}

Traverser.prototype.add = function(item, prevPath) {
	this.itemList.push(item);
	if (this.pathList) {
		var path = new Path(item, prevPath);
		this.pathList.push(path);
	}
}

/*****************************************************************************/
function AddVertexStep(resourceId) {
	this.resourceId = resourceId;
}

AddVertexStep.prototype.execute = function(traverser) {
	var next = new Traverser(traverser, this);
	var vertex = traverser.graph.vertex(this.resourceId);
	next.add(vertex);
	return next;
}
/*****************************************************************************/
function InwardStep() {
	this.predicateList = arguments;
}

InwardStep.prototype.execute = function(traverser) {
	var graph = traverser.graph;
	var next = new Traverser(traverser, this);
	var list = traverser.itemList;
	
	var plist = this.predicateList;
	
	for (var k=0; k<plist.length; k++) {
		var predicate = plist[k];
		for (var i=0; i<list.length; i++) {
			var item = list[i];
			var path = traverser.path(i);
			if (item instanceof Vertex) {
				var edges = item.inward(predicate);
				for (var j=0; j<edges.length; j++) {
					var e = edges[j];
					var subject = edges[j].subject;
					if (subject instanceof RdfResource) {
						subject = graph.vertex(subject);
					}
					next.add(subject, path);
				}
			}
			// TODO: Handle the case where the item is an Edge.
			
		}
	}
	
	
	return next;
}

/*****************************************************************************/
function InwardTransitiveClosureStep(predicate) {
	this.predicate = rdf.iri(predicate);
}

InwardTransitiveClosureStep.prototype.execute = function(traverser) {
	var i;
	var list = [];
	var key;
	var map = {};
	
	for (i=0; i<traverser.itemList.length; i++) {
		var item = traverser.itemList[i];
		var key = item.id.stringValue;
		if (!map[key]) {
			map[key] = item;
			list.push(item);
		}
	}
	
	var graph = traverser.graph;

	for (i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			
			var entityList = item.v().inward(this.predicate).toList();
			for (var j=0; j<entityList.length; j++) {
				var entity = entityList[j];
				key = entity.id.stringValue;
				if (!map[key]) {
					list.push(entity);
				}
			}
		}
	}
	traverser.itemList = list;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function TransitiveClosureStep(predicate) {
	this.predicate = rdf.iri(predicate);
}

TransitiveClosureStep.prototype.execute = function(traverser) {
	var i;
	var list = [];
	var key;
	var map = {};
	
	for (i=0; i<traverser.itemList.length; i++) {
		var item = traverser.itemList[i];
		var key = item.id.stringValue;
		if (!map[key]) {
			map[key] = item;
			list.push(item);
		}
	}
	
	var graph = traverser.graph;

	for (i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			
			var entityList = item.v().out(this.predicate).toList();
			for (var j=0; j<entityList.length; j++) {
				var entity = entityList[j];
				key = entity.id.stringValue;
				if (!map[key]) {
					list.push(entity);
				}
			}
		}
	}
	traverser.itemList = list;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function HasTransitiveStep(predicate, value) {
	this.predicate = rdf.iri(predicate);
	this.value = rdf.node(value);
}

HasTransitiveStep.prototype.execute = function(traverser) {
	var filtered = [];
	var list = traverser.itemList;
	
	var graph = traverser.graph;
	
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			
			var valueList = item.v().out(this.predicate).toList();
			var map = {};
			for (var j=0; j<valueList.length; j++) {
				var value = valueList[j];
				if (value instanceof Statement) {
					value = value.object;
				}
				if (this.value.equals(value)) {
					filtered.push(item);
					break;
				} else if (value instanceof Vertex) {
					// Check to see if we have traversed value yet.
					var key = value.id.stringValue;
					if (!map[key]) {
						// We have not yet traversed value, so traverse it now
						// and add any outgoing nodes to the valueList.
						map[key] = value;
						var next = value.v().out(this.predicate).toList();
						for (var k=0; k<next.length; k++) {
							valueList.push(next[k]);
						}
					}
				}
			}
		}
	}
	traverser.itemList = filtered;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function InstanceOfStep(owlClass) {
	this.owlClass = rdf.node(owlClass);
}

InstanceOfStep.prototype.execute = function(traverser) {
	var filtered = [];
	var list = traverser.itemList;
	var wantedIRI = this.owlClass.stringValue;
	var graph = traverser.graph;
	
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			var superMap = {};
			var typeList = item.v().out(rdf.type).toList();
			for (var j=0; j<typeList.length; j++) {
				var type = typeList[j];
				if (type.id.stringValue === wantedIRI) {
					filtered.push(item);
					break;
				} else {
					var superList = type.v().out(rdfs.subClassOf).toList();
					for (var k=0; k<superList.length; k++) {
						var superType = superList[k];
						var key = superType.id.stringValue;
						if (!superMap[key]) {
							superMap[key] = superType;
							typeList.push(superType);
						}
					}
				}
			}
		}
	}
	traverser.itemList = filtered;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function NodeKindStep(nodeKindList) {
	this.nodeKindList = [];
	for (var i=0; i<nodeKindList.length; i++) {
		this.nodeKindList[i] = rdf.iri(nodeKindList[i]);
	}
}

NodeKindStep.prototype.execute = function(traverser) {
	var iri = "http://www.w3.org/ns/shacl#IRI";
	var bnode = "http://www.w3.org/ns/shacl#BlankNode";
	var literal = "http://www.w3.org/ns/shacl#Literal";
	var kind = this.nodeKindList;
	var filtered = [];
	var list = traverser.itemList;
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			var id = item.id;
			for (var j=0; j<kind.length; j++) {
				var nodeKind = kind[j].stringValue;
				if ((id instanceof IRI) && nodeKind===iri) {
					filtered.push(item);
				} else if (id instanceof BNode && nodeKind===bnode) {
					filtered.push(item);
				}
			}
		}
	}
	traverser.itemList = filtered;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function HasStep(predicate, value) {
	this.predicate = predicate;
	this.value = value;
}
HasStep.prototype.execute = function(traverser) {
	var filtered = [];
	var list = traverser.itemList;
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			var select = item.select(item.id, this.predicate, this.value, 1);
			if (select.length>0) {
				filtered.push(item);
			}
		}
	}
	traverser.itemList = filtered;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function HasNotStep(predicate, value) {
	this.predicate = predicate;
	this.value = value;
}
HasNotStep.prototype.execute = function(traverser) {
	var filtered = [];
	var list = traverser.itemList;
	var pathList = traverser.pathList;
	var filteredPathList = (pathList) ? [] : null;
	
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			var select = item.select(item.id, this.predicate, this.value, 1);
			if (select.length==0) {
				filtered.push(item);
				if (pathList) {
					filteredPathList.push(pathList[i]);
				}
			}
		}
	}
	traverser.itemList = filtered;
	traverser.pathList = filteredPathList;
	traverser.addFilter(this);
	
	return traverser;
}
/*****************************************************************************/
function AddPropertyStep(predicate, object) {
	this.predicate = predicate;
	this.object = object;
}

AddPropertyStep.prototype.execute = function(traverser) {
	var graph = traverser.graph;
	var list = traverser.itemList;
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			graph.statement(item, this.predicate, this.object);
		}
	}
	
	return traverser;
}
/*****************************************************************************/
function AddRelationshipStep(predicate, object) {
	this.predicate = predicate;
	this.object = object;
}

AddRelationshipStep.prototype.execute = function(traverser) {
	var graph = traverser.graph;
	if (typeof(this.object) === "string") {
		this.object =  graph.vertex(this.object);
	}
	var list = traverser.itemList;
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			
			graph.statement(item, this.predicate, this.object);
		}
	}
	
	return traverser;
}

/*****************************************************************************/
function UniqueStep() {
	
}

UniqueStep.prototype.execute = function(traverser) {
	var filtered = [];
	var map = {};
	var list = traverser.itemList;
	for (var i=0; i<list.length; i++) {
		var item = list[i];
		if (item instanceof Vertex) {
			if (!map[item.id.stringValue]) {
				map[item.id.stringValue] = item;
				filtered.push(item);
			}
		}
	}
	
	traverser.itemList = filtered;
	traverser.addFilter(this);
	return traverser;
}
/*****************************************************************************/

function UnionStep(traversalList) {
	this.traversalList = traversalList;
}

UnionStep.prototype.execute = function(traverser) {
	var next = new Traverser(traverser, this);
	
	for (var i=0; i<this.traversalList.length; i++) {
		var traversal = this.traversalList[i];
		traversal.source = traverser.clone();
		
		var result = traversal.toList();
		for (var j=0; j<result.length; j++) {
			var item = result[j];
			var path = traverser.path(i);
			next.add(item, path);
		}
	}
	
	return next;
}

/*****************************************************************************/
function OutStep() {
	this.predicateList = arguments;
}

OutStep.prototype.execute = function(traverser) {
	
	// TODO: optimize for case where the next step is first().
	// In that case, we should return only the first node found, not the whole list
	
	var next = new Traverser(traverser, this);
	
	var predicateList = this.predicateList;
	
	for (var k=0; k<predicateList.length; k++) {
		var predicate = predicateList[k];
		
		var list = traverser.itemList;
		for (var i=0; i<list.length; i++) {
			var item = list[i];
			var path = traverser.path(i);

			var edges = item.select(item, predicate);
			for (var i=0; i<edges.length; i++) {
				var e = edges[i];
				var object = edges[i].object;
				if (object instanceof RdfResource) {
					object = traverser.graph.vertex(object);
				}
				next.add(object, path);
			}
			
		}
	}
	
	
	return next;
}

/*****************************************************************************/
function RepeatStep(traversal) {
	this.traversal = traversal;
}

RepeatStep.prototype.execute = function(traverser) {
	// TODO: handle the do-while case where this step is followed by an UntilStep.
	// For now, we assume it is a while-do loop, and the UntilStep has handled the loop.
	// In this case, there is nothing to do.  Just return the supplied traverser.
	
	return traverser;
}
/*****************************************************************************/
function UntilStep(condition) {
	this.condition = condition;
}

UntilStep.prototype.execute = function(traverser) {
	
	if (this.next && (this.next instanceof RepeatStep)) {
		// Implement while-do loop
		
		var source = traverser;
		
		var count = 0;
		while (!this.evaluateCondition(source) ) {
			count++;
			if (count > 100) {
				throw new Error("maximum number of iterations exceeded");
			}
			source = this.doRepeatStep(source);
		}
		
		return source;
		
	} else if (this.prev && this.prev instanceof RepeatStep) {
		// TODO: implement do-while loop
	}

	return traverser;
}

UntilStep.prototype.doRepeatStep = function(source) {
	
	var repeatTraversal = this.next.traversal;
	repeatTraversal.source = source;
	repeatTraversal.execute();
	
	var result = repeatTraversal.result;
	repeatTraversal.result = null;
	repeatTraversal.source = null;
	
	return result;
	
	
}

UntilStep.prototype.evaluateCondition = function(traverser) {
	var condition = this.condition;
	condition.result = null;
	condition.source = traverser.clone();
	condition.execute();
	var result = (condition.result && condition.result.itemList.length>0);
	condition.result = null;
	return result;
}

/*****************************************************************************/
function PathStep() {
	
}

PathStep.prototype.execute = function(traverser) {
	var root = traverser.root();
	var next = new Traverser(traverser, this);
	var list = traverser.itemList;
	for (var i=0; i<list.length; i++) {
		var path = root.path(i);
		this.buildPath(next, [], path);
	}

	return next;
}

PathStep.prototype.buildPath = function(sink, sequence, pathElement) {
	sequence.push(pathElement.item);
	var length = sequence.length;
	var next = pathElement.next;
	
	if (next.length==0) {
		sink.add(sequence.slice(0));
	}
	for (var i=0; i<next.length; i++) {
		this.buildPath(sink, sequence, next[i]);
	}
}

/*****************************************************************************/
function TraversalFactory() {
	
}

TraversalFactory.prototype.out = function() {
	return Traversal.prototype.out.apply(new Traversal(), arguments);
}

TraversalFactory.prototype.inward = function(predicate) {
	return new Traversal().inward(predicate);
}

/*****************************************************************************/

function Traversal(source) {
	this.source = source;
	this.lastStep = null;
	this.firstStep = null;
	this.result = null;
}


Traversal.prototype.first = function() {
	var list = this.execute();
	return list.length>0 ? list[0] : null;
}

Traversal.prototype.path = function() {
	if (this.source && !this.source.pathList) {
		var pathList = this.source.pathList = [];
		
		var list = this.source.itemList;
		for (var i=0; i<list.length; i++) {
			var item = list[i];
			pathList.push(new Path(item));
		}
	}
	
	return this.addStep(new PathStep());
}

Traversal.prototype.nodeKind = function() {
	return this.addStep(new NodeKindStep(arguments));
}

Traversal.prototype.until = function(condition) {
	return this.addStep(new UntilStep(condition));
}

Traversal.prototype.repeat = function(traversal) {
	return this.addStep(new RepeatStep(traversal));
}

Traversal.prototype.v = function(resourceId) {
	return this.addStep(new AddVertexStep(resourceId));
}

Traversal.prototype.out = function() {
	
	return this.addStep( OutStep.construct(arguments) );
}

Traversal.prototype.addProperty = function(predicate, object) {
	return this.addStep( new AddPropertyStep(predicate, object) );
}

Traversal.prototype.addRelationship = function(predicate, object) {
	return this.addStep( new AddRelationshipStep(predicate, object) );
}

Traversal.prototype.instanceOf = function(type) {
	return this.addStep(new InstanceOfStep(type));
}

Traversal.prototype.union = function() {
	return this.addStep( new UnionStep(arguments));
}

Traversal.prototype.hasNot = function(predicate, value) {
	return this.addStep( new HasNotStep(predicate, value) );
}

Traversal.prototype.addType = function(value) {
	return this.addRelationship(RDF.TYPE, value);
}

Traversal.prototype.hasType = function(value) {
	return this.addStep( new HasStep(RDF.TYPE, value));
}

Traversal.prototype.hasTransitive = function(predicate, value) {
	return this.addStep( new HasTransitiveStep(predicate, value) );
}

Traversal.prototype.inwardTransitiveClosure = function(predicate) {
	return this.addStep(new InwardTransitiveClosureStep(predicate));
}

Traversal.prototype.transitiveClosure = function(predicate) {
	return this.addStep(new TransitiveClosureStep(predicate));
}

Traversal.prototype.unique = function() {
	return this.addStep( new UniqueStep() );
}

Traversal.prototype.has = function(predicate, value) {
	return this.addStep( new HasStep(predicate, value) );
}

Traversal.prototype.inward = function() {
	return this.addStep(InwardStep.construct(arguments));
}

Traversal.prototype.addStep = function(step) {
	if (this.lastStep) {
		this.lastStep.next = step;
		step.prev = this.lastStep;
	}
	this.lastStep = step;
	if (!this.firstStep) {
		this.firstStep = step;
	}
	return this;
}

/**
 * TODO: Refactor so that execute performs the computation but returns this traversal.
 * Use the 'toList' function to perform the computation and return the list of items.
 */
Traversal.prototype.execute = function() {
	if (this.result) {
		return this.result.itemList;
	}
	var step = this.firstStep;
	var traverser = this.source;
	
	while (step) {
		traverser = step.execute(traverser);
		step = step.next;
	}
	this.result = traverser;
	return traverser.itemList;
}


Traversal.prototype.toList = Traversal.prototype.execute;

/*****************************************************************************/
ChangeSet = function(addition, removal) {
	this.addition = addition;
	this.removal = removal;
}

ChangeSet.prototype.addStatement = function(statement) {
	if (!this.addition) {
		this.addition = [];
	}
	this.addition.push(statement);
}

ChangeSet.prototype.undo = function(graph) {
	if (this.addition) {
		for (var i=0; i<this.addition.length; i++) {
			var statement = this.addition[i];
			graph.remove(statement);
		}
	}
	if (this.removal) {
		for (var i=0; i<this.removal.length; i++) {
			var statement = this.removal[i];
			graph.add(statement);
		}
	}
}

ChangeSet.prototype.redo = function(graph) {
	if (this.addition) {
		for (var i=0; i<this.addition.length; i++) {
			var statement = this.addition[i];
			graph.add(statement);
		}
	}
	if (this.removal) {
		for (var i=0; i<this.removal.length; i++) {
			var statement = this.removal[i];
			graph.remove(statement);
		}
	}
}

ChangeSet.prototype.removeStatement = function(statement) {
	if (!this.removal) {
		this.removal = [];
	}
	this.removal.push(statement);
}

ChangeSet.prototype.serialize = function() {
	var json = {};
	if (this.addition) {
		json.addition = this.serializeList(this.addition);
	}
	if (this.removal) {
		json.removal = this.serializeList(this.removal);
	}
	return json;
}

ChangeSet.prototype.serializeList = function(list) {
	var result = [];
	for (var i=0; i<list.length; i++) {
		result.push(list[i].serialize());
	}
	return result;
}

/************************************************************************/
/**
 * Create a ApplyChangeSet activity.
 * @class
 * @classdesc An activity that modifies the statements in a Graph.
 * @property {ChangeSet} changeSet The ChangeSet that encapsulates the statements
 * 		added to or removed from the taget Graph.
 * @property {Graph} targetGraph The target Graph that was modified by this action.
 */
function ApplyChangeSet(changeSet, targetGraph) {
	this.type = "ApplyChangeSet";
	
	/**
	 * The ChangeSet that encapsulates statements added to or removed from
	 * the target Graph.
	 * 
	 * @type ChangeSet
	 */
	this.object = changeSet;
	
	/**
	 * The Graph that was modified by this activity
	 * @type Graph 
	 */
	this.target = targetGraph;
}

ApplyChangeSet.prototype.undo = function() {
	this.object.undo(this.target);
}

ApplyChangeSet.prototype.redo = function() {
	this.object.redo(this.target);
}

ApplyChangeSet.prototype.serialize = function() {
	return {
		"@type" : this.type,
		object: this.object.serialize(),
		target: this.target.id.stringValue
	};
}


/*****************************************************************************/
Statement = function(subject, predicate, object) {
	this.subject = subject;
	this.predicate = predicate;
	this.object = object;
}

Statement.prototype.toString = function() {
	return this.subject.toString() + ' ' + this.predicate.toString() + ' ' + this.object.toString() + '.';
}

Statement.prototype.key = function() {
	try {

		var value = Sha1.hash(this.predicate.key() + '|' + this.object.key()).substring(0,32);
		var data = this.subject.key() + '#' + value;
		return data;
	} catch (e) {
		console.log(e);
	}
}

Statement.prototype.serialize = function() {
	return {
		subject: this.subject.serialize(),
		predicate: this.predicate.serialize(),
		object: this.object.serialize()
	};
}

/*****************************************************************************/

TermDefinition = function(id, type) {
	if (id) {
		this.id = id;
	}
	if (type) {
		this.type = type;
	}
	
}

/*****************************************************************************/
Context = function(object) {
	this.terms = {};
	if (object) {
		for (key in object) {
			var value = object[key];
			if (typeof(value)==="string") {
				this.addSimpleTerm(key, value);
			} else {
				this.terms[key] = value;
			}
		}
	}
}

Context.prototype.addSimpleTerm = function(key, id) {
	this.terms[key] = new TermDefinition(id);
}

Context.prototype.addTypedTerm = function(key, id, type) {
	this.terms[key] = new TermDefinition(id, type);
}

Context.prototype.expand = function(key) {
	var colon = key.indexOf(':');
	if (colon >= 0) {
		var prefix = key.substring(0, colon);
		var prefixTerm = this.terms[prefix];
		if (prefixTerm) {
			var localName = key.substring(colon+1);
			return prefixTerm.id + localName;
		}
	}
}
/*****************************************************************************/
Graph = function() {
	this.id = null;
	this.bnodeMap = null;
	this.vertexMap = {};
	this.edge = {};
	this.schema = null;
}

Graph.prototype.addHandler = function(listener) {
	if (!this.handlerList) {
		this.handlerList = [];
	}
	this.handlerList.add(listener);
}

Graph.prototype.removeHandler = function(handler) {
	if (this.handlerList) {
		var list = this.handlerList;
		for (var i=0; i<list.length; i++) {
			if (list[i] === handler) {
				list.splice(i, 1);
				return;
			}
		}
	}
}

Graph.prototype.vertex = function(id, readOnly) {
	if (!id) {
		id = this.resource();
	}
	
	if (id instanceof Vertex) {
		if (id.graph === this) {
			return id;
		}
		id = id.id;
	}
	var key = (typeof(id) === 'string') ? id : id.key();
	var vertex = this.vertexMap[key];
	if (!vertex && !readOnly) {
		var resource = (typeof(id)==='string') ? this.resource(id) : id;
		vertex = new Vertex(resource, this);
		this.vertexMap[key] = vertex;
		if (this.handlerList) {
			var list = this.handlerList;
			for (var i=0; i<list.length; i++) {
				list[i].handleVertex(vertex);
			}
		}
	}
	
	return vertex;
	
}

Graph.prototype.V = function() {

	var source = new Traverser(null, null, this);
	for (var i=0; i<arguments.length; i++) {
		var id = arguments[i];
		var vertex = this.vertex(id);
		source.add(vertex);
	}
	return new Traversal(source);
}

Graph.prototype.replaceIRI = function(oldIRI, newIRI) {
	var oldValue = konig.rdf.stringValue(oldIRI);
	var newValue = konig.rdf.stringValue(newIRI);
	
	
	var vertex = this.vertex(oldIRI, true);
	if (vertex) {
		delete this.vertexMap[oldValue];
		this.vertexMap[newValue] = vertex;

		var oldNode = konig.rdf.node(oldIRI);
		var newNode = new IRI(newValue);
			
		vertex.replaceIRI(oldNode, newNode);

		
	} else {
		this.vertex(newIRI);
	}
	
	
	
}

Graph.prototype.remove = function(statement) {
	var key = statement.key();
	delete this.edge[key];
	
	var subject = statement.subject;
	var subjectKey = subject.key();
	var vertex = this.vertexMap[subjectKey];
	if (vertex) {
		vertex.remove(statement);
		if (vertex.isEmpty()) {
			this.collectGarbage(subject);
			
		}
	}
	var object = statement.object;
	if (object instanceof RdfResource) {
		vertex = this.vertexMap[object.key()];
		if (vertex) {
			this.collectGarbage(object);
		}
	}
	
	if (this.handlerList) {
		var list = this.handlerList;
		for (var i=0; i<list.length; i++) {
			list[i].handleRemoveStatement(statement);
		}
	}
}

Graph.prototype.collectGarbage = function(subject) {
	for (var e in this.edge) {
		var s = this.edge[e];
		if (
			(subject.stringValue === s.subject.stringValue) ||
			(subject.stringValue === s.predicate.stringValue) ||
			(subject.stringValue === s.object.stringValue)
		) {
			return;
		}
	}
	delete this.vertexMap[subject.key()];
	if (subject instanceof BNode) {
		var other = this.bnodeMap[subject.stringValue];
		if (other) {
			delete this.bnodeMap[other.stringValue];
			delete this.bnodeMap[subject.stringValue];
		}
		
	}
}

function appendUnique(source, target) {
	outer:
	for (var i=0; i<source.length; i++) {
		var a = source[i];
		for (var j=0; j<target.length; j++) {
			var b = target[j];
			if (a.equals(b)) {
				continue outer;
			}
		}
		target.push(a);
	}
}

/**
 * Test whether a given subject is an instance of a specified type.
 * @param {RdfResource|Vertex|string} subject The subject whose type is being tested
 * @param {RdfResource|Vertex|string} type The specified type against which the subject is being tested.
 * @returns {Boolean} true if the given subject is an instance of the specified type, and false otherwise.
 */
Graph.prototype.instanceOf = function(subject, type) {
	
	if (type instanceof RdfResource) {
		type = type.stringValue;
	} else if (type instanceof Vertex) {
		type = type.id.stringValue;
	}
	
	var stack = this.select(subject, RDF.TYPE, null);
	for (var i=0; i<stack.length; i++) {
		var object = stack[i].object;
		if (object.stringValue === type) {
			return true;
		}
		appendUnique(this.select(object, rdfs.subClassOf, null), stack);
	}
	return false;
}

Graph.prototype.namedIndividualsList = function() {
	var list = [];
	for (var key in this.vertexMap) {
		var vertex = this.vertexMap[key];
		var rdfNode = vertex.id;
		if (rdfNode instanceof IRI) {
			list.push(vertex);
		}
	}
	return list;
}


Graph.prototype.load = function(doc, callback) {
	var self = this;
	
	jsonld.expand(doc, function(err, expanded) {
		if (err) {
			callback(err, this);
		} else {
			jsonld.flatten(expanded, function(err, flattened){
				if (err) {
					callback(err, this);
				} else {
					self.loadFlattened(flattened);
					callback(null, this);
				}
			});
		}
	});
}

Graph.prototype.getStatementById = function(id) {
	return this.edge[id];
}

Graph.prototype.add = function(statement) {
	return this.statement(statement.subject, statement.predicate, statement.object);
}

Graph.prototype.statement = function(subject, predicate, object) {

	if (subject instanceof Vertex) {
		subject = subject.id;
	}
	if (object instanceof Vertex) {
		object = object.id;
	}
	
	if (typeof(object) === 'number') {
		if (object !== (object|0) ) {
			object = this.typedLiteral(object.toString(), xsd.double);
		} else {
			object = this.typedLiteral(object.toString(), xsd.integer);
		}
	}
	
	if (typeof(subject) === 'string') {
		subject = this.resource(subject);
	}
	if (typeof(predicate)==='string') {
		predicate = this.predicate(predicate);
	}
	if (typeof(object)==='string') {
		object = this.object(object);
	}
	var s = new Statement(subject, predicate, object);
	this.edge[s.key()] = s;
	
	this.vertex(subject).add(s);
	if (object instanceof RdfResource) {
		this.vertex(object).add(s);
	}
	if (this.handlerList) {
		var list = this.handlerList;
		for (var i=0; i<list.length; i++) {
			list[i].handleStatement(s);
		}
	}
	
	return s;
}

Graph.prototype.loadJSON = function(json, context) {
	if (!context) {
		context = new konig.jsonld.Context(json['@context']);
	}
	var expanded = context.expand(json);
	var flat = context.flatten(expanded);
	this.loadFlattened(flat['@graph']);
}

Graph.prototype.loadFlattened = function(flattened) {
	this.bnodeMap = {};
	for (var i=0; i<flattened.length; i++) {
		var obj = flattened[i];
		var subject = this.resource(obj['@id']);
		for (var key in obj) {
			
			if (key ==='@id') {
				continue;
			}
			
			var predicate = null;
			var isType = false;
			if (key === '@type') {
				predicate = this.predicate(RDF.TYPE);
				isType = true;
				
			} else if (key === '@list') {
				
				var vertex = this.vertex(subject);
				var sink = vertex.elements = [];
				var list = obj[key];
				for (var j=0; j<list.length; j++) {
					var value = list[j];
					var object = this.flatValue(value);
					if (object instanceof RdfNode) {
						object = this.vertex(object);
					}
					sink.push(object);
				}
				continue;
				
				
			} else {
				predicate = this.predicate(key);
			}
			var list = obj[key];
			for (var k=0; k<list.length; k++) {
				var value = list[k];
				if (isType) {
					var object = this.iri(value);
					this.statement(subject, predicate, object);
					this.schema.statement(object, predicate, OWL.CLASS);
				} else {

					var object = this.flatValue(value);
					
					var s = this.statement(subject, predicate, object);
//					console.log("FLAT: " + s.toString());
				}
			}
		}
		
	}
}

Graph.prototype.flatValue = function(value) {
	var object = null;
	switch(typeof(value)) {
	case "string" :
		throw new Error("Invalid flattened structure");
		
	case "object" :
		var id = value["@id"];
		
		if (id) {
			if (id.startsWith("_:")) {
				object = this.resource(id);
			} else {
				object = this.iri(id);
			}
		} else {
			var objectType = value["@type"];
			var objectValue = value["@value"];
			var objectLanguage = value["@language"];
			object = 
				(objectLanguage) ? this.langString(objectValue, objectLanguage) :
				(objectType) ? this.typedLiteral(objectValue, objectType) :
				new Literal(objectValue);
				
		}
		
		
		break;
	}
	return object;
	
}

Graph.prototype.resource = function(stringValue) {
	
	stringValue = rdf.stringValue(stringValue);
	
	if (!stringValue || stringValue.startsWith('_:')) {
		if (!this.bnodeMap) {
			this.bnodeMap = {};
		}
		var bnode = stringValue ? this.bnodeMap[stringValue] : null;
		if (bnode) {
			return bnode;
		}
		var vertex = stringValue ? this.vertexMap[stringValue] : null;
		if (vertex) {
			return vertex.id;
		}
		
		var id = '_:' + uuid.v4();
		bnode = new BNode(id);
		if (stringValue) {
			this.bnodeMap[stringValue] = bnode;
		}
		this.vertex(bnode);
		return bnode;
		
	} else {
		
		var vertex = this.vertexMap[stringValue];
		if (vertex) {
			return vertex.id;
		}
		var iri = new IRI(stringValue);
		this.vertex(iri);
		return iri;
	}
}

Graph.prototype.iri = function(stringValue, readOnly) {
	
	stringValue = rdf.stringValue(stringValue);
	var vertex = this.vertexMap[stringValue];
	if (vertex) {
		return vertex.id;
	}
	
	if (readOnly) {
		return null;
	}
	
	iri = new IRI(stringValue);
	this.vertex(iri);
	return iri;
}

Graph.prototype.predicate = function(stringValue) {
	if (!this.schema) {
		this.schema = new Graph();
	}
	return this.schema.iri(stringValue);
}

Graph.prototype.object = function(value) {
	if (typeof(value) === 'object') {
		var id = value['@id'];
		if (id) {
			return this.resource(id);
		}
		var stringValue = value['@value'];
		var language = value['@language'];
		if (language) {
			return this.langString(stringValue, language);
		}
		var type = value['@type'];
		if (type) {
			return this.typedLiteral(stringValue, type);
		}
		return this.literal(stringValue);
	}
	
	return this.literal(value);
	
}

Graph.prototype.literal = function(stringValue) {
	var type = typeof(stringValue);
	
	if (type === 'string') {
		return new Literal(stringValue);
	} else if (type === 'number') {
		var integer = parseInt(stringValue);
		if (integer == stringValue) {
			return this.typedLiteral(stringValue, xsd.integer);
		}
		return this.typedLiteral(stringValue, xsd.double);
	}
	
	var obj = stringValue;
	var value = obj['@value'];
	var language = obj['@language'];
	if (language) {
		return langString(value, language);
	}
	var type = obj['@type'];
	if (type) {
		return typedLiteral(stringValue, type);
	}
	return literal(stringValue);
	
}

Graph.prototype.typedLiteral = function(stringValue, type) {
	if (typeof(type) === 'string') {
		type = new IRI(type);
	}
	var literal = new Literal(stringValue);
	literal.type = type;
	return literal;
}

Graph.prototype.langString = function(stringValue, language) {
	var literal = new Literal(stringValue);
	literal.language = language;
	return literal;
}

Graph.prototype.contains = function(subject, predicate, object) {
	var vertex = this.vertex(subject, true);
	if (!vertex) {
		return false;
	}
	
	var list = vertex.select(subject, predicate, object, 1);
	return list.length>0;
	
}

Graph.prototype.select = function(subject, predicate, object) {
	var array = [];
	
	
	for (var key in this.edge) {
		var e = this.edge[key];
		
		var ok = true;
		if (subject) {
			ok = e.subject.equals(subject);
		}
		if (ok && predicate) {
			ok = e.predicate.equals(predicate);
		}
		if (ok && object) {
			ok = e.object.sameValue(object);
		}
		if (ok) {
			array.push(e);
		}
	}
	
	return array;
}


RdfModule = function() {

	this.Context = Context;
	this.IRI = IRI;
	this.RDF = RDF;
	this.rdfs = rdfs;
	this.xsd = xsd;
	this.BNode = BNode;
	this.OWL = OWL;
	this.ChangeSet = ChangeSet;
	this.ApplyChangeSet = ApplyChangeSet;
	this.RdfResource = RdfResource;
	this.Vertex = Vertex;
	this.Literal = Literal;
	this.Traversal = Traversal;
	this.type = RDF.TYPE;
	this.Property = RDF.PROPERTY;
	this.Graph = Graph;
	this.__ = new TraversalFactory();

	this.type = new IRI('http://www.w3.org/1999/02/22-rdf-syntax-ns#type');
	this.Property = new IRI('http://www.w3.org/1999/02/22-rdf-syntax-ns#Property');
	
	this.rdfaContext = {
	    "cat": "http://www.w3.org/ns/dcat#",
	    "qb": "http://purl.org/linked-data/cube#",
	    "grddl": "http://www.w3.org/2003/g/data-view#",
	    "ma": "http://www.w3.org/ns/ma-ont#",
	    "owl": "http://www.w3.org/2002/07/owl#",
	    "rdf": "http://www.w3.org/1999/02/22-rdf-syntax-ns#",
	    "rdfa": "http://www.w3.org/ns/rdfa#",
	    "rdfs": "http://www.w3.org/2000/01/rdf-schema#",
	    "rif": "http://www.w3.org/2007/rif#",
	    "rr": "http://www.w3.org/ns/r2rml#",
	    "skos": "http://www.w3.org/2004/02/skos/core#",
	    "skosxl": "http://www.w3.org/2008/05/skos-xl#",
	    "wdr": "http://www.w3.org/2007/05/powder#",
	    "void": "http://rdfs.org/ns/void#",
	    "wdrs": "http://www.w3.org/2007/05/powder-s#",
	    "xhv": "http://www.w3.org/1999/xhtml/vocab#",
	    "xml": "http://www.w3.org/XML/1998/namespace",
	    "xsd": "http://www.w3.org/2001/XMLSchema#",
	    "prov": "http://www.w3.org/ns/prov#",
	    "sd": "http://www.w3.org/ns/sparql-service-description#",
	    "org": "http://www.w3.org/ns/org#",
	    "gldp": "http://www.w3.org/ns/people#",
	    "cnt": "http://www.w3.org/2008/content#",
	    "dcat": "http://www.w3.org/ns/dcat#",
	    "earl": "http://www.w3.org/ns/earl#",
	    "ht": "http://www.w3.org/2006/http#",
	    "ptr": "http://www.w3.org/2009/pointers#",
	    "cc": "http://creativecommons.org/ns#",
	    "ctag": "http://commontag.org/ns#",
	    "dc": "http://purl.org/dc/terms/",
	    "dc11": "http://purl.org/dc/elements/1.1/",
	    "dcterms": "http://purl.org/dc/terms/",
	    "foaf": "http://xmlns.com/foaf/0.1/",
	    "gr": "http://purl.org/goodrelations/v1#",
	    "ical": "http://www.w3.org/2002/12/cal/icaltzd#",
	    "og": "http://ogp.me/ns#",
	    "rev": "http://purl.org/stuff/rev#",
	    "sioc": "http://rdfs.org/sioc/ns#",
	    "v": "http://rdf.data-vocabulary.org/#",
	    "vcard": "http://www.w3.org/2006/vcard/ns#",
	    "schema": "http://schema.org/",
	    "describedby": "http://www.w3.org/2007/05/powder-s#describedby",
	    "license": "http://www.w3.org/1999/xhtml/vocab#license",
	    "role": "http://www.w3.org/1999/xhtml/vocab#role"
	};
}

RdfModule.prototype.iri = function(value) {
	if (value instanceof IRI) {
		return value;
	}
	if (value instanceof Vertex) {
		return value.id;
	}
	if (typeof(value) === "string") {
		return new IRI(value);
	}
	throw new Error("Illegal argument: " + value);
}

RdfModule.prototype.node = function(value) {
	if (value instanceof RdfNode) {
		return value;
	}
	if (value instanceof Vertex) {
		return value.id;
	}
	if (typeof("value") === "string") {
		return new Literal(value);
	}
	// TODO: consider handling JSON object.
	throw new Error("Illegal argument: " + value);
}

RdfModule.prototype.stringValue = function( node ) {
	if (!node) {
		return null;
	}
	if (node.stringValue) {
		return node.stringValue;
	}
	if (node instanceof Vertex) {
		return node.id.stringValue;
	}
	return node;
}

RdfModule.prototype.step = function() {
	return new Traversal();
}

RdfModule.prototype.rdfaType = function(element) {

	var type = element.getAttribute('typeof');
	var list = [];
	if (type) {
		type = type.split(/\s/);
		for (var i=0; i<type.length; i++) {
			list.push(this.rdfaExpand(element, type[i]));
		}
	}
	
	return list;
}

/**
 * Compute the fully-qualified IRI for the RDFa 'resource' attribute on a specified element.
 * @param {DOM.Element} element The element whose RDFa 'resource' attribute is being evaluated
 * @return {string} The fully-qualified IRI for the RDFa 'resource' attribute on the given
 * 		element, or null if the element has no 'resource' attribute.
 */
RdfModule.prototype.rdfaResource = function(element) {
	var resource = element.getAttribute('resource');
	
	return resource ? this.rdfaExpand(element, resource) : null;
}

/**
 * Expand an RDFa value for an IRI to the fully-qualified IRI.
 */
RdfModule.prototype.rdfaExpand = function(element, value) {
	if (
		value.startsWith("http://") ||
		value.startsWith("https://") ||
		value.startsWith("ftp://") ||
		value.startsWith("urn:")
	) {
		return value;
	}
	
	var colon = value.indexOf(':');
	if (colon >= 0) {
		var prefix = value.substring(0, colon);
		var namespace = this.rdfaNamespace(element, prefix);
		if (namespace) {
			var localName = value.substring(colon+1);
			return namespace + localName;
		}
		throw new Error("Namespace not found for prefix: " + prefix);
	}
	
	var vocab = this.rdfaVocab(element);
	if (vocab) {
		return vocab + value;
	}
	throw new Error("RDFa vocab attribute not found");
}

RdfModule.prototype.rdfaVocab = function(element) {
	while (element) {
		var vocab = element.getAttribute("vocab");
		if (vocab) {
			return vocab;
		}
		element = element.parentElement;
	}
	return null;
}

RdfModule.prototype.rdfaNamespace = function(element, prefix) {
	var p = prefix + ":";
	
	while (element) {
		var prefixAttr = element.getAttribute("prefix");
		if (prefixAttr) {
			var list = prefixAttr.split(/\s/);
			for (var i=0; i<list.length; i+=2) {
				var prefixValue = list[i];
				if (prefixValue === p) {
					return list[i+1];
				}
			}
		}
		element = element.parentElement;
	}
	return this.rdfaContext[prefix] || null;
}

RdfModule.prototype.graph = function() {
	return new Graph();
}

if (typeof(konig)==="undefined") {
	konig = {};
}
window.rdf = konig.rdf = new RdfModule();

	
});
