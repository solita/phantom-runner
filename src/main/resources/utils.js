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
    var strings = {};
    
    strings.formatString = function(str/*, arguments */) {
        var formatted = str;
        for (var arg in arguments) {
            // skip first since it is the original string
            if (arg > 0) {
                // we want the string indexing to start from 0, thus (arg - 1)
                formatted = formatted.replace("{" + (arg - 1) + "}", arguments[arg]);
            }
        }
        return formatted;
    };

    strings.dateToStr = function(date, fmt) {
        var result = fmt == null ? "dd.MM.yyyy HH:mm:ss" : fmt;
        
        var leadingZeroes = function(str, width) {
            var result = str;
            
            if (result.length > width) {
                return result.substr(result.length - width);
            }
            
            while (result.length < width) {
                result = "0" + result;
            }
            return result;
        };
        
        var timeElements = [
            {
                char: "d",
                format: function(date, length) {
                    return leadingZeroes(date.getDate(), length);
                }
            }, 
            {
                char:"M",
                format: function(date, length) {
                    return leadingZeroes(date.getMonth() + 1, length);
                }
            }, 
            {
                char: "y",
                format: function(date, length) {
                    return leadingZeroes(date.getFullYear(), length);
                }
            }, 
            {
                char: "H",
                format: function(date, length) {
                    return leadingZeroes(date.getHours(), length);
                }
            }, 
            {
                char: "m",
                format: function(date, length) {
                    return leadingZeroes(date.getMinutes(), length);
                }
            }, 
            {
                char: "s",
                format: function(date, length) {
                    return leadingZeroes(date.getSeconds(), length);
                }
            }];
        
        var findBounds = function(str, char) {
            var start = str.indexOf(char);
            var end = start;
            for (; end + 1 < str.length && str[end + 1] == char; end++);
            
            return [start, end];
        };
        
        for (var i = 0; i < timeElements.length; i++) {
            var bounds = findBounds(result, timeElements[i].char);
            result = result.replace(new RegExp(timeElements[i].char + "+"), timeElements[i].format(date));
        }
        return result;
    };
    
    var logger = function(obj) {    
        function createLogger(loggingFunction) {
            return function() {
                if (window.console && (typeof console[loggingFunction] === 'function' || typeof console[loggingFunction] === 'object')) { 
                    var log = null;
                    var args = Array.prototype.slice.call(arguments);
                    if (typeof console[loggingFunction] === 'function') {
                        // typical case
                        log = console[loggingFunction];
                    } else {
                        // IE9 case
                        log = Function.prototype.bind.call(console[loggingFunction], console);
                        for (key in args) {
                            args[key] = args[key] + " ";
                        }
                    }
                    args.splice(0,0, strings.dateToStr(new Date()) + ' [' + obj + '] -');
                    log.apply(console, args);
                }
                return arguments.length == 1 ? arguments[0] : arguments;
            };
        }
        
        var logger = createLogger("log");
        logger.error = createLogger("error");
        logger.warn = createLogger("warn");
        logger.info = createLogger("info");
        logger.debug = createLogger("debug");
        
        return logger;
    };
    
    exports.strings = strings;
    exports.logger = logger;
}());