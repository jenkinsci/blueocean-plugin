describe("store.js", function () {

    it("- test ", function (done) {
        var javaScriptExtensionInfo = require('./javaScriptExtensionInfo-01.json');
        var store = require('../store');
        var jsModules = require('@jenkins-cd/js-modules');
        var pluginsLoaded = {};
        var loaded = 0;

        // Mock the calls to addScript
        jsModules.addScript = function(scriptUrl, options) {
            expect(scriptUrl).toBe('io/jenkins/' + options.hpiPluginId + '/jenkins-js-extension.js');
            // mimic registering of those extensions
            for(var i1 = 0; i1 < javaScriptExtensionInfo.length; i1++) {
                var pluginMetadata = javaScriptExtensionInfo[i1];
                var extensions = pluginMetadata.extensions;
                for(var i2 = 0; i2 < extensions.length; i2++) {
                    store.addExtension(extensions[i2].component, extensions[i2].extensionPoint);
                }
            }
            if (pluginsLoaded[options.hpiPluginId] === undefined) {
                pluginsLoaded[options.hpiPluginId] = true;
                loaded++;
            }
            options.success();
        };

        // Initialise the store with some extension point info. At runtime,
        // this info will be loaded from <jenkins>/blue/javaScriptExtensionInfo
        store.setExtensionPointMetadata(javaScriptExtensionInfo);

        // Call load for ExtensionPoint impls 'ep-1'. This should mimic
        // the store checking all plugins and loading the bundles for any
        // plugins that define an impl of 'ep-1' (if not already loaded).
        store.loadExtensions('ep-1', function() {
            if (loaded === 2) {
                expect(pluginsLoaded['plugin-1']).toBeDefined();
                expect(pluginsLoaded['plugin-2']).toBeDefined();

                // if we call load again, nothing should happen as
                // all plugin bundles have been loaded i.e. loaded
                // should still be 2 (i.e. unchanged).
                store.loadExtensions('ep-1', function() {
                    expect(loaded, 2);

                    // Calling it yet again for different extension point, but
                    // where the bundles for that extension point have already.
                    store.loadExtensions('ep-2', function() {
                        expect(loaded, 2);
                        done();
                    });
                });
            }
        });
    });
});
