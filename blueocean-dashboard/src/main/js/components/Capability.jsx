import React, { Component } from 'react';
import update from 'react-addons-update';
import { classMetadataStore } from '@jenkins-cd/js-extensions';

export const capabilityStore = classes => ComposedComponent => class extends Component {
    constructor(props) {
        super(props);

        this.state = {
            capabilities: {},
        };
    }

    componentDidMount() {
        const self = this;
        let _classes = classes(this.props);

        if(typeof _classes === "string") {
            _classes = [_classes];
        }

        for(let _class of _classes) {
            classMetadataStore.getClassMetadata(_class, (classMeta) => {
                self._setState(_class, self._classesToObj(classMeta.classes));
            });
        }
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
    _setState(key, value) {

        // Block calls to setState for components that are
        // not in a mounted state.
        if (!this.unmounted) {
            let newData = { capabilities : {}};
            newData.capabilities[key] = { $set: value };
            this.setState(previousState => update(previousState, newData));     
        }
    }

    render() {
        const { capabilities } = this.state;
       
        // Early out. Doing it here means we don't have to do it in
        // the composed componenet
        let _classes = classes(this.props);

        if(typeof _classes === "string") {
            _classes = [_classes];
        }
        for(let _class of _classes) {
            if (!capabilities[_class] || !capabilities[_class].classes) {
                return null;
            }
        }

        // This passes all props and state to ComposedComponent where
        // they will all show as props.
        return <ComposedComponent {...this.props} {...this.state} />;
    }
};

