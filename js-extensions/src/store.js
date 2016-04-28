/**
 * The registered ExtensionPoint instances.
 */
var points = {};
/**
 * The ExtensionPoint metadata.
 */
var extensionPointList = [];

exports.setExtensionPointMetadata = function(data) {
    // This data should come from <jenkins>/blue/javaScriptExtensionInfo
    if (data) {
        // We clone the data because we add to it.
        extensionPointList = JSON.parse(JSON.stringify(data));
        var cssloadtracker = require('./cssloadtracker');
        cssloadtracker.setExtensionPointMetadata(extensionPointList);
    }
};

exports.addExtensionPoint = function(key) {
    points[key] = points[key] || [];
};

exports.addExtension = function (key, extension) {
    exports.addExtensionPoint(key);
    points[key].push(extension);
};

exports.loadExtensions = function(key, onload) {
    loadBundles(key, function() {
        onload(exports.getExtensions(key));
    });
};

exports.getExtensions = function(key) {
    return points[key] || [];
};

exports.getExtensionList = function() {
    return extensionPointList;
};

function LoadCountMonitor() {
    this.counter = 0;
    this.callback = undefined;
}
LoadCountMonitor.prototype.inc = function() {
    this.counter++;
    if (this.callback) {
        this.callback();
    }
};
LoadCountMonitor.prototype.dec = function() {
    this.counter--;
    if (this.callback) {
        this.callback();
    }
};
LoadCountMonitor.prototype.onchange = function(callback) {
    this.callback = callback;
};

function loadBundles(extensionPointId, onBundlesLoaded) {

    var jsModules = require('@jenkins-cd/js-modules');
    var loadCountMonitor = new LoadCountMonitor();

    function loadPluginBundle(pluginMetadata) {
        loadCountMonitor.inc();

        // The plugin bundle for this plugin may already be in the process of loading (async extension
        // point rendering). If it's not, pluginMetadata.loadCountMonitors will not be undefined,
        // which means we can go ahead with the async loading. If it is, pluginMetadata.loadCountMonitors
        // is defined, we just add "this" loadCountMonitor to pluginMetadata.loadCountMonitors.
        // It will get called as soon as the script loading is complete.
        if (!pluginMetadata.loadCountMonitors) {
            pluginMetadata.loadCountMonitors = [];
            pluginMetadata.loadCountMonitors.push(loadCountMonitor);
            jsModules.import(pluginMetadata.hpiPluginId + ':jenkins-js-extension')
                .onFulfilled(function() {
                    pluginMetadata.bundleLoaded = true;
                    for (var i = 0; i < pluginMetadata.loadCountMonitors.length; i++) {
                        pluginMetadata.loadCountMonitors[i].dec();
                    }
                    delete pluginMetadata.loadCountMonitors;
                });
        } else {
            pluginMetadata.loadCountMonitors.push(loadCountMonitor);
        }
    }

    function checkLoading() {
        if (loadCountMonitor.counter === 0) {
            onBundlesLoaded();
        }
    }

    // Iterate over each plugin in extensionPointMetadata, async loading
    // the extension point .js bundle (if not already loaded) for each of the
    // plugins that implement the specified extensionPointId.
    for(var i1 = 0; i1 < extensionPointList.length; i1++) {

        var pluginMetadata = extensionPointList[i1];
        var extensions = pluginMetadata.extensions;

        for(var i2 = 0; i2 < extensions.length; i2++) {
            if (extensions[i2].extensionPoint === extensionPointId) {
                // This plugin implements the ExtensionPoint.
                // If we haven't already loaded the extension point
                // bundle for this plugin, lets load it now.
                if (!pluginMetadata.bundleLoaded) {
                    loadPluginBundle(pluginMetadata);
                }
            }
        }
    }

    // Listen to the inc/dec calls now that we've iterated
    // over all of the plugins.
    loadCountMonitor.onchange(function() {
        checkLoading();
    });

    // Call checkLoading immediately in case all plugin
    // bundles have been loaded already.
    checkLoading();
}
