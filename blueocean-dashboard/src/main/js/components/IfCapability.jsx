import React, { Component, PropTypes } from 'react';
import { capabilityStore } from './Capability';

const { string, object } = PropTypes;

class IfCapability extends Component {
    render() {
        const { className, capability, capabilities } = this.props;
       
        if (capabilities[className].contains(capability)) {
            return this.props.children;
        }

        return null;
    }
}

IfCapability.propTypes = {
    className: string,
    capability: string,
    capabilities: object,
    children: React.PropTypes.node,
};

export default capabilityStore(props => props.className)(IfCapability);

