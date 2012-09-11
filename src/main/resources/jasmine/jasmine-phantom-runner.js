(function() {
	var server = require('webserver').create();
	var system = require('system');

	var libPaths = system.args.slice(1);
	
	var errorHandler = function(result) {
		console.log("Error occured");
		for (var key in result.error) {
			if (typeof(result.error[key]) == 'object') {
				
			} else {
				console.log(key + ":", result.error[key]);
			}
		}
	};
	
	var runHandler = function(request, response) {
		response.statusCode = 200;
		var page = require('webpage').create();
		for (var key in libPaths) {
			if (!page.injectJs(libPaths[key])) {
				throw "Couldn't inject Javascript resource: " + libPaths[key];
			}
		}
		page.onConsoleMessage = function (msg) { 
			console.log(msg); 
		};
		
		var result = page.evaluate(function(testData) {	
			try {
				window.eval(testData);
			} catch (e) {
				return {error: e};
			}
		}, request.post);
		
		if (result && result.error) {
			errorHandler(result);
		}
		
		response.write("");
		response.close();
	};

	var parseHandler = function(url) {
		return {
			handle: (function() {
				if (url.indexOf("run") != -1) {
					return runHandler;
				} else {
					throw "Unsupported operation";
				}
			})()
		};
	};

	var service = server.listen(18080, function (request, response) {
		try {
			parseHandler(request.url).handle(request, response);
		} catch (e) {
			response.statusCode = 500;
			response.write('<html><body><h1>Error</h1><p>' + e + '</p></body></html>');
			console.error(e);
			response.close();
		}
	});
}());