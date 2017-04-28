/**
 * Created by cmeyers on 10/21/16.
 */
var React = require('react');
var PropTypes = React.PropTypes;

/**
 * An internal component that inserts things into the (separate) context of mounted extensions. We need this for our
 * configuration object, which helps resolve URLs for media, REST endpoints, etc, and we also need to bridge the
 * "router" context property in order for extensions to be able to use &lt;Link&gt; from react-router.
 */
export class ContextBridge extends React.Component {

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
    router: PropTypes.object,
    config: PropTypes.object
};

ContextBridge.propTypes = {
    children: PropTypes.any,
    router: PropTypes.object,
    config: PropTypes.object
};
