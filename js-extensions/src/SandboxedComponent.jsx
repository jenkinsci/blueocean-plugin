/**
 * Created by cmeyers on 10/21/16.
 */
var React = require('react');
var ReactDOM = require('react-dom');
var PropTypes = React.PropTypes;

var ContextBridge = require('./ContextBridge').ContextBridge;
var ErrorUtils = require('./ErrorUtils').default;

/**
 * Component that renders any passed-in children via a separate ReactDOM.render call.
 * Useful for rendering untrusted React elements: errors will be trapped and displayed so the main UI isn't broken.
 */
export class SandboxedComponent extends React.Component {

    constructor(props) {
        super(props);

        this.domNode = null;
    }

    componentWillReceiveProps(nextProps) {
        if (nextProps.children !== this.props.children) {
            this._cleanupChild();
            this._renderChild(nextProps);
        }
    }

    componentDidMount() {
        this._renderChild(this.props);
    }

    componentWillUnmount() {
        this._cleanupChild();
        this.domNode = null;
    }

    _renderChild(props) {
        if (!this.domNode) {
            return null;
        }

        try {
            var contextValuesAsProps = {
                config: this.context.config,
                router: this.context.router
            };
            var bridgedComponent = React.createElement(ContextBridge, contextValuesAsProps, props.children);
            ReactDOM.render(bridgedComponent, this.domNode);
        } catch (e) {
            console.warn('error rendering: ', e);
            ReactDOM.render(ErrorUtils.errorToElement(e), this.domNode);
        }
    }

    _cleanupChild() {
        if (this.domNode) {
            try {
                ReactDOM.unmountComponentAtNode(this.domNode);
            }
            catch (err) {
                console.log("Error unmounting component", err);
            }
        }
    }

    render() {
        const extraClass = this.props.className || '';

        return (
            <div className={`sandbox-component ${extraClass}`} ref={(node) => { this.domNode = node; }}></div>
        );
    }
}

SandboxedComponent.propTypes = {
    children: PropTypes.node,
    className: PropTypes.string,
};

SandboxedComponent.contextTypes = {
    router: PropTypes.object,
    config: PropTypes.object,
};
