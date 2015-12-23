$(window).ready(function() {

if (!konig) {
	konig = {};
}
	
/******************************************************************************/
	
/**
 * A mock for the MapService.  Used for testing.
 */	
function MockMapService() {
}	

/**
 * Start this MapService.  Interpret the browser window's address, fetch
 * the requested concept map, and return the outer SVG for the concept map to
 * the given callback function.
 * @param {MapService~svgCallback} callback - The callback that receives the SVG for the concept map.
 */
MockMapService.prototype.start = function(callback) {
	// In this mock, we hard-code the requested SVG.
	// In a real MapService, we would deduce it from the browser's address.
	
	if (konigTestData.defaultMap) {
		this.getMap(konigTestData.defaultMap, callback);
	}
}

MockMapService.prototype.getMap = function(mapURL, callback) {

	var map = konigTestData[mapURL];
	var error = map ? null : "File Not Found";
	
	callback(mapURL, error, map);

}

MockMapService.prototype.putMap = function(mapURL, outerSVG) {
	konigTestData[mapURL] = outerSVG;
}

/******************************************************************************/
/**
 * @class
 */
function MockGraphService(source) {
	this.source = source || konigTestData;
}

/**
 * Get a named graph.
 * @param {string} graphIRI - The IRI for the graph that is being requested.
 * @param {MockGraphService~graphCallback} callback - The callback that handles the response.
 */
MockGraphService.prototype.getGraph = function(graphIRI, callback) {
	var key = 
		(graphIRI.stringValue) ? graphIRI.stringValue :
		(graphIRI.id) ? graphIRI.id.stringValue :
		graphIRI;
	
	var response = {
		graphIRI: graphIRI,
		graphContent: this.source[key]
	};
	
	callback(response);
}

/**
 * A callback that handles the response from a getGraph request
 * @param {Object} response The response from the getGraph method.  This object contains
 * 		two fields: graphIRI is the IRI of the graph that was requested, and graphContent
 * 		is the JSON representation of the graph content.
 */


/******************************************************************************/
function MockAutocompleteService(testData) {
	var data = testData || konigTestData;
	this.suggest = data ? data.suggest : null;
}


MockAutocompleteService.prototype.suggestResource = function(request, response) {
	var term = request.term;
	if (this.suggest) {
		var array = this.suggest.resource[term.trim().toLowerCase()] || [];
		
		var result = [];
		for (var i=0; i<array.length; i++) {
			var object = array[i];
			result.push({
				label: object.prefLabel,
				value: object.id
			});
		}
		
		response(result);
		
	} else {
		response([]);
	}
	
}

/******************************************************************************/

konig.mapService = new MockMapService();
konig.autocompleteService = new MockAutocompleteService();
konig.graphService = new MockGraphService();
	
});