var React = require('react');
var ReactDOM = require('react-dom');
var ExtensionStore = require('./ExtensionStore.js');
var ResourceLoadTracker = require('./ResourceLoadTracker').instance;

/**
 * An internal component that inserts things into the (separate) context of mounted extensions. We need this for our
 * configuration object, which helps resolve URLs for media, REST endpoints, etc, and we also need to bridge the
 * "router" context property in order for extensions to be able to use &lt;Link&gt; from react-router.
 */
class ContextBridge extends React.Component {

    getChildContext() {
        return {
            router: this.props.router,
            config: this.props.config
        };
    }

    render() {
        return this.props.children;
    }
}

ContextBridge.childContextTypes = {
    router: React.PropTypes.object,
    config: React.PropTypes.object
};

ContextBridge.propTypes = {
    children: React.PropTypes.any,
    router: React.PropTypes.object,
    config: React.PropTypes.object
};

/**
 * Renderer for react component extensions for which other plugins can provide an implementing Component.
 */
export class ExtensionRenderer extends React.Component {
    constructor() {
        super();
        // Initial state is empty. See the componentDidMount and render functions.
        this.state = { extensions: null };
    }
    
    componentWillMount() {
        this._setExtensions();
    }
    
    componentDidMount() {
        ResourceLoadTracker.onMount(this.props.extensionPoint);
        this._renderAllExtensions();
    }

    componentDidUpdate() {
        this._renderAllExtensions();
    }

    componentWillUnmount() {
        this._unmountAllExtensions();
        ResourceLoadTracker.onUnmount(this.props.extensionPoint);
    }
    
    _setExtensions() {
        ExtensionStore.instance.getExtensions(this.props.extensionPoint, this.props.dataType,
            extensions => this.setState({extensions: extensions})
        );
    }
    
    /**
     * This method renders the "leaf node" container divs, one for each registered extension, that live in the same
     * react hierarchy as the &lt;ExtensionRenderer&gt; instance itself. As far as our react is concerned, these are
     * childless divs that are never updated. Actually rendering the extensions themselves is done by
     * _renderAllExtensions.
     */
    render() {
        var extensions = this.state.extensions;
        if (!extensions) {
            return null; // this is called before extension data is available
        }
        
        // Add a <div> for each of the extensions. See the __renderAllExtensions function.
        var extensionDivs = [];
        for (var i = 0; i < extensions.length; i++) {
            extensionDivs.push(<div key={i}/>);
        }
        return React.createElement(this.props.wrappingElement, null, extensionDivs);
    }

    /**
     * For each extension, we have created a "leaf node" element in the DOM. This method creates a new react hierarchy
     * for each, and instructs it to render. From that point on we have a separation that keeps the main app insulated
     * from any plugin issues that may cause react to throw while updating. Inspired by Nylas N1.
     */
    _renderAllExtensions() {
        // NB: This needs to be a lot cleverer if the list of extensions for a specific point can change;
        // We will need to link each extension with its containing element, in some way that doesn't leak :) Easy in
        // browsers with WeakMap, less so otherwise.
        const el = ReactDOM.findDOMNode(this);
        if (el) {
            const children = el.children;
            if (children) {
                const extensions = this.state.extensions;

                // The number of children should be exactly the same as the number
                // of extensions. See the render function for where these are added.
                if (!extensions || extensions.length !== children.length) {
                    console.error('Unexpected error in Jenkins ExtensionRenderer rendering (' + this.props.extensionPoint + '). Expecting a child DOM node for each extension point.');
                    return;
                }
                // render each extension on the allocated child node.
                for (var i = 0; i < extensions.length; i++) {
                    this._renderExtension(children[i], extensions[i]);
                }
            }
        }
    }

    /** Actually render an individual extension */
    _renderExtension(element, extension) {
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
    }

    /**
     * Clean up child extensions' react hierarchies. Necessary because they live in their own react hierarchies that
     * would otherwise not be notified when this is being unmounted.
     */
    _unmountAllExtensions() {
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
}

ExtensionRenderer.defaultProps = {
    wrappingElement: "div"
};

ExtensionRenderer.propTypes = {
    extensionPoint: React.PropTypes.string.isRequired,
    dataType: React.PropTypes.any,
    wrappingElement: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.element])
};

ExtensionRenderer.contextTypes = {
    router: React.PropTypes.object,
    config: React.PropTypes.object
};
