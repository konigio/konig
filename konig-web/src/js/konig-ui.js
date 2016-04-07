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
$(window).ready(function() {

var RDF = rdf.RDF;
var RDFS = rdf.RDFS;
var IRI = rdf.IRI;
var RdfResource = rdf.RdfResource;
var Vertex = rdf.Vertex;
var skos = konig.skos;
var rdfs = konig.rdfs;
var Literal = rdf.Literal;

function toURI(iri) {
	return URI.serialize(URI.parse(iri));
}

function concatPath(baseIRI, relativePath) {
	if (baseIRI.endsWith('/')) {
		return baseIRI + relativePath;
	}
	return baseIRI + '/' + relativePath;
	
}
 

function GraphLoader(graph, actionHistory) {
	this.graph = graph;
	this.actionHistory = actionHistory;
}

GraphLoader.prototype.loadModel = function(graphIRI, callback) {
	// For now, we assume that graphIRI is a relative IRI.
	// Hack the full IRI
	
	
	var json = konigTestData[graphIRI];
	if (this.actionHistory) {
		this.actionHistory.append(new LoadGraphAction(graphIRI));
	}
	if (!this.graph.id) {
		this.graph.id = IRI.create(graphIRI);
	}
	this.graph.load(json, callback);
}

/***************************************************************************/
function LoadGraphAction(graphIRI) {
	this.type = "LoadGraph";
	this.loadedGraph = graphIRI;
}

LoadGraphAction.prototype.marshal = function(doc) {
	doc.actions.push({
		"@type" : this.type,
		loadedGraph: this.loadedGraph
	});
}
/***************************************************************************/
function InstanceOfFilter(graph, excludedTypeList) {
	this.graph = graph;
	this.excludedTypeList = excludedTypeList;
}

InstanceOfFilter.prototype.reject = function(rdfVertex) {
	var subject = rdfVertex.id;
	var list = this.excludedTypeList;
	for (var i=0; i<list.length; i++) {
		var type = list[i];
		if (this.graph.instanceOf(subject, type)) {
			return true;
		}
	}
	return false;
}
/***************************************************************************/

function KonigIndex(konigUI) {
	this.suppressIndexEvents = false;
	this.konigUI = konigUI;
	this.vertexFilter = new InstanceOfFilter(konigUI.graph, [RDF.PROPERTY]);

	var self = this;

	$.jstree.defaults.checkbox.whole_node = false;
	$.jstree.defaults.checkbox.tie_selection = false;
	$.jstree.defaults.checkbox.three_state = false;
	$.jstree.defaults.core.themes.dots = false;
	this.conceptTree = $('#konig-concept-list').jstree({
		core : {
			check_callback: true
		},
		plugins: ["checkbox"]
	
	}).bind('check_node.jstree', function(event, data){
		self.onCheckIndexItem(event, data);
	}).bind('uncheck_node.jstree', function(event, data){
		self.onUncheckIndexItem(event, data);
	});

	var presenter = konigUI.rdfController.presenter;
	
	presenter.bind("SelectPresenterElement", this.clearSelection, this);
	presenter.bind("RdfVertexBind", this.onRdfVertexBind, this);
	presenter.bind("RdfVertexUnbind", this.onRdfVertexUnbind, this);
	presenter.bind("RdfStatementBind", this.onRdfStatementBind, this);
	presenter.bind("RdfStatementUnbind", this.onRdfStatementUnbind, this);
	presenter.bind("RdfVertexDelete", this.onRdfVertexDelete, this);
	presenter.bind("AddVertex", this.onAddVertex, this);
}

KonigIndex.prototype.onRdfVertexDelete = function(event) {
	var doomedId = event.resourceId;
	var treeNodeId = this.indexSubject(doomedId);
	this.conceptTree.jstree().delete_node(treeNodeId);
}

KonigIndex.prototype.onRdfStatementUnbind = function(event) {
	var statement = event.statement;
	this.suppressIndexEvents = true;
	var treeNodeId = this.indexObjectId(statement);
	this.conceptTree.jstree().uncheck_node('#' + treeNodeId);
	this.suppressIndexEvents = false;
}

KonigIndex.prototype.onRdfStatementBind = function(event) {
	var statement = event.statement;
	this.suppressIndexEvents = true;
	var treeNodeId = this.indexObjectId(statement);
	
	var tree = this.conceptTree.jstree();
	var treeNode = tree.get_node(treeNodeId);
	
	
	if (!treeNode) {

		var graph = this.konigUI.graph;
		var subject = statement.subject;
		var subjectIndexId = this.indexSubject(statement.subject);
		var subjectNode = document.getElementById(subjectIndexId);
		if (!subjectNode) {
			var vertex = graph.getVertex(subjectIndexId);
			this.addVertex(vertex);
			tree.check_node('#' + subjectIndexId);
		}
		
		var predicate = graph.vertex(statement.predicate);
		var label = predicate.propertyValue(rdfs.label);
		if (!label) {
			label = predicate.id.localName;
		} else {
			label = label.stringValue;
		}
		
		
		var nodeData = {
			id: treeNodeId,
			text: label
		};
		
		var subjectData = tree.get_node(subjectIndexId);
		var index = this.insertionIndex(tree, subjectData, nodeData.text);
		tree.create_node(subjectIndexId, nodeData, index);
	}
	
	this.conceptTree.jstree().check_node('#' + treeNodeId);
	this.suppressIndexEvents = false;
}

KonigIndex.prototype.insertionIndex = function(tree, parentData, label) {
	var list = parentData.children;
	for (var i=0; i<list.length; i++) {
		var childData = tree.get_node(list[i]);
		if (childData.text.localeCompare(label) >= 0) {
			return i;
		}
	}
	return list.length;
}

KonigIndex.prototype.onAddVertex = function(event) {
	var vertex = event.vertex;
	this.suppressIndexEvents = true;
	
	this.addVertex(vertex);
	
	this.suppressIndexEvents = false;
}

KonigIndex.prototype.onRdfVertexBind = function(event) {
	
	if (this.vertexFilter.reject(event.vertexData.rdfVertex)) {
		return;
	}
	
	this.suppressIndexEvents = true;
	var treeNodeId = this.indexSubject(event.vertexData.rdfVertex.id);
	var exists = document.getElementById(treeNodeId);
	if (!exists) {
		this.addVertex(event.vertexData.rdfVertex);
	}

	this.conceptTree.jstree().check_node('#' + treeNodeId);
	this.suppressIndexEvents = false;
}


KonigIndex.prototype.onRdfVertexUnbind = function(event) {
	this.suppressIndexEvents = true;
	var treeNodeId = this.indexSubject(event.vertexData.rdfVertex.id);
	this.conceptTree.jstree().uncheck_node('#' + treeNodeId);
	// TODO: uncheck all children
	this.suppressIndexEvents = false;
}


KonigIndex.prototype.updateIndex = function() {
	var treeNodes = [];

	this.counter = 0;
	var list = this.konigUI.graph.namedIndividualsList();
	for (var i=0; i<list.length; i++) {
		var vertex = list[i];
		if (this.vertexFilter.reject(vertex)) {
			continue;
		}
		
		var node = {
			id: this.indexSubject(vertex.id),
			text: this.nodeText(vertex)
		};
		var propertyList = this.indexPropertyList(vertex);
		if (propertyList.length>0) {
			node.children = propertyList;
		}
		treeNodes.push(node);
		
	}
	
	treeNodes = treeNodes.sort(function(a, b){
		return a.text.localeCompare(b.text);
	});
	
	for (var i=0; i<treeNodes.length; i++) {
		var node = treeNodes[i];
		if (!document.getElementById(node.id)) {
			this.conceptTree.jstree().create_node('#', node);
		}
	}
}

/**
 * @param {jQuery} tree The jQuery object representing the jstree instance.
 * @param {string} [text] The text that will be displayed for the node.
 * @param {string} [id] The id attribute for the node
 * @classdesc A node within a tree widget
 */
TreeNode = function(tree, text, id) {
	this.tree = tree;
	this.id = id;
	this.text = text;
	this.self = null;
	this.parent = null;
	this.children = null;
}

/**
 * Create a new TreeNode that belongs to the same jstree instance as this one.
 * @param {string} [text] The text that will be displayed for the node
 * @param {string} [id] The id attribute for the node.
 */
TreeNode.prototype.create = function(text, id) {
	return new TreeNode(this.tree, text, id);
}

TreeNode.prototype.getText = function() {
	this.bind();
	this.text = this.tree.jstree().get_text('#' + this.id);
	return this.text;
}

TreeNode.prototype.bind = function() {
	if (!this.self) {
		this.self = $('#' + this.id);
	}
	return this.self.length>0;
}

TreeNode.prototype.getChildren = function() {
	if (!this.children) {
		this.children = [];
		
		var source = this.self || this.tree;
		
		var list = source.find('ul').find('li');
		for (var i=0; i<list.length; i++) {
			var child = new TreeNode(this.tree);
			child.self = $(list[i]);
			child.parent = this;
			this.children.push(child);
		}
	}
	return this.children;
}

TreeNode.prototype.getId = function() {
	if (!this.id) {
		this.id = this.self.attr('id');
	}
	return this.id;
}

TreeNode.prototype.setText = function(value) {
	this.text = value;
	if (this.self) {
		this.tree.jstree().set_text(this.self, value);
	}
}

TreeNode.prototype.insert = function(index, child) {

	var node = {
		id: child.id,
		text: child.text
	};
	
	var kids = this.getChildren();
	kids.splice(index, 0, child);
	
	child.id = this.tree.jstree().create_node(this.self, node, index);
	child.self = $('#' + child.id);
	child.parent = this;
	
	
}

TreeNode.prototype.append = function(child) {
	
	var kids = this.getChildren();
	this.insert(kids.length, child);
	
}

KonigIndex.prototype.element = function(id) {
	return document.getElementById(id);
}

KonigIndex.prototype.nodeText = function(node) {
	
	if (node instanceof Literal) {
		// TODO: truncate the string value if it is too long.
		return node.stringValue;
	}
	
	var vertex = 
		(node instanceof Vertex) ? node :
		this.konigUI.graph.vertex(node);
	
	var label = 
		vertex.v().out(skos.prefLabel).first() ||
		vertex.v().out(skos.altLabel).first() ||
		vertex.v().out(rdfs.label).first() ||
		null;
	
	return label ? label.stringValue : vertex.id.localName;
	
	
}

KonigIndex.prototype.addStatement = function(statement, parentId) {
	

	var objectId = this.indexObjectId(statement);
	if (this.element(objectId)) {
		// The statement already exists in the index.
		return objectId;
	}
	
	if (!parentId) {
		parentId = this.indexSubject(statement.subject);
	}
	
	if (!this.element(parentId)) {
		parentId = this.addVertex(statement.subject);
	}
	
	var label = this.nodeText(statement.predicate);
	var tree = this.conceptTree.jstree();
	var parentNode = tree.get_node(parentId);
	var predicateId = this.indexPredicate(statement);
	var kids = parentNode.children;
	var childNode = null;
	var index = "last";
	
	if (this.element(predicateId)) {
		parentNode = tree.get_node(predicateId);
		parentId = predicateId;
	}
	
	// find the insertion point.
	for (var i=0; i<kids.length; i++) {
		var childId = kids[i];
		
		childNode = tree.get_node(childId);
		if (
			parentId !== predicateId &&
			childNode.data && 
			childNode.data.predicate &&
			childNode.data.predicate === statement.predicate.stringValue
		) {
			// This childNode has the same predicate as the one we are adding.
			// Need to introduce an intermediary.
			
			var json = {
				id: predicateId,
				text: label
			};
			tree.create_node(parentId, i, json);
			tree.move_node(childNode.id, predicateId);
			
			index = label.localeCompare(childNode.text) <= 0 ? 0 : 1;
			label = this.nodeText(statement.object);
			
			
		}
		if (childNode.text.localeCompare(label)<=0) {
			index = i;
			break;
		}
	}
	var json = {
		id: objectId,
		text: label,
		data: {predicate: statement.predicate.stringValue}
	};
	tree.create_node(parentId, json, index);
	if (statement.object instanceof IRI) {
		this.addVertex(statement.object);
	}
	
	return objectId;
	
}



KonigIndex.prototype.addVertex = function(rdfVertex) {

	if (!(rdfVertex instanceof Vertex)) {
		rdfVertex = this.konigUI.graph.vertex(rdfVertex);
	}
	
	this.suppressIndexEvents = true;
	var controller = this.konigUI.rdfController;

	var newNodeId = this.indexSubject(rdfVertex.id);
	var prior = document.getElementById(newNodeId);
	if (prior) {
		return newNodeId;
	}

	var newNode = {
		id: newNodeId,
		text: this.nodeText(rdfVertex)
	};
	
	var list = this.conceptTree.find('ul').find('li');
	var tree = this.conceptTree.jstree();
	
	var treeNodeId = null;
	var label = newNode.text;
	for (var i=0; i<list.length; i++) {
		var id = $(list[i]).attr('id');
		var node = tree.get_node('#' + id);
		
		if (label.localeCompare(node.text) <= 0) {
			treeNodeId = tree.create_node('#', newNode, i);
			break;
		}
		
	}
	if (!treeNodeId) {
		treeNodeId = tree.create_node('#', newNode, 'last');
	}
	

	var list = rdfVertex.outStatements();
	for (var i=0; i<list.length; i++) {
		this.addStatement(list[i], newNodeId);
	}
	
	
	this.suppressIndexEvents = false;
	
	return newNodeId;
}



KonigIndex.prototype.indexPropertyList = function(vertex) {
	var map = {};
	var outgoing = vertex.outStatements();
	for (var i=0; i<outgoing.length; i++) {
		var statement = outgoing[i];

		if (statement.predicate.stringValue === rdfs.label.stringValue) {
			continue;
		}
		
		var key = statement.predicate.stringValue;
		var node = map[key];
		if (node) {
			var kids = node.children;
			if (!kids) {
				kids = node.children = [];
				var s = node.statement;
				kids.push({
					id: this.indexObjectId(s),
					text: this.nodeText(s.object)
				})
				
			}
			var child = {
				id: this.indexObjectId(statement),
				text: this.nodeText(statement.object)
			};
			kids.push(child);
			
		} else {
			node = {
				text: this.nodeText(statement.predicate),
				statement: statement
			};
			map[key] = node;
		}
		
	}

	var list = [];
	for (var key in map) {
		var node = map[key];
		if (node.children) {
			node.id = this.indexPredicate(node.statement);
		} else {
			node.id = this.indexObjectId(node.statement);
		}
		delete node.statement;
		list.push(node);
	}
	return list;
}

KonigIndex.prototype.clearSelection = function() {
	this.conceptTree.jstree().deselect_all();
}

KonigIndex.prototype.onCheckIndexItem = function(event, data) {
	
	if (!this.suppressIndexEvents) {

		var controller = this.konigUI.rdfController;
		var actionHistory = controller.presenter.actionHistory;
		var txn = actionHistory.beginTransaction();
		
		var nodeId = data.node.id;
		if (nodeId.startsWith('index-subject-')) {
			
			var subjectId = nodeId.substring('index-subject-'.length);
			var subject = controller.showResource(subjectId);
			if (subject) {
				controller.presenter.centerViewBoxAt(subject.vertexView);
			}
		} else if (nodeId.startsWith('index-object-')) {

			this.supressIndexEvents = true;
			var key = nodeId.substring('index-object-'.length);
			var statement = controller.graph.getStatementById(key);
			if (statement) {
				controller.showStatement(statement);
			}
			this.suppressIndexEvents = false;
		}
		
		actionHistory.commit(txn);
	
	}
}

KonigIndex.prototype.onUncheckIndexItem = function(event, data) {
	
	if (!this.suppressIndexEvents) {

		var controller = this.konigUI.rdfController;
		var nodeId = data.node.id;
		if (nodeId.startsWith('index-subject-')) {
			
			var subjectId = nodeId.substring('index-subject-'.length);
			
			var vertexView = controller.vertexViewById(subjectId);
			if (vertexView) {
				controller.presenter.deleteElement(vertexView);
			}
			
		} else if (nodeId.startsWith('index-object-')) {
			var key = nodeId.substring('index-object-'.length);
			var arcView = controller.arcViewById(key);
			if (arcView) {
				controller.presenter.deleteElement(arcView);
			}
		}
	
	}
}

KonigIndex.prototype.indexObjectId = function(statement) {
	return 'index-object-' + statement.key();
}



KonigIndex.prototype.indexSubject = function(iriNode) {
	var value = (typeof(iriNode)==="string") ? iriNode : iriNode.stringValue;
	return 'index-subject-' + value;
}


KonigIndex.prototype.indexPredicate = function(statement) {
	var text = statement.subject.stringValue + '|' + statement.predicate.stringValue;
	var hash = Sha1.hash(text);
	return 'index-predicate-' + hash;
}


/****************************************************************************/
function MenuSystem() {
	this.openMenu = null;
	var menuSystem = this;
	
	$('body').bind('click', function() {
		if (menuSystem.openMenu) {
			menuSystem.openMenu.hide();
		}
	});
	
	
	$('.konig-menu-container').each(function() {
		var menu = $(this);
		menu.remove();
		$('body').append(menu);
		menu.hide();
	});
	
	$('.konig-menu').each(function(){
		
		$(this).click(function(event){
			var self = $(this);
			var box = this.getBoundingClientRect();
			
			var containerId = self.data('container');
			var menu =  menuSystem.openMenu = $('#' + containerId);
			menu.show().menu();
			menu.css('left', box.left + "px").css('top', box.bottom + "px");
			event.stopPropagation();
		});
	});
}

/****************************************************************************/
	
function KonigUI() {
	var self = this;
	this.mapService = konig.mapService;
	this.rdfController = konig.rdfController;
	this.layout = $('body').layout(
		{
			applyDefaultStyles:true,
			north: {
				resizable: true,
				closable: false
			}
		}
	);
	
	this.graph = this.rdfController.graph;
	this.graphLoader = new GraphLoader(this.graph, this.rdfController.presenter.actionHistory);
	this.index = new KonigIndex(this);
	
	$(window).resize(function() {
		self.onResizeWindow();
	});
	
	this.onResizeWindow();

	this.menuSystem = new MenuSystem();
	
	this.mapService.start(function(mapURL, err, svgContent) {
		self.rdfController.presenter.renderSVG(mapURL, svgContent);
	});
	
}	


KonigUI.prototype.TreeNode = TreeNode;

KonigUI.prototype.onResizeWindow = function() {
	
	if (this.resizeTimer) {
		clearTimeout(this.resizeTimer);
	}
	
	this.doResizeWindow();
	
	var self = this;
	this.resizeTimer = setTimeout(function(){
		delete self.resizeTimer;
		self.doResizeWindow();
	}, 200);

}

KonigUI.prototype.doResizeWindow = function() {

	var center = $('.ui-layout-center');
	var width = center.width() - 6;
	var height = center.height()-6;
	var paper = konig.presenter.paper.node;
	paper.setAttribute('width', width);
	paper.setAttribute('height', height);
}

	
if (typeof(konig) === 'undefined') {

	konig = {};
}

konig.ui = new KonigUI();

ui = konig.ui;



	
});
