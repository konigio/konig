//Lets require/import the HTTP module
var http = require('http'),     
	url = require('url'),
	path = require('path'),
	fs = require('fs');

var mimeTypes = {
		"jsonld": "application/ld+json", 
	    "html": "text/html",
	    "jpeg": "image/jpeg",
	    "jpg": "image/jpeg",
	    "png": "image/png",
	    "js": "text/javascript",
	    "css": "text/css"};


//Lets define a port we want to listen to
const PORT=8000; 


//Create a server
var server = http.createServer(function(req,res){
	// Set CORS headers
	res.setHeader('Access-Control-Allow-Origin', '*');
	res.setHeader('Access-Control-Request-Method', '*');
	res.setHeader('Access-Control-Allow-Methods', 'OPTIONS, GET');
	res.setHeader('Access-Control-Allow-Headers', '*');
	res.setHeader('Cache-Control', 'no-cache, no-store, must-revalidate');
	
	var uri = url.parse(req.url).pathname;
    var filename = path.join(process.cwd(), "src", uri);
    fs.exists(filename, function(exists) {
        if(!exists) {
            console.log("not exists: " + filename);
            res.writeHead(200, {'Content-Type': 'text/plain'});
            res.write('404 Not Found\n');
            res.end();
            return;
        }
        var mimeType = mimeTypes[path.extname(filename).split(".")[1]];
        res.writeHead(200, {'Content-Type' : mimeType});

        var fileStream = fs.createReadStream(filename);
        fileStream.pipe(res);
    }); //end path.exists
});

//Lets start our server
server.listen(PORT, function(){
    //Callback triggered when server is successfully listening. Hurray!
    console.log("Server listening on: http://localhost:%s", PORT);
});