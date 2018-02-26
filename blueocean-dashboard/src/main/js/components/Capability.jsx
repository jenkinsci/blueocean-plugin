import React, { Component } from 'react';
import update from 'react-addons-update';
import { classMetadataStore } from '@jenkins-cd/js-extensions';
import { Record } from 'immutable';

/* eslint new-cap: [0] */
export class CapabilityRecord extends Record({ classNames: [] }) {
    contains(capability) {
        if (this.classNames) {
            return this.classNames.find(className => className === capability) !== undefined;
        }

        return false;
    }
}

/**
 * capabilityStore is a enhances components to inject capabilities for
 * a given class.
 *
 * Usage: capabilityStore(classFunction)(React.Component) where
 *
 * It is used much like connect() is for redux. classFunction is a function
 * that takes this.props as an argument and returns either a string or array
 * of classes to lookup capabilities for.
 *
 * props => props.pipeline._class
 * props => [props.pipeline._class, props.favoriteData._class]
 *
 * capabilityStore will inject "capabilities" as a property into the component
 * so you need to add "capabilities: object" to propTypes. capabilities.has(MULTIBRANCH)
 * can be called to find out if a capability is supported. A list of capabilities is in the
 * Capabilities.js file.
 *
 * Note that the syntax classesFunction => ComposedComponent => class extends Component
 * is ES6 shorthand for:
 *
 * function capabilityStore(classesFunction) {
 *    return function(ComposedComponent) {
 *        return new Component {
 *            render() {
 *                return <ComposedComponent />
 *            }
 *        }
 *    }
 * }
 */
export const capabilityStore = classesFunction => ComposedComponent => class extends Component {
    constructor(props) {
        super(props);

        this.state = {
            capabilities: {},
        };
    }

    componentDidMount() {
        const self = this;
        let classesMap = classesFunction(this.props);

        if (typeof classesMap === 'string') {
            classesMap = [classesMap];
        }

        for (const className of classesMap) {
            classMetadataStore.getClassMetadata(className, (classMeta) => {
                self._setState(className, new CapabilityRecord({ classNames: classMeta.classes }));
            });
        }
    }

    componentWillUnmount() {
        this.unmounted = true;
    }

    _setState(key, value) {
        // Block calls to setState for components that are
        // not in a mounted state.
        if (!this.unmounted) {
            const newData = { capabilities: {} };
            newData.capabilities[key] = { $set: value };
            this.setState(previousState => update(previousState, newData));
        }
    }

    render() {
        const { capabilities } = this.state;
        
        // Early out. Doing it here means we don't have to do it in
        // the composed component
        let classesMap = classesFunction(this.props);
        if (classesMap === undefined || classesMap === null) {
            throw new Error('capabilityStore function did not find class in props.');
        }

        if (typeof classesMap === 'string') {
            classesMap = [classesMap];
        }
        
        for (const className of classesMap) {
            if (!capabilities[className] || !capabilities[className].classNames) {
                return null;
            }
        }

        // This passes all props and state to ComposedComponent where
        // they will all show as props.
        return <ComposedComponent {...this.props} {...this.state} />;
    }
};

