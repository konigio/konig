$(function(){
	
function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, "\\$&");
    var regex = new RegExp("[?&]" + name + "(=([^&#]*)|&|#|$)", "i"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}
	
function AjaxOntologyService(defaultGraph) {
	this.defaultGraph = defaultGraph;
}

AjaxOntologyService.prototype.getOntologyGraph = function(callback) {
	var src = getParameterByName('src');
	if (!src && typeof(this.defaultGraph)==="string") {
		src = this.defaultGraph;
	}
	if (src) {
		$.get(src, null, function(data) {
			var object = (typeof(data) === "object") ? data : JSON.parse(data);
			callback(object);
		});
	} else if (this.defaultGraph) {
		callback(this.defaultGraph);
	}
}

if (typeof(konig)==="undefined") {
	konig = {}
}

konig.AjaxOntologyService = AjaxOntologyService;


});