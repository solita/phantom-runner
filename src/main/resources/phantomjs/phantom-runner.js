(function() {
	var server = require('webserver').create();
	var system = require('system');

	var libPaths = system.args.slice(1);

	var waitFor = function(testFx, onReady, timeOutMillis) {
	    var maxtimeOutMillis = timeOutMillis ? timeOutMillis : 3001, //< Default Max Timout is 3s
	        start = new Date().getTime(),
	        condition = false,
	        interval = setInterval(function() {
	            if ( (new Date().getTime() - start < maxtimeOutMillis) && !condition ) {
	                // If not time-out yet and condition not yet fulfilled
	                condition = (typeof(testFx) === "string" ? eval(testFx) : testFx()); //< defensive code
	            } else {
	                if(!condition) {
	                    // If condition still not fulfilled (timeout but condition is 'false')
	                    console.log("'waitFor()' timeout");
	                    phantom.exit(1);
	                } else {
	                    // Condition fulfilled (timeout and/or condition is 'true')
	                    console.log("'waitFor()' finished in " + (new Date().getTime() - start) + "ms.");
	                    typeof(onReady) === "string" ? eval(onReady) : onReady(); //< Do what it's supposed to do once the condition is fulfilled
	                    clearInterval(interval); //< Stop this interval
	                }
	            }
	        }, 100); //< repeat check every 250ms
	};
	
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
		
		waitFor(function(){
            return page.evaluate(function(){
                var el = document.getElementById('qunit-testresult');
                if (el && el.innerText.match('completed')) {
                    return true;
                }
                return false;
            });
        }, function(){
            var failedNum = page.evaluate(function(){
                var el = document.getElementById('qunit-testresult');
                
                try {
                    return el.getElementsByClassName('failed')[0].innerHTML;
                } catch (e) { }
                return 10000;
            });
            response.write(JSON.stringify({failed: failedNum}));
            response.close();
        });
		
		/*var result = page.evaluate(function(testData) {
			try {
				eval(testData);
				
				console.log();
			} catch (e) {
				return {error: e};
			}
		}, request.post);
		
		if (result && result.error) {
			errorHandler(result);
		}*/
		
		//response.write("");
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