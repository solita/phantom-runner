/**
 * The MIT License (MIT)
 *
 * Copyright (c) 2012 Solita Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without
 * limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT
 * LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO
 * EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 */
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