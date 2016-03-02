var React = require('react');
var ReactDOM = require('react-dom');
var store = require('./store.js');

var ExtensionPoint = React.createClass({

    getInitialState: function () {
        // Initial state is empty. See the componentDidMount and render functions.
        return {};
    },

    componentDidMount: function() {
        var thisEp = this;
        store.loadExtensions(this.props.name, function(extensions) {
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
    },

    // TODO: resolve possible differences/inconsistencies between render, _renderAllExtensions and _renderExtension
    // Something looks wrong with all of that... nested render, render, render

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

    _renderAllExtensions: function() {
        // TODO: This needs to be a lot cleverer if the list of extensions for a specific point can change
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
            ReactDOM.render(component, element);
        } catch (e) {
            console.log("error rendering", extension.name, e);

            var errorDiv = <div className="error alien">Error rendering {extension.name}: {e.toString()}</div>;
            ReactDOM.render(errorDiv, element);
        }
    },

    /** Clean up child extensions */
    _unmountAllExtensions: function() {
        var children = ReactDOM.findDOMNode(this).children;
        for (var i = 0; i < children.length; i++) {
            ReactDOM.unmountComponentAtNode(children[i]); // TODO: Can this throw?
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

module.exports = ExtensionPoint;
