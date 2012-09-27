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