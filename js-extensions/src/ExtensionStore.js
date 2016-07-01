/**
 * ExtensionStore is responsible for maintaining extension metadata
 * including type/capability info
 */
export class ExtensionStore {
    /**
     *  FIXME this is NOT a constructor, as there's no common way to
     *  pass around a DI singleton at the moment across everything
     *  that needs it (e.g. redux works for the app, not for other
     *  things in this module)
     *  
     *  Needs:
     *  args = {
     *      extensionDataProvider: callback => {
     *          ... // get the data
     *          callback(extensionData); // array of extensions
     *      },
     *      typeInfoProvider: (type, callback) => {
     *          ... // get the data based on 'type'
     *          callback(typeInfo);
     *      }
     *  }
     */
    init(args) {
        // This data should come from <jenkins>/blue/js-extensions
        this.extensionDataProvider = args.extensionDataProvider;
        this.extensionPointList = undefined; // cache from extensionDataProvider...
        /**
         * The registered ExtensionPoint metadata + instance refs
         */
        this.extensionPoints = {};
        /**
         * Type info cache
         */
        this.typeInfo = {};
        /**
         * Used to fetch type information
         */
        this.typeInfoProvider = args.typeInfoProvider;
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
        throw `Unable to locate plugin for ${extensionPointId} / ${pluginId} / ${component}`;
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
    getExtensions(key, type, onload) {
        if (!this.extensionDataProvider) {
            throw "Must call ExtensionStore.init({ extensionDataProvider: (cb) => ..., typeInfoProvider: (type, cb) => ... }) first";
        }
        // Allow calls like: getExtensions('something', a => ...)
        if (arguments.length === 2 && typeof(type) === 'function') {
            onload = type;
            type = undefined;
        }
        // And calls like: getExtensions(['a','b'], (a,b) => ...)
        if (key instanceof Array) {
            var keys = key;
            var args = [];
            var nextArg = ext => {
                if(ext) args.push(ext);
                if (keys.length === 0) {
                    onload(...args);
                } else {
                    var arg = keys[0];
                    keys = keys.slice(1);
                    this.getExtensions(arg, null, nextArg);
                }
            };
            nextArg();
            return;
        }
        
        this._loadBundles(key, extensions => this._filterExtensions(extensions, key, type, onload));
    }
    
    /**
     * Gets the type/capability info for the given data type
     */
    getTypeInfo(type, onload) {
        var ti = this.typeInfo[type];
        if (ti) {
            return onload(ti);
        }
        this.typeInfoProvider(type, (data) => {
            ti = this.typeInfo[type] = JSON.parse(JSON.stringify(data));
            ti.classes = ti.classes || [];
            if (ti.classes.indexOf(type) < 0) {
                ti.classes = [type, ...ti.classes];
            }
            onload(ti);
        });
    }

    _filterExtensions(extensions, key, currentDataType, onload) {
        if (currentDataType && typeof(currentDataType) === 'object'
                && '_class' in currentDataType) { // handle the common API incoming data
            currentDataType = currentDataType._class;
        }
        if (extensions.length === 0) {
            onload(extensions); // no extensions for the given key
            return;
        }
        if (currentDataType) {
            var currentTypeInfo = this.typeInfo[currentDataType];
            if (!currentTypeInfo) {
                this.getTypeInfo(currentDataType, () => {
                    this._filterExtensions(extensions, key, currentDataType, onload);
                });
                return;
            }
            // prevent returning extensions for the given type
            // when a more specific extension is found
            var matchingExtensions = [];
            eachType: for (var typeIndex = 0; typeIndex < currentTypeInfo.classes.length; typeIndex++) {
                // currentTypeInfo.classes is ordered by java hierarchy, including
                // and beginning with the current data type
                var type = currentTypeInfo.classes[typeIndex];
                for (var i = 0; i < extensions.length; i++) {
                    var extension = extensions[i];
                    if (type === extension.type) {
                        matchingExtensions.push(extension);
                    }
                }
                // if we have this specific type handled, don't
                // proceed to parent types
                if (matchingExtensions.length > 0) {
                    break eachType;
                }
            }
            extensions = matchingExtensions;
        } else {
            // exclude typed extensions when types not requested
            extensions = extensions.filter(m => !('type' in m));
        }
        onload(extensions.map(m => m.instance));
    }
    
    /**
     * Fetch all the extension data
     */
    _loadExtensionData(oncomplete) {
        if (this.extensionPointList) {
            onconplete(this.extensionPointList);
            return;
        }
        this.extensionDataProvider(data => {
            // We clone the data because we add to it.
            this.extensionPointList = JSON.parse(JSON.stringify(data));
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
            var ResourceLoadTracker = require('./ResourceLoadTracker').instance;
            ResourceLoadTracker.setExtensionPointMetadata(this.extensionPointList);
            if (oncomplete) oncomplete(this.extensionPointList);
        });
    }

    /**
     * Load the bundles for the given type
     */
    _loadBundles(extensionPointId, onload) {
        // Make sure this has been initialized first
        if (!this.extensionPointList) {
            this._loadExtensionData(() => {
                this._loadBundles(extensionPointId, onload);
            });
            return;
        }
        
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
                jsModules.import(pluginMetadata.hpiPluginId + ':jenkins-js-extension')
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

// should figure out DI with singletons so we can move
// required providers to other injection points, ideally
export const instance = new ExtensionStore();
