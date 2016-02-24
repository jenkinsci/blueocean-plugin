// See https://github.com/jenkinsci/js-test

var jsTest = require("jenkins-js-test");

describe("blueocean.js", function () {

    it("- test init", function (done) {
        jsTest.onPage(function() {
            // Load blueocean module. That will trigger init, which will export js-extensions,
            // making it available from js-modules to other .js bundles in plugins etc.
            jsTest.requireSrcModule('blueocean');

            // An import of jenkins-cd:js-extensions should work. This mimics what will happen
            // in plugins. They will build their bundles using js-builder, which will import
            // @jenkins-cd/js-extensions and use it to register the ExtensionPoint impls defined
            // in the plugin's metadata file.
            var jenkinsMods = require('jenkins-js-modules');
            jenkinsMods.import('jenkins-cd:js-extensions')
                .onFulfilled(function(extensions) {
                    expect(extensions).toBeDefined();
                    done();
                });
        });
    });
});