//
// Explicit polyfill for Error functions not supported in IE
// and seemingly not polyfilled by Babel.
//

// Error.captureStackTrace
//
// Only on ref to it in the Babel org on GitHub:
// https://github.com/search?q=org%3Ababel+captureStackTrace&type=Code
//
Error.captureStackTrace = Error.captureStackTrace || function (obj) {
        if (Error.prepareStackTrace) {
            var frame = {
                isEval: function () {
                    return false;
                },
                getFileName: function () {
                    return "filename";
                },
                getLineNumber: function () {
                    return 1;
                },
                getColumnNumber: function () {
                    return 1;
                },
                getFunctionName: function () {
                    return "functionName";
                }
            };

            obj.stack = Error.prepareStackTrace(obj, [frame, frame, frame]);
        } else {
            obj.stack = obj.stack || obj.name || "Error";
        }
    };
