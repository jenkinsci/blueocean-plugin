var points = {};

exports.addExtensionPoint = function(key) {
    points[key] = points[key] || [];
};

exports.addExtension = function (key, extension) {
    exports.addExtensionPoint(key);
    points[key].push(extension);
};

exports.getExtensions = function(key) {
    return points[key] || [];
};