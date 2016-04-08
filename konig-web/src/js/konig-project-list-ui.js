$(function(){

function ProjectListPresenter(user, projectManager) {
	this.user = user || konig.user;
	this.projectManager = projectManager || konig.projectManager;
	this.listTemplate = $('#project-list-template').html();
	this.container = this.projectManager.getProjectListByUser(this.user.id);
	Mustache.parse(this.listTemplate);
}

ProjectListPresenter.prototype.render = function() {
	
	
	var self = this;
	
	$('#new-project-button').button().click(function(event){
		self.newProject();
	});
	
	$('#create-project-button').click(function(event){
		self.createProject();
	});
	this.renderList();
}

ProjectListPresenter.prototype.renderList = function() {

	var rendered = Mustache.render(this.listTemplate, this.container);
	$('#konig-project-list').html(rendered);
}

ProjectListPresenter.prototype.createProject = function() {
	var projectName = $('#project-name-input').val().trim();
	
	if (projectName.length == 0) {
		$('#create-project-message').text("You must enter a project name");
		return;
	}
	
	var project = {
		"id" : "http://www.konig.io/entity/" + uuid.v1(),
		"name" : projectName,
		"owner" : this.user
	};
	
	this.projectManager.createProject(project);
	
	$('#new-project-dialog').dialog('close');
	
	this.container.contains.push(project);
	this.renderList();
	
	
}

ProjectListPresenter.prototype.newProject = function() {
	
	$("#project-name-input").val("");
	$('#create-project-message').text("");
	
	$("#new-project-dialog").dialog({
		width: 500,
		title: "New Project"
	});
}

konig.ProjectListPresenter = ProjectListPresenter;
	
});