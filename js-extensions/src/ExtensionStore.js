/**
 * ExtensionStore is responsible for maintaining extension metadata
 * including type/capability info
 */
export default class ExtensionStore {
    /**
     *  FIXME this is NOT a constructor, as there's no common way to
     *  pass around a DI singleton at the moment across everything
     *  that needs it (e.g. redux works for the app, not for other
     *  things in this module).
     *
     *  NOTE: this is currently called from `blueocean-web/src/main/js/init.jsx`
     *
     *  Needs:
     *  args = {
     *      extensionData: [array of extensions data],
     *      classMetadataStore: {
     *          getClassMetadata(dataType, callback) => {
     *              ... // get the data based on 'dataType'
     *              callback(typeInfo);
     *          }
     *      }
     *  }
     */
    init(args) {
        this.extensionData = args.extensionData;
        this.extensionPointList = undefined; // cache from extensionData. See _initExtensionPointList().

        /**
         * The registered ExtensionPoint metadata + instance refs
         */
        this.extensionPoints = {};
        /**
         * Used to fetch type information
         */
        this.classMetadataStore = args.classMetadataStore;

        // Now init the extension point list.
        this._initExtensionPointList();
    }

    /**
     * Register the extension script object
     */
    _registerComponentInstance(extensionPointId, pluginId, component, instance) {
        var extensions = this.extensionPoints[extensionPointId];
        if (!extensions) {
            this._loadBundles(extensionPointId, () => this._registerComponentInstance(extensionPointId, pluginId, component, instance));
            return;
        }
        var extension = this._findPlugin(extensionPointId, pluginId, component);
        if (extension) {
            extension.instance = instance;
            return;
        }
        throw new Error(`Unable to locate plugin for ${extensionPointId} / ${pluginId} / ${component}`);
    }

    /**
     * Finds a plugin by extension point id, plugin id, component name
     */
    _findPlugin(extensionPointId, pluginId, component) {
        var extensions = this.extensionPoints[extensionPointId];
        if (extensions) {
            for (var i = 0; i < extensions.length; i++) {
                var extension = extensions[i];
                if (extension.pluginId == pluginId && extension.component == component) {
                    return extension;
                }
            }
        }
    }

    /**
     * The primary function to use in order to get extensions,
     * will call the onload callback with a list of exported extension
     * objects (e.g. React classes or otherwise).
     */
    getExtensions(extensionPoint, filter, onload) {
        // Allow calls like: getExtensions('something', a => ...)
        if (arguments.length === 2 && typeof(filter) === 'function') {
            onload = filter;
            filter = undefined;
        }

        // And calls like: getExtensions(['a','b'], (a,b) => ...)
        if (extensionPoint instanceof Array) {
            var args = [];
            var nextArg = ext => {
                if(ext) args.push(ext);
                if (extensionPoint.length === 0) {
                    onload(...args);
                } else {
                    var arg = extensionPoint[0];
                    extensionPoint = extensionPoint.slice(1);
                    this.getExtensions(arg, filter, nextArg);
                }
            };
            nextArg();
            return;
        }

        this._loadBundles(extensionPoint, extensions => this._filterExtensions(extensions, filter, onload));
    }

    /**
     * Get the version string for the named plugin.
     * @param pluginName The plugin name/Id (short name).
     * @return The version string for the named plugin, or undefined if the plugin is not installed/active.
     */
    getPluginVersion(pluginName) {
        for(var i = 0; i < this.extensionPointList.length; i++) {
            var pluginMetadata = this.extensionPointList[i];
            if (pluginMetadata.hpiPluginId === pluginName) {
                return pluginMetadata.hpiPluginVer;
            }
        }

        return undefined;
    }

    _filterExtensions(extensions, filters, onload) {
        if (extensions.length === 0) {
            onload(extensions); // no extensions to filter
            return;
        }

        if (filters) {
            // allow calls like: getExtensions('abcd', dataType(something), ext => ...)
            if (!filters.length) {
                filters = [ filters ];
            }
            var remaining = [].concat(filters);
            var nextFilter = extensions => {
                if (remaining.length === 0) {
                    // Map to instances and proceed
                    onload(extensions.map(m => m.instance));
                } else {
                    var filter = remaining[0];
                    remaining = remaining.slice(1);
                    filter(extensions, nextFilter);
                }
            };
            nextFilter(extensions);
        } else {
            // Map to instances and proceed
            onload(extensions.map(m => m.instance));
        }
    }

