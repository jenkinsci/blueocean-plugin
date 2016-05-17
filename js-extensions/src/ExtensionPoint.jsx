var React = require('react');
var ReactDOM = require('react-dom');
var store = require('./store.js');
var cssloadtracker = require('./cssloadtracker');

// TODO: Move this package to babel, and update this to ES6

/**
 * An internal component that inserts things into the (separate) context of mounted extensions. We need this for our
 * configuration object, which helps resolve URLs for media, REST endpoints, etc, and we also need to bridge the
 * "router" context property in order for extensions to be able to use &lt;Link&gt; from react-router.
 */
var ContextBridge = React.createClass({

    getChildContext: function() {
        return {
            router: this.props.router,
            config: this.props.config
        };
    },

    render: function() {
        return this.props.children;
    }
});

ContextBridge.childContextTypes = {
    router: React.PropTypes.object,
    config: React.PropTypes.object
};

ContextBridge.propTypes = {
    router: React.PropTypes.object,
    config: React.PropTypes.object
};

/**
 * Implement an ExtensionPoint for which other plugins can provide an implementing Component.
 */
var ExtensionPoint = React.createClass({

    getInitialState: function () {
        // Initial state is empty. See the componentDidMount and render functions.
        return {};
    },

    componentDidMount: function() {
        cssloadtracker.onMount(this.props.name);
        var thisEp = this;
        ExtensionPoint.registerExtensionPoint(this.props.name, function(extensions) {
            thisEp.setState({
                extensions: extensions
            });
        });
    },

    componentDidUpdate: function() {
        this._renderAllExtensions();
    },

    componentWillUnmount: function() {
        this._unmountAllExtensions();
        cssloadtracker.onUnmount(this.props.name);
    },

    /**
     * This method renders the "leaf node" container divs, one for each registered extension, that live in the same
     * react hierarchy as the &lt;ExtensionPoint&gt; instance itself. As far as our react is concerned, these are
     * childless divs that are never updated. Actually rendering the extensions themselves is done by
     * _renderAllExtensions.
     */
    render: function() {
        var extensions = this.state.extensions;

        if (!extensions) {
            // Initial state. componentDidMount will kick in and load the extensions.
            return null;
        } else if (extensions.length === 0) {
            console.warn('No "' + this.props.name + '" ExtensionPoint implementations were found across the installed plugin set. See ExtensionList:');
            console.log(store.getExtensionList());
            return null;
        } else {
            // Add a <div> for each of the extensions. See the __renderAllExtensions function.
            var extensionDivs = [];
            for (var i = 0; i < extensions.length; i++) {
                extensionDivs.push(<div key={i}/>);
            }
            return React.createElement(this.props.wrappingElement, null, extensionDivs);
        }
    },

    /**
     * For each extension, we have created a "leaf node" element in the DOM. This method creates a new react hierarchy
     * for each, and instructs it to render. From that point on we have a separation that keeps the main app insulated
     * from any plugin issues that may cause react to throw while updating. Inspired by Nylas N1.
     */
    _renderAllExtensions: function() {
        // NB: This needs to be a lot cleverer if the list of extensions for a specific point can change;
        // We will need to link each extension with its containing element, in some way that doesn't leak :) Easy in
        // browsers with WeakMap, less so otherwise.
        const el = ReactDOM.findDOMNode(this);
        if (el) {
            const children = el.children;
            if (children) {
                const extensions = store.getExtensions(this.props.name);

                // The number of children should be exactly the same as the number
                // of extensions. See the render function for where these are added.
                if (!extensions || extensions.length !== children.length) {
                    console.error('Unexpected error in Jenkins ExtensionPoint rendering (' + this.props.name + '). Expecting a child DOM node for each extension point.');
                    return;
                }
                // render each extension on the allocated child node.
                for (var i = 0; i < extensions.length; i++) {
                    this._renderExtension(children[i], extensions[i]);
                }
            }
        }
    },

    /** Actually render an individual extension */
    _renderExtension: function(element, extension) {
        var component = React.createElement(extension, this.props);
        try {
            var contextValuesAsProps = {
                config: this.context.config,
                router: this.context.router
            };
            var bridgedComponent = React.createElement(ContextBridge, contextValuesAsProps, component);
            ReactDOM.render(bridgedComponent, element);
        } catch (e) {
            console.log("error rendering", extension.name, e);

            var errorDiv = <div className="error alien">Error rendering {extension.name}: {e.toString()}</div>;
            ReactDOM.render(errorDiv, element);
        }
    },

    /**
     * Clean up child extensions' react hierarchies. Necessary because they live in their own react hierarchies that
     * would otherwise not be notified when this is being unmounted.
     */
    _unmountAllExtensions: function() {
        var thisNode = ReactDOM.findDOMNode(this);
        var children = thisNode ? thisNode.children : null;
        if (children && children.length) {
            for (var i = 0; i < children.length; i++) {
                var child = children[i];
                try {
                    if (child) {
                        ReactDOM.unmountComponentAtNode(child);
                    }
                }
                catch (err) {
                    // Log and continue, don't want to stop unmounting children
                    console.log("Error unmounting component", child, err);
                }
            }
        }
    }
});

ExtensionPoint.defaultProps = {
    wrappingElement: "div"
};

ExtensionPoint.propTypes = {
    name: React.PropTypes.string.isRequired,
    wrappingElement: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.element])
};

ExtensionPoint.contextTypes = {
    router: React.PropTypes.object,
    config: React.PropTypes.object
};

/**
 * Provide a static helper to avoid having to expose the store
 */
ExtensionPoint.getExtensions = function getExtensions(name) {
    return store.getExtensions(name);
};

/**
 * Register the existence of an ExtensionPoint and load the extensions. onLoad is (extensions)=>{}
 */
ExtensionPoint.registerExtensionPoint = function registerExtensionPoint (name, onLoad) {
    store.loadExtensions(name, function (extensions) {
        if (typeof onLoad === "function") onLoad(extensions);
    });
};

module.exports = ExtensionPoint;
