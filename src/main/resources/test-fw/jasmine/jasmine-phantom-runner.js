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
    var log = require(phantomrunner.modules.utils).logger("JasminePhantomRunner");
    
    var runHandler = function(data, page) {
        
        return page.evaluate(function(data) {
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
            
            var failMessage = "";
            if (!results.passed()) {
                failMessage = "Jasmine spec '" + parseTestName(data.testName) + "' in suite '" + parseSuiteName(data.testName) + "' failed";
            }
            
            var jsonResult = {
                passed: results.passed(),
                totalCount: results.totalCount,
                passedCount: results.passedCount,
                failedCount: results.failedCount,
                failMessage: failMessage
            };
            
            return JSON.stringify(jsonResult);
        }, data);
    };
    
    phantomrunner.modules.PhantomServer.initRunner(runHandler, log);
    
}());