    /**
     * Initialize the extension point list from the configured extension data.
     */
    _initExtensionPointList() {
        if (!this.extensionData) {
            throw new Error("Must call ExtensionStore.init({ extensionData: array, typeInfoProvider: (type, cb) => ... }) first");
        }
        if (this.extensionPointList) {
            return;
        }
        // We clone the data because we add to it.
        this.extensionPointList = JSON.parse(JSON.stringify(this.extensionData));
        for(var i1 = 0; i1 < this.extensionPointList.length; i1++) {
            var pluginMetadata = this.extensionPointList[i1];
            var extensions = pluginMetadata.extensions || [];

            for(var i2 = 0; i2 < extensions.length; i2++) {
                var extensionMetadata = extensions[i2];
                extensionMetadata.pluginId = pluginMetadata.hpiPluginId;
                var extensionPointMetadatas = this.extensionPoints[extensionMetadata.extensionPoint] = this.extensionPoints[extensionMetadata.extensionPoint] || [];
                extensionPointMetadatas.push(extensionMetadata);
            }
        }
        this.resourceLoadTracker.setExtensionPointMetadata(this.extensionPointList);
    }

    /**
     * Load the bundles for the given type
     */
    _loadBundles(extensionPointId, onload) {
        var extensionPointMetadatas = this.extensionPoints[extensionPointId];
        if (extensionPointMetadatas && extensionPointMetadatas.loaded) {
            onload(extensionPointMetadatas);
            return;
        }

        extensionPointMetadatas = this.extensionPoints[extensionPointId] = this.extensionPoints[extensionPointId] || [];

        var jsModules = require('@jenkins-cd/js-modules');
        var loadCountMonitor = new LoadCountMonitor();

        var loadPluginBundle = (pluginMetadata) => {
            loadCountMonitor.inc();

            // The plugin bundle for this plugin may already be in the process of loading (async extension
            // point rendering). If it's not, pluginMetadata.loadCountMonitors will not be undefined,
            // which means we can go ahead with the async loading. If it is, pluginMetadata.loadCountMonitors
            // is defined, we just add "this" loadCountMonitor to pluginMetadata.loadCountMonitors.
            // It will get called as soon as the script loading is complete.
            if (!pluginMetadata.loadCountMonitors) {
                pluginMetadata.loadCountMonitors = [];
                pluginMetadata.loadCountMonitors.push(loadCountMonitor);
                jsModules.importModule(pluginMetadata.hpiPluginId + ':jenkins-js-extension')
                    .onFulfilled(() => {
                        pluginMetadata.bundleLoaded = true;
                        for (var i = 0; i < pluginMetadata.loadCountMonitors.length; i++) {
                            pluginMetadata.loadCountMonitors[i].dec();
                        }
                        delete pluginMetadata.loadCountMonitors;
                    });
            } else {
                pluginMetadata.loadCountMonitors.push(loadCountMonitor);
            }
        };

        var checkLoading = () => {
            if (loadCountMonitor.counter === 0) {
                extensionPointMetadatas.loaded = true;
                onload(extensionPointMetadatas);
            }
        };

        // Iterate over each plugin in extensionPointMetadata, async loading
        // the extension point .js bundle (if not already loaded) for each of the
        // plugins that implement the specified extensionPointId.
        for(var i1 = 0; i1 < this.extensionPointList.length; i1++) {

            var pluginMetadata = this.extensionPointList[i1];
            var extensions = pluginMetadata.extensions || [];

            for(var i2 = 0; i2 < extensions.length; i2++) {
                var extensionMetadata = extensions[i2];
                if (extensionMetadata.extensionPoint === extensionPointId) {
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
        loadCountMonitor.onchange( () => {
            checkLoading();
        });

        // Call checkLoading immediately in case all plugin
        // bundles have been loaded already.
        checkLoading();
    }
}

/**
 * Maintains load counts for components
 */
class LoadCountMonitor {
    constructor() {
        this.counter = 0;
        this.callback = undefined;
    }

    inc() {
        this.counter++;
        if (this.callback) {
            this.callback();
        }
    }

    dec() {
        this.counter--;
        if (this.callback) {
            this.callback();
        }
    }

    onchange(callback) {
        this.callback = callback;
    }
}
