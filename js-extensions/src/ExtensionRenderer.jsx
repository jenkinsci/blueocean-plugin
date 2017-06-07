var React = require('react');
var ReactDOM = require('react-dom');
var ContextBridge = require('./ContextBridge').ContextBridge;
var importedExtensionStore = require('./ExtensionStore.js').instance;
var importedResourceLoadTracker = require('./ResourceLoadTracker').instance;

/**
 * Renderer for react component extensions for which other plugins can provide an implementing Component.
 */
export default class ExtensionRenderer extends React.Component {

    constructor() {
        super();
        // Initial state is empty. See the componentDidMount and render functions.
        this.state = { extensions: null };
    }

    componentWillMount() {
        this._setExtensions(this.props);
    }

    componentDidMount() {
        ExtensionRenderer.resourceLoadTracker.onMount(this.props.extensionPoint, () => {
            this._renderAllExtensions();
        });
    }

    componentWillUpdate(nextProps, nextState) {
        if (!nextState.extensions) {
            this._setExtensions(nextProps);
        }
    }

    componentDidUpdate() {
        this._renderAllExtensions();
    }

    componentWillUnmount() {
        this._unmountAllExtensions();
    }

    /**
     * Force a reload and re-render of all extensions registered with this ExtensionPoint.
     * Useful if the props (such as 'filter') have changed and need to be re-evaluated.
     */
    reloadExtensions() {
        // triggers a reload of extensions via componentWillUpdate
        this.setState({
            extensions: null,
        });
    }

    _setExtensions() {
        ExtensionRenderer.extensionStore.getExtensions(this.props.extensionPoint, this.props.filter,
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
            // Rendered before extension data is available - if data is loaded but no
            // extensions are found, we will get [] rather than null, and will want to
            // render an empty wrappingElement, or possibly "default" children
            return null;
        }

        var newChildren = [];

        // Add a <div> for each of the extensions. See the __renderAllExtensions function.
        for (var i = 0; i < extensions.length; i++) {
            newChildren.push(<div key={i}/>);
        }

        if (newChildren.length === 0 && this.props.children) {
            newChildren = this.props.children;
        }

        const {
            className,
            extensionPoint,
            wrappingElement
        } = this.props;

        const classNames = ['ExtensionPoint', extensionPoint.replace(/\.+/g,'-')];

        if (className) {
            classNames.push(className);
        }

        const newProps = {
            className: classNames.join(' ')
        };

        return React.createElement(wrappingElement, newProps, newChildren);
    }

    /**
     * For each extension, we have created a "leaf node" element in the DOM. This method creates a new react hierarchy
     * for each, and instructs it to render. From that point on we have a separation that keeps the main app insulated
     * from any plugin issues that may cause react to throw while updating. Inspired by Nylas N1.
     */
    _renderAllExtensions() {
        const extensions = this.state.extensions;

        if (!extensions || extensions.length === 0) {
            // No extensions loaded. Return early because we may have default DOM children.
            return;
        }

        // NB: This needs to be a lot cleverer if the list of extensions for a specific point can change;
        // We will need to link each extension with its containing element, in some way that doesn't leak :) Easy in
        // browsers with WeakMap, less so otherwise.
        const el = ReactDOM.findDOMNode(this);
        if (el) {
            const children = el.children;
            if (children) {

                // The number of children should be exactly the same as the number
                // of extensions. See the render function for where these are added.
                if (extensions.length !== children.length) {
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

        const extensions = this.state.extensions;

        if (!extensions || extensions.length === 0) {
            // No extensions loaded. Return early because we may have default DOM children which react
            // will unmount normally
            return;
        }

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
    children: React.PropTypes.node,
    extensionPoint: React.PropTypes.string.isRequired,
    filter: React.PropTypes.any,
    wrappingElement: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.element])
};

ExtensionRenderer.contextTypes = {
    router: React.PropTypes.object,
    config: React.PropTypes.object
};