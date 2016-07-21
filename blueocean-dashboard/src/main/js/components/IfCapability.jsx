import React, { Component, PropTypes } from 'react';
import { classMetadataStore } from '@jenkins-cd/js-extensions';

const { string } = PropTypes;

export default class IfCapability extends Component {
    constructor(props) {
        super(props);

        this.state = {
            capabilities: undefined,
        };
    }
    
    componentDidMount() {
        const self = this;
        classMetadataStore.getClassMetadata(this.props.dataClass, (classMeta) => {
            self._setState({
                capabilities: classMeta.classes,
            });
        });
    }

    componentWillUnmount() {
        this.unmounted = true;
    }
    
    _setState (stateObj) {
        // Block calls to setState for components that are
        // not in a mounted state.
        if (!this.unmounted) {
            this.setState(stateObj);
        }
    }
 
    render() {
        const { capabilities } = this.state;
        const { capability } = this.props;

        //Exit early, we havent fetched the caps yet.
        if(!capabilities ) {
            return null;
        }

        //console.log("caps",caps);
        if(capabilities.find((cap => cap === capability))) {
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