import * as React from 'react';
import * as ReactDOM from 'react-dom';

import extensionRegistry, { extensions } from './Extensions';

interface ContextBridgeProps {
    children: any;
    router: Object;
    config: Object;
}

/**
 * An internal component that inserts things into the (separate) context of mounted extensions. We need this for our
 * configuration object, which helps resolve URLs for media, REST endpoints, etc, and we also need to bridge the
 * "router" context property in order for extensions to be able to use &lt;Link&gt; from react-router.
 */
class ContextBridge extends React.Component<ContextBridgeProps,{}> {
    static childContextTypes = {
        router: Object,
        config: Object
    };
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

export interface ExtensionRendererProps {
    extension: any;
    filter?: any;
    wrappingElement: String | any;
}

/**
 * Renderer for react component extensions for which other plugins can provide an implementing Component.
 */
export default class ExtensionRenderer extends React.Component<ExtensionRendererProps,{}> {
    context = {
        router: Object,
        config: Object
    };

    static contextTypes = {
        router: Object,
        config: Object
    };

    public static defaultProps: ExtensionRendererProps = {
        extension: undefined,
        wrappingElement: "div"
    };

    constructor() {
        super();
        // Initial state is empty. See the componentDidMount and render functions.
        this.state = { extensions: null };
    }

    componentWillMount() {
        this._renderAllExtensions();
    }

    componentDidMount() {
        //ExtensionRenderer.ResourceLoadTracker.onMount(this.props.extensionPoint);
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
     * react hierarchy as the &lt;ExtensionRenderer&gt; instance itself. As far as our react is concerned, these are
     * childless divs that are never updated. Actually rendering the extensions themselves is done by
     * _renderAllExtensions.
     */
    render() {
        return React.createElement(this.props.wrappingElement, null, <div/>);
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
            this._renderExtension(el.children[0], this.props.extension);
        }
    }

    /** Actually render an individual extension */
    _renderExtension(element: any, extension: any) {
        var component = React.createElement(extension, this.props);
        try {
            var contextValuesAsProps: any = {
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
