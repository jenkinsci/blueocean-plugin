/**
 * TODO: Docs
 * @type {RegExp}
 */

//===[ Imports ]====================================================================================================

import React, {Component} from 'react';

//===[ Consts ]=====================================================================================================

//===[ Extensions ]=================================================================================================

// Manager
export class ExtensionPointStore {
    constructor() {
        this.points = {};
    }

    addExtensionPoint(key) {
        console.log("addExtensionPoint");
        this.points[key] = this.points[key] || [];
    }

    addExtension(key, extension) {
        console.log("addExtension");
        this.addExtensionPoint(key);
        this.points[key].push(extension);
    }

    getExtensions(key) {
        console.log("getExtensions", key, this.points);
        return this.points[key] || [];
    }

    registerListener(key, handler) {
    }

    unRegisterListener(key, handler) {
    }
}

// Ugly global
export const extensionPointStore = new ExtensionPointStore();

// Extension API for future
export class Extension {
    mount(element, props) {
    }

    updated(props) {
    }

    unmount() {
    }
}

var key = 1; // TODO: This is bad, mmkay

function _renderExtension(extension) {
    var $$ = extension; // TODO: Why the fuck do I need this?
    try {
        console.log("_renderExtension", extension);
        return <$$ {...this.props} key={key++}/>
    } catch (e) {
        console.log("error rendering", extension.name, e);
        return <div key={key++}>Error rendering {extension.name}: {e}</div>
    }
}

// Renderer
export class ExtensionPoint extends Component {

    render() {
        return (
            <div>
                {extensionPointStore.getExtensions(this.props.name).map(_renderExtension.bind(this))}
            </div>)
    }
}

//===[ PluginManager ]==============================================================================================

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