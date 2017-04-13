module.exports = function(React, ReactDOM, extensions) {
    /**
     * An internal component that inserts things into the (separate) context of mounted extensions. We need this for our
     * configuration object, which helps resolve URLs for media, REST endpoints, etc, and we also need to bridge the
     * "router" context property in order for extensions to be able to use &lt;Link&gt; from React-router.
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
     * Renderer for React component extensions for which other plugins can provide an implementing Component.
     */
    class ExtensionRenderer extends React.Component {
        constructor() {
            super();
        }

        componentWillMount() {
            //ExtensionRenderer.ResourceLoadTracker.onMount(this.props.extensionPoint);
        }

        componentDidMount() {
            this._renderAllExtensions();
        }

        componentDidUpdate() {
            this._renderAllExtensions();
        }

        componentWillUnmount() {
            this._unmountAllExtensions();
        }

        /**
         * This method renders the "leaf node" container divs, one for each registered extension, that live in the same
         * React hierarchy as the &lt;ExtensionRenderer&gt; instance itself. As far as our React is concerned, these are
         * childless divs that are never updated. Actually rendering the extensions themselves is done by
         * _renderAllExtensions.
         */
        render() {
            return React.createElement(this.props.wrappingElement, null, React.createElement('div'));
        }

        /**
         * For each extension, we have created a "leaf node" element in the DOM. This method creates a new React hierarchy
         * for each, and instructs it to render. From that point on we have a separation that keeps the main app insulated
         * from any plugin issues that may cause React to throw while updating. Inspired by Nylas N1.
         */
        _renderAllExtensions() {
            // NB: This needs to be a lot cleverer if the list of extensions for a specific point can change;
            // We will need to link each extension with its containing element, in some way that doesn't leak :) Easy in
            // browsers with WeakMap, less so otherwise.
            const el = ReactDOM.findDOMNode(this);
            if (el) {
                const children = el.children;
                this._renderExtension(el.children[0], this.props.extension);
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

                var errorDiv = React.createElement('div', { className: "error alien" }, `Error rendering ${extension.name}: ${e.toString()}`);
                ReactDOM.render(errorDiv, element);
            }
        }

        /**
         * Clean up child extensions' React hierarchies. Necessary because they live in their own React hierarchies that
         * would otherwise not be notified when this is being unmounted.
         */
        _unmountAllExtensions() {
            var thisNode = ReactDOM.findDOMNode(this);
            var children = thisNode ? thisNode.children : null;
            if (children && children.length) {
                var child = children[0];
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

    ExtensionRenderer.defaultProps = {
        wrappingElement: "div"
    };

    ExtensionRenderer.propTypes = {
        extension: React.PropTypes.any.isRequired,
        filter: React.PropTypes.any,
        wrappingElement: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.element])
    };

    ExtensionRenderer.contextTypes = {
        router: React.PropTypes.object,
        config: React.PropTypes.object
    };

    return ExtensionRenderer;
}
