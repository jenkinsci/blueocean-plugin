/**
 * TODO: Docs
 * @type {RegExp}
 */

//===[ Imports ]====================================================================================================

import React, {Component} from 'react';
import ReactDOM from 'react-dom';

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

/** Actually render the extension */
function _renderExtension(element, extension, props) {
    var $$ = extension; // TODO: Why the f*** do I need this?
    console.log("_renderExtension", extension);
    
    var component = <$$ {...props} />
    try {
        ReactDOM.render(component, element);
    } catch (e) {
      console.log("error rendering", extension.name, e);
      
      var errorDiv = <div className="error alien">Error rendering {extension.name}: {e.toString()}</div>;
      ReactDOM.render(errorDiv, element);
    }
}

// Renderer
export class ExtensionPoint extends Component {
    componentDidMount() {
      this._renderExtensions();
    }
    componentDidUpdate() {
      this._renderExtensions();
    }
    render() {
        var extensionDivs = [];
        extensionPointStore.getExtensions(this.props.name).forEach(() => {
          extensionDivs.push(<div/>);
        });
        return <div>
          {extensionDivs}
        </div>;
    }
    
    _renderExtensions() {
          var el = ReactDOM.findDOMNode(this).children;
          var extensions = extensionPointStore.getExtensions(this.props.name);
          for(var i = 0; i < extensions.length; i++) {
            _renderExtension(el[i], extensions[i], this.props);
          }
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
