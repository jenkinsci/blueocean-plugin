import React, { Component, PropTypes } from 'react';
import isoFetch from 'isomorphic-fetch';
import { supportedInputTypesMapping } from './parameter/index';

const stopProp = (event) => {
    event.stopPropagation();
};

export default class InputStep extends Component {

    constructor(props) {
        super(props);
        this.state = {};
    }

    /**
     * life cycle mapper to invoke the creation of the form state
     */
    componentWillMount() {
        this.createFormState(this.props);
    }

    /**
     * life cycle mapper to invoke the creation of the form state
     */
    componentWillReceiveProps(nextProps) {
        this.createFormState(nextProps);
    }

    /**
     * Create a replica of the input parameters in state.
     * @param props
     */
    createFormState(props) {
        const { node } = props;
        console.log({node});
        if (node) {
            const { config = {} } = this.context;
            const {
                input: { id, parameters: inputParameters, message, ok },
                _links: { self: { href } }
            } = node;
            const parameters = {};
            inputParameters.map((parameter, index) => {
                parameters[index] = parameter;
            });
            this.setState({ parameters, id, href: `${config._rootURL}${href}` });
        }
    }

    /**
     * Creates an array from the parameter object which is in the current state
     * @returns array - of values
     */
    stateParametersToArray() {
        return Object.values(this.state.parameters).map(item => {
            return { name: item.name, value: item.defaultParameterValue.value }
        });
    }

    /**
     * change a specific parameter value and update the state.
     * @param index - which parameter we need to change
     * @param event - the event leading to the change
     */
    changeParameter(index, event) {
        console.log('onChange', index, event);
        const originalParameters = this.state.parameters;
        originalParameters[index].defaultParameterValue.value = event;
        this.setState({ parameters: originalParameters });
    }

    /**
     * Submit the form out of the state data
     */
    submitForm() {
        const { href, id } = this.state;
        const body = { id, parameter: this.stateParametersToArray() };
        const fetchOptions = {
            credentials: 'include',
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(body),
        };
        isoFetch(href, fetchOptions).then(response => console.log(response));
    }

    render() {
        const { parameters } = this.state;
        // Early out
        if (!parameters) {
            return null;
        }
        const { input: { message, ok } } = this.props.node;

        console.log('state', this.state);
        console.log('stateToFormSubmit', this.stateParametersToArray());

        return (<div className="inputStep">
            <h1>{message}</h1>
            <div onClick={(event => stopProp(event))}>
                <a title={ok} onClick={() => this.submitForm()}>
                    <span className="button-label">{ok}</span>
                </a>
            </div>
            {
                Object.values(parameters).map((parameter, index) => {
                    const { type } = parameter;
                    const returnValue = supportedInputTypesMapping[type];
                    if (returnValue) {
                        return React.createElement(returnValue, {
                            ...parameter,
                            key: index,
                            onChange: (event) => this.changeParameter(index, event)
                        });
                    }
                    return <div>No component found for type {type}.</div>;
                })
            }
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
