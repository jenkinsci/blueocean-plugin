import React, { Component } from 'react';
import { classMetadataStore } from '@jenkins-cd/js-extensions';

export const capabilityStore = pipelineClass => ComposedComponent => class extends Component {
    constructor(props) {
        super(props);

        this.state = {
            capabilities: {
                has: () => false,
            },
        };
    }

    componentDidMount() {
        const self = this;
        classMetadataStore.getClassMetadata(pipelineClass(this.props), (classMeta) => {
            self._setState({
                capabilities: self._classesToObj(classMeta.classes),
            });
        });
    }

    componentWillUnmount() {
        this.unmounted = true;
    }
    
    _classesToObj(classes) {
        if (!classes) {
            return {
                has: () => false,
            };
        }

        return {
            classes,
            has: capability => classes.find(_class => _class === capability) !== undefined,
        };
    }
    _setState(stateObj) {
        // Block calls to setState for components that are
        // not in a mounted state.
        if (!this.unmounted) {
            this.setState(stateObj);
        }
    }

    render() {
        const { capabilities } = this.state;
       
        // Early out. Doing it here means we don't have to do it in
        // the composed componenet
        if (!capabilities.classes) {
            return null;
        }

        // This passes all props and state to ComposedComponent where
        // they will all show as props.
        return <ComposedComponent {...this.props} {...this.state} />;
    }
};

