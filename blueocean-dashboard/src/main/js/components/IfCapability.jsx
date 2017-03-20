import React, { Component, PropTypes } from 'react';
import { capabilityStore } from './Capability';

class IfCapability extends Component {
    render() {
        const { className, capability, capabilities } = this.props;

        // since one or more 'capability' can be supplied, check if at least one of the supplied capabilities is present
        const capabilityList = capability instanceof Array ? capability : [capability];
        const result = capabilityList.some((capable) => capabilities[className].contains(capable));

        if (result) {
            return this.props.children;
        }

        return null;
    }
}

IfCapability.propTypes = {
    className: PropTypes.string,
    capability: PropTypes.oneOfType([PropTypes.string, PropTypes.array]),
    capabilities: PropTypes.object,
    children: PropTypes.node,
};

export default capabilityStore(props => props.className)(IfCapability);

