$(function(){
	


function MockProjectManager() {
	this.testData = {
		"id" : "http://www.konig.io/entity/qz3iE63bC7",
		"type" : "ldp:DirectContainer",
		"membershipResource" : "http://www.konig.io/entity/9ScJbdjojt",
		"ldp:hasMemberRelation" : "ks:hasProject",
		"contains" : [{
			"id" : "http://www.konig.io/entity/4LAVfUAztz",
			"name" : "The Digestive System"
		},{
			"id" : "http://www.konig.io/entity/FwXxCMs44e",
			"name" : "The Circulatory System"
		}]
	};
}	

MockProjectManager.prototype.createProject = function(project) {
	console.log("CREATE PROJECT", project);
}

MockProjectManager.prototype.getProjectListByUser = function(userId) {
	return this.testData;
}

if (typeof(konig)==="undefined") {
	konig = {};
}

konig.projectManager = new MockProjectManager();

konig.user = {
		id: "http://www.konig.io/entity/4LAVfUAztz"
}
	
});