var logging = require('@jenkins-cd/logging');

var debugs = [];

console.debug = console.log;
/**
 * Turn on debug
 */
function setDEBUG() {
    process.env.DEBUG = debugs.join(',');
}
/**
 * enable debug for a specific category
 * @param category
 */
exports.enable = function(category) {
    if (debugs.indexOf(category) === -1) {
        debugs.push(category);
        setDEBUG();
    }
};

/**
 * disable debug for a specific category
 * @param category
 */
exports.disable = function(category) {
    var indexOf = debugs.indexOf(category);
    if (indexOf !== -1) {
        debugs = debugs.splice(indexOf, 1);
        setDEBUG();
    }
};

if (process.env.LOG_CONFIG) {
    process.env.DEBUG = process.env.LOG_CONFIG;
}
