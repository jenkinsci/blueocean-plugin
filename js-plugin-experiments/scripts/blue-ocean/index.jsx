/**
 * TODO: Docs
 * @type {RegExp}
 */

//===[ Imports ]========================================================================================================

//===[ Consts ]=========================================================================================================

//===[ PluginManager ]==================================================================================================

const keyRegex = /^\w[-.\w\d_]+$/;

function validateKey(key) {
    if (typeof(key) !== "string") {
        throw new Error("Key not string");
    }
    if (!keyRegex.exec(key)) {
        throw new Error("Key didn't match " + keyRegex);
    }
}

export class PluginManager {
    constructor() {
        this.plugins = {};
    }

    registerPlugin(metadata) {
        const {key, name} = metadata;
        console.log("registerPlugin:", name, "key", key);

        validateKey(key);

        // TODO: Check for dupe keys
        this.plugins[key] = metadata;
        // TODO: notify changes to plugin list
        // TODO: look up dependencies
        // TODO: bind provided artefacts to namespace
        // TODO: call some sort of "init" method for the plugin
        // TODO: link plugin mappings to extension points
    }
}