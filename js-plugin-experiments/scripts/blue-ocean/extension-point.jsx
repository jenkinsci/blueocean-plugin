import React, {Component, PropTypes} from 'react';
import ReactDOM from 'react-dom';

//--------------------------------------------------------------------------
//
//  ExtensionPointStore
//
//--------------------------------------------------------------------------

// TODO: move this into a proper redux store + reducer

export class ExtensionPointStore {
    constructor() {
        this.points = {};
    }

    addExtensionPoint(key) {
        //console.log("addExtensionPoint");
        this.points[key] = this.points[key] || [];
    }

    addExtension(key, extension) {
        //console.log("addExtension");
        this.addExtensionPoint(key);
        this.points[key].push(extension);
    }

    getExtensions(key) {
        //console.log("getExtensions", key, this.points);
        return this.points[key] || [];
    }

    registerListener(key, handler) {
    }

    unRegisterListener(key, handler) {
    }
}

// Ugly global
export const extensionPointStoreSingleton = new ExtensionPointStore();

//--------------------------------------------------------------------------
//
//  ExtensionPoint
//
//--------------------------------------------------------------------------

/**
 * TODO: Docs
 */
export class ExtensionPoint extends Component {

    componentDidMount() {
        this._renderAllExtensions();
    }

    componentDidUpdate() {
        this._renderAllExtensions();
    }

    componentWillUnmount() {
        this._unmountAllExtensions();
    }

    render() {
        var extensionDivs = [];
        var extensions = extensionPointStoreSingleton.getExtensions(this.props.name);

        for (var i = 0; i < extensions.length; i++) {
            extensionDivs.push(<div key={i}/>);
        }

        return React.createElement(this.props.wrappingElement, null, extensionDivs);
    }

    _renderAllExtensions() {
        // TODO: This needs to be a lot cleverer if the list of extensions for a specific point can change
        const el = ReactDOM.findDOMNode(this).children;
        const extensions = extensionPointStoreSingleton.getExtensions(this.props.name);
        for (let i = 0; i < extensions.length; i++) {
            this._renderExtension(el[i], extensions[i]);
        }
    }

    /** Actually render an individual extension */
    _renderExtension(element, extension) {
        var component = React.createElement(extension, this.props);
        try {
            ReactDOM.render(component, element);
        } catch (e) {
            console.log("error rendering", extension.name, e);

            var errorDiv = <div className="error alien">Error rendering {extension.name}: {e.toString()}</div>;
            ReactDOM.render(errorDiv, element);
        }
    }

    /** Clean up child extensions */
    _unmountAllExtensions() {
        for (let node of ReactDOM.findDOMNode(this).children) {
            ReactDOM.unmountComponentAtNode(node); // TODO: Can this throw?
        }
    }
}

ExtensionPoint.defaultProps = {
    wrappingElement: "div"
};

ExtensionPoint.propTypes = {
    name: PropTypes.string.isRequired,
    wrappingElement: PropTypes.oneOfType([PropTypes.string, PropTypes.element])
};
