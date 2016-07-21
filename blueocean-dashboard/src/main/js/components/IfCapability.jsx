import React, { Component, PropTypes } from 'react';
import {
    actions,
    capabilities as capabilitiesSelector,
    createSelector,
    connect,
} from '../redux';
const { object, array, func, string , bool} = PropTypes;

export default class IfCapability extends Component {
    constructor(props) {
        super(props);
        const { dataClass } = this.props;
        this.props.fetchCapabilitiesIfNeeded(dataClass);

    }
    render() {
        const { dataClass, capabilities ,capability } = this.props;

        //Exit early, we havent fetched the caps yet.
        if(!capabilities || !capabilities[dataClass]) {
            return false;
        }

        //console.log("caps",caps);
        if(capabilities[dataClass].find((cap => cap === capability))) {
            return this.props.children;
        } else {
            return null;
        }
    }
};

IfCapability.propTypes = {
    dataClass: string,
    capability: string,
};


const selectors = createSelector([capabilitiesSelector], (capabilities) => ({ capabilities }));

export default connect(selectors, actions)(IfCapability);
