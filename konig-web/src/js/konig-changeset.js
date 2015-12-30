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

var rdf = konig.rdf;	
var BNode = rdf.BNode;	
var owl = konig.owl;
var OwlReasoner = konig.OwlReasoner;
var Graph = rdf.Graph;

/*****************************************************************************/

function ChangeSet(data, schema) {
	this.data = data;
	this.schema = schema || data;
	this.reasoner = new OwlReasoner(data, this.schema);
	this.additions = rdf.graph();
	this.removals = rdf.graph();
	this.priorState = rdf.graph();
	this.newBNode = {};
}

ChangeSet.prototype.add= function(statement) {
	this.additions.add(statement);
}

ChangeSet.prototype.remove = function(statement) {
	this.removals.add(statement);
}

ChangeSet.prototype.recordPriorState = function(statement) {
	this.recordNode(statement.subject);
	this.recordNode(statement.object);
}

ChangeSet.prototype.recordNode = function(node) {
	
	node = rdf.node(node);
	if (node instanceof BNode) {
		var id = BNode.id;
		
		var newNode = this.newNode[id.stringValue];
		if (newNode) {
			var prior = this.priorState.vertex(node, true);
			if (!prior) {
				this.recordBNode(node);
			}
		}
	}
}

ChangeSet.prototype.recordBNode = function(node) {
	
	var g = this.data;
	var schema = this.schema;
	
	
	var vertex = g.vertex(node);
	
	
	if (!this.recordTypedBNode(vertex)) {
		// TODO: record BNode full state.
	}
	
}


ChangeSet.prototype.recordTypedBNode = function(node) {

	var typeList = node.v().out(rdf.type).toList();
	
	for (var i=0; i<typeList.length; i++) {
		var keyList = typeList[i].v().out(owl.hasKey).first();
		if (keyList && keyList.elements && keyList.elements.length>0) {
			keyList = keyList.elements;
			var sink = [];
			var count = 0;
			for (var j=0; j<keyList.length; j++) {
				var predicate = keyList[j];
				var out = this.reasoner.select(node, predicate, null);
				sink.push(out);
				if (out.length > 0) {
					count++;
				}
			}
			if (count == keyList.length) {
				for (var j=0; j<keyList.length; j++) {
					var list = sink[j];
					for (var k=0; k<list.length; k++) {
						var s = list[k];
						this.priorState.add(s);
					}
				}
			}
		}
		
	}
}

ChangeSet.prototype.handleVertex = function(vertex) {
	var id = vertex.id;
	if (id instanceof BNode) {
		this.newBNode[id.stringValue] = vertex;
	}
}

ChangeSet.prototype.handleStatement = function(statement) {
	this.recordNode(statement.subject);
	this.recordNode(statement.object);
	this.additions.add(statement);
}

ChangeSet.prototype.handleRemoveStatement = function(statement) {
	this.removals.add(statement);
}

/*****************************************************************************/
Graph.prototype.beginTransaction = function() {
	if (!this.changeSet) {
		this.changeSet = new ChangeSet(this, this);
		this.addHandler(this.changeSet);
		return this.changeSet;
	}
	return null;
}

Graph.prototype.endTransaction = function(txn) {
	if (txn && txn === this.changeSet) {
		this.removeHandler(this.changeSet);
		delete this.changeSet;
	}
	return txn;
}
/*****************************************************************************/
konig.ChangeSet = ChangeSet;
	
});
