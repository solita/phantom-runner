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
var phantomrunner = phantomrunner == undefined ? {} : phantomrunner;

(function() {
    // initializes all JavaScript files given as system parameter
    var system = require("system");
    var fs = require('fs');
    
    // since PhantomJS require-mechanism works on relative paths not on module names as typically
    // with common-js we need to provide these paths everywhere. Sucks, but such is life.
    phantomrunner.modules = {};
    
    system.args.forEach(function (arg, i) {
        // first parameter is this file
        if (i > 0) {
            // PhantomJS doesn't want to see Windows drive letters and it want's the path to be
            // separated by /, never with \
            var phantomisedPath = arg.replace(new RegExp("\\\\", "g"), "/").replace(/[CDEFGH]:/i, "");
            var moduleName = phantomisedPath.substring(phantomisedPath.lastIndexOf('/') + 1, phantomisedPath.lastIndexOf('.js'));
            
            phantomrunner.modules[moduleName] = phantomisedPath;
        }
    });
    
    // execute the server script file (always second in the argument list)
    eval(fs.read(system.args[1]));
    
    // and then execute the runner file, after this everything should go on automatically
    eval(fs.read(system.args[2]));
}());