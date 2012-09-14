(function() {
	var server = require('webserver').create();
	var system = require('system');

	var errorHandler = function(result) {
		console.log("Error occured");
		for (var key in result.error) {
			if (typeof(result.error[key]) == 'object') {
				
			} else {
				console.log(key + ":", result.error[key]);
			}
		}
	};
	
	var page = null;
	
	var initHandler = function(request, response) {
		// release previous page data from memory if any
		if (page != null) {
			page.release();
		}
		
		page = require('webpage').create();
		
		var postData = JSON.parse(request.post); // content is JSON data
		
		for (var key in postData.libDatas) {
			page.evaluate(function(libData) {
				window.eval(libData);
			}, postData.libDatas[key]);
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
		}, postData.testFileData);
		
		if (result && result.error) {
			errorHandler(result);
		}
		
		
		response.statusCode = 200;
		response.write("");
		response.close();
	};
	
	var runHandler = function(request, response) {
		var resultJson = page.evaluate(function(request) {
			var isSuiteRequest = function(post) {
				return post.indexOf("#!#") == -1;
			};
			
			var parseSuiteName = function(post) {
				return post.split("#!#")[0];
			};
			
			var parseTestName = function(post) {
				return post.split("#!#")[1];
			};
			
			var findSuite = function(post) {
				var suiteName = parseSuiteName(post);
				var suites = jasmine.getEnv().currentRunner().suites();
				for (var i = 0; i < suites.length; i++) {
					if (suites[i].description === suiteName) {
						return suites[i];
					}
				}
				throw "No suite with name " + suiteName + " found, check your test initialization";
			};
			
			var findTest = function(post) {
				var suite = findSuite(post);
				var testName = parseTestName(post);
				for (var i = 0; i < suite.children().length; i++) {
					if (suite.children()[i].description === testName) {
						return suite.children()[i];
					}
				}
				throw "No test with name " + testName + " found, check your test initialization";
			};
			
			if (isSuiteRequest(request.post)) {
				findSuite(request.post).execute();
			} else {
				findTest(request.post).execute();				
			}
			
			var results = jasmine.getEnv().currentRunner().results();
			
			var jsonResult = {
				passed: results.passed(),
				totalCount: results.totalCount,
				passedCount: results.passedCount,
				failedCount: results.failedCount
			};
			
			return JSON.stringify(jsonResult);
		}, request);
		
		response.statusCode = 200;
		response.write(resultJson);
		response.close();
	};
	
	var parseHandler = function(url) {
		return {
			handle: (function() {
				if (url.indexOf("run") != -1) {
					return runHandler;
				} else if (url.indexOf("init") != -1) { 
					return initHandler;
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