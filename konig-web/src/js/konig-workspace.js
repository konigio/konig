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