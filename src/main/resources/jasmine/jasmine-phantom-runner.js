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
	
	// websocket stuff
	var ws = new WebSocket("ws://localhost:18080");
	ws.onmessage = function(e) {
		var data = JSON.parse(e.data);
		parseHandler(data.type).handle(data);
	};
	
	var page = null;
	
	var initHandler = function(data) {
		// release previous page data from memory if any
		if (page != null) {
			page.release();
		}
		
		page = require('webpage').create();
		
		for (var key in data.libDatas) {
			page.evaluate(function(libData) {
				window.eval(libData);
			}, data.libDatas[key]);
		}
		
		for (var key in data.extLibs) {
			page.evaluate(function(libData) {
				window.eval(libData);
			}, data.extLibs[key]);
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
		}, data.testFileData);
		
		if (result && result.error) {
			errorHandler(result);
		}
		
		ws.send(JSON.stringify({}));
	};
	
	var runHandler = function(data) {
		var resultJson = page.evaluate(function(data) {
			var isSuiteRequest = function(testName) {
				return testName.indexOf("#!#") == -1;
			};
			
			var parseSuiteName = function(testName) {
				return testName.split("#!#")[0];
			};
			
			var parseTestName = function(testName) {
				return testName.split("#!#")[1];
			};
			
			var findSuite = function(testName) {
				var suiteName = parseSuiteName(testName);
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
			
			if (isSuiteRequest(data.testName)) {
				findSuite(data.testName).execute();
			} else {
				findTest(data.testName).execute();				
			}
			
			var results = jasmine.getEnv().currentRunner().results();
			
			var jsonResult = {
				passed: results.passed(),
				totalCount: results.totalCount,
				passedCount: results.passedCount,
				failedCount: results.failedCount
			};
			
			return JSON.stringify(jsonResult);
		}, data);
		
		ws.send(resultJson);
	};
	
	var parseHandler = function(type) {
		return {
			handle: (function() {
				if (type.indexOf("run") != -1) {
					return runHandler;
				} else if (type.indexOf("init") != -1) { 
					return initHandler;
				} else {
					throw "Unsupported operation";
				}
			})()
		};
	};
	
}());