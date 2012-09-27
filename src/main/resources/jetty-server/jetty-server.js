(function() {
    // re-expose the jetty-server under the generic PhantomServer 
    phantomrunner.modules.PhantomServer = {}
    
    phantomrunner.modules.PhantomServer.initRunner = function(runHandler, logger) {
        var log = logger == null ? require(phantomrunner.modules.utils).logger("JettyHandler") : logger;
        
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
        
        // websocket stuff
        var ws = new WebSocket("ws://localhost:18080");
        ws.onmessage = function(e) {
            var data = JSON.parse(e.data);
            ws.send(parseHandler(data.type).handle(data, page));
        };
    };

}());