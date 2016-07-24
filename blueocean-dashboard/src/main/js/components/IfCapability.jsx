import React, { Component, PropTypes } from 'react';
import { capabilityStore } from './Capability';

const { string, object } = PropTypes;

class IfCapability extends Component {
    render() {
        const { _class, capability, capabilities } = this.props;
       
        if (capabilities[_class].has(capability)) {
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

