(function() {
    // re-expose the jetty-server under the generic PhantomServer 
    phantomrunner.modules.PhantomServer = {}
    
    phantomrunner.modules.PhantomServer.initRunner = function(runHandler, logger) {
        var log = logger == null ? require(phantomrunner.modules.utils).logger("PhantomServerHandler") : logger;
        
        var page = null;
        
        var initHandler = function(data) {
            // release previous page data from memory if any
            if (page != null) {
                page.release();
            }
            
            page = require('webpage').create();
            
            page.onConsoleMessage = function (msg) { 
                log(msg);
            };

            // set the provided HTML as page content
            page.content = data.testFileData;
            
            return JSON.stringify({});
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
        
        // PhantomJS server initialisation and handling
        var server = require('webserver').create();

        // FIXME: how can we pass the HTTP port from JUnit configs into here?
        server.listen(18080, function(request, response) {
            try {
                var data = JSON.parse(request.post);
                var responseData = parseHandler(data.type).handle(data, page);
                
                response.setHeader("Content-Length", responseData.length);
                response.statusCode = 200;
                response.write(responseData);
                response.close();
            } catch (e) {
                response.statusCode = 500;
                response.write(JSON.stringify(e));
                response.close();
            }
        });
    };

}());