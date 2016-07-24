import React, { Component, PropTypes } from 'react';
import { capabilityStore } from './Capability';

const { string, object } = PropTypes;

class IfCapability extends Component {
    render() {
        const { capabilities } = this.props;
        const { capability } = this.props;
       
        if (capabilities.has(capability)) {
            return this.props.children;
        }

        return null;
    }
}

IfCapability.propTypes = {
    _class: string,
    capability: string,
    capabilities: object,
    children: React.PropTypes.node,
};

export default capabilityStore(props => props._class)(IfCapability);

