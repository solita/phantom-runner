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