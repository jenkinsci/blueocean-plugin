/**
 * Jenkins js-builder extension plugin. https://github.com/jenkinsci/js-builder
 *
 * Adds some js-extensions specific behaviour to the build.
 */

/**
 * Install the js-builder plugin. This function will be called by js-builder.
 */
exports.install = function() {
    var jsBundle = require('./subs/extensions-bundle');
    var cssBundle = require('./subs/css-bundle');

    var extensionsJSON = jsBundle.bundle();
    if (extensionsJSON) {
        // Attach plugin CSS info to the extensions JSON, if there is any.
        extensionsJSON = cssBundle.bundle(extensionsJSON);

        // Save extensions JSON again.
        jsBundle.setJSExtensionsJSON(extensionsJSON);
    }
};
