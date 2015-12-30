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
	
var ks = konig.ks;
var ke = konig.ke;
var as = konig.as;
var prov = konig.prov;
var Traversal = rdf.Traversal;

/************************************************************************/
/**
 * @class
 * @classdesc An event which fires when a new Vertex is added to the Workspace
 */
function AddVertex(vertex) {
	this.type = "AddVertex";
	this.vertex = vertex;
}
/************************************************************************/
function Workspace(id, graph, graphService) {
	this.id = id;
	this.graph = graph || konig.rdfController.graph;
	this.graphService = graphService || konig.graphService;
}

Workspace.prototype.loadEntity = function(entityId) {
	
	if (!this.entityWasLoaded(entityId)) {
		var self = this;
		this.graphService.getGraph(entityId, function(response){
			var json = response.graphContent;
			if (json) {
				self.graph.loadJSON(json, konig.defaultContext);
				var vertex = self.graph.vertex(entityId);
				konig.eventManager.notifyHandlers(new AddVertex(vertex));
			}
		});
	}
	
}

/**
 * Check whether the specified entity was loaded into this workspace.
 * @param {konig.rdf.IRI | string} entityId The identifier for the entity.
 * @returns {boolean} True if the entity was loaded into this workspace, and false otherwise.
 */
Workspace.prototype.entityWasLoaded = function(entityId) {

	return this.graph.V(entityId)
		.out(prov.wasUsedBy)
		.hasType(ks.LoadGraph)
		.has(as.object, this.id)
		.exists();
}

/************************************************************************/
function WorkspaceManager() {
	this.activeWorkspace = null;
}

WorkspaceManager.prototype.getActiveWorkspace = function() {
	if (!this.activeWorkspace) {
		var id = new IRI(ke.NAMESPACE + uuid.v1());
		this.activeWorkspace = new Workspace(id);
	}
	
	return this.activeWorkspace;
}


/************************************************************************/

konig.Workspace = Workspace;	
konig.WorkspaceManager = WorkspaceManager;
konig.workspaceManager = new WorkspaceManager();
	
});
