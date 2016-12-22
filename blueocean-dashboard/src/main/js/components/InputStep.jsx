import React, { Component, PropTypes } from 'react';
import isoFetch from 'isomorphic-fetch';
import { i18nTranslator } from '@jenkins-cd/blueocean-core-js';
import { supportedInputTypesMapping } from './parameter/index';

/**
 * Simple helper to stop stopPropagation
 * @param event the event we want to cancel
 */
const stopProp = (event) => {
    event.stopPropagation();
};

/**
 * Translate function
 */
const translate = i18nTranslator('blueocean-dashboard');

/**
 * Creating a "<form/>"less form to submit the input parameters requested by the user in pipeline.
 *
 * We keep all form data in state and change them onChange and onToggle (depending of the parameter
 * type). We match the different supported inputTypes with a mapping functions
 * @see supportedInputTypesMapping
 * That mapping delegates to the specific implementation where we further delegate to JDL components.
 * In case you want to register a new mapping you need to edit './parameter/index' to add a new mapping
 * and further in './parameter/commonProptypes' you need to include the new type in the oneOf array.
 */
export default class InputStep extends Component {

    // we start with an empty state
    state = {};

    /**
     * react life cycle mapper to invoke the creation of the form state
     */
    componentWillMount() {
        this.createFormState(this.props);
    }

    /**
     * react life cycle mapper to invoke the creation of the form state
     */
    componentWillReceiveProps(nextProps) {
        this.createFormState(nextProps);
    }

    /**
     * Create a replica of the input parameters in state. Basically we just dump the whole item.
     * @param props
     */
    createFormState(props) {
        const { node } = props;
        // console.log({ node });
        if (node) {
            const { config = {} } = this.context;
            const {
                input: { id, parameters: inputParameters },
                _links: { self: { href } },
            } = node;
            const parameters = {};
            inputParameters.map((parameter, index) => {
                parameters[index] = parameter;
                return parameter;
            });
            this.setState({ parameters, id, href: `${config._rootURL}${href}` });
        }
    }

    /**
     * change a specific parameter value and update the state.
     * @param index - which parameter we need to change
     * @param event - the event leading to the change
     */
    changeParameter(index, event) {
        // console.log('onChange', index, event);
        const originalParameters = this.state.parameters;
        originalParameters[index].defaultParameterValue.value = event;
        this.setState({ parameters: originalParameters });
    }

    /**
     * Creates an array from the parameter object which is in the current state
     * @returns array - of values
     */
    stateParametersToArray() {
        return Object.values(this.state.parameters).map(item => {
            const returnArray = { name: item.name, value: item.defaultParameterValue.value };
            return returnArray;
        });
    }

    /**
     * Generic submit function. The calculations for the url has been done in
     * @see createFormState Here we simply POST the data to the server.
     * @param body - could be ok or cancel body
     */
    submitForm(body) {
        const { href } = this.state;
        const fetchOptions = {
            credentials: 'include',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        };
        isoFetch(href, fetchOptions)
            .then(
                response => console.log(response)
            );
    }

    /**
     * Submit the form as "cancel" out of the state data id.
     */
    cancelForm() {
        const { id } = this.state;
        const body = { id, abort: true };
        this.submitForm(body);
    }

    /**
     * Submit the form as "ok" out of the state data parameters and id.
     */
    okForm() {
        const { id } = this.state;
        const body = { id, parameters: this.stateParametersToArray() };
        this.submitForm(body);
    }

    render() {
        const { parameters } = this.state;
        // Early out
        if (!parameters) {
            return null;
        }
        const { input: { message, ok } } = this.props.node;

        // console.log('state', this.state);
        // console.log('stateToFormSubmit', this.stateParametersToArray());

        const cancelCaption = translate('rundetail.input.cancel');
        return (<div className="inputStep">
            <h3>{message}</h3>
            <div className="inputBody">
                {
                    Object.values(parameters).map((parameter, index) => {
                        const { type } = parameter;
                        const returnValue = supportedInputTypesMapping[type];
                        if (returnValue) {
                            return React.createElement(returnValue, {
                                ...parameter,
                                key: index,
                                onChange: (event) => this.changeParameter(index, event),
                            });
                        }
                        return <div>No component found for type {type}.</div>;
                    })
                }
            </div>
            <div onClick={(event => stopProp(event))} className="inputControl">
                <a title={cancelCaption} onClick={() => this.cancelForm()} className="btn inverse" >
                    <span className="button-label">{cancelCaption}</span>
                </a>
                <a title={ok} onClick={() => this.okForm()} className="btn" >
                    <span className="button-label">{ok}</span>
                </a>
            </div>
        </div>);
    }
}

const { object, shape } = PropTypes;

InputStep.propTypes = {
    node: shape().isRequired,
};

InputStep.contextTypes = {
    config: object.isRequired,
};
