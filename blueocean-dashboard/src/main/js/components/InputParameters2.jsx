import React, { Component, PropTypes } from 'react';
import isoFetch from 'isomorphic-fetch';
import {
    i18nTranslator,
    ModalView,
  ModalBody,
  ModalHeader,
  ToastUtils,
} from '@jenkins-cd/blueocean-core-js';
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
const t = i18nTranslator('blueocean-dashboard');

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
export default class InputParameters extends Component {

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
        const { input } = props;
        if (input) {
            const { config = {} } = this.context;
            const { parameters: inputParameters, _links: { self: { href } } } = input;
            const parameters = {};
            inputParameters.map((parameter, index) => {
                parameters[index] = parameter;
                return parameter;
            });
            this.setState({ parameters, href: `${config._rootURL}${href}/runs/` });
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
        const { parameters } = this.state;
        return Object.keys(parameters).map(key => {
            const item = parameters[key];
            return { name: item.name, value: item.defaultParameterValue.value };
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
        return isoFetch(href, fetchOptions)
            .then(
                response => {
                    if (response.status >= 300 || response.status < 200) {
                        const error = new Error(response.statusText);
                        error.response = response;
                        throw error;
                    }
                    return response.json();
                }
            );
    }

    /**
     * Submit the form as "cancel"
     */
    cancelForm() {
        // FIXME: abort
        // this.submitForm(body);
    }

    /**
     * Submit the form as "ok" out of the state data parameters.
     */
    okForm() {
        const body = { parameters: this.stateParametersToArray() };
        this.submitForm(body)
            .then((runInfo) => ToastUtils
              .createRunStartedToast(this.props.runnable, runInfo, this.props.onNavigation));
    }

    render() {
        const { parameters } = this.state;
        // Early out
        if (!parameters) {
            return null;
        }
        const message = t('parameterized.pipeline.header', { defaultValue: 'Pipeline parameter' });
        const ok = t('parameterized.pipeline.submit', { defaultValue: 'Build' });
        const parametersArray = Object.keys(parameters).map(key => parameters[key]);

        // console.log('state', this.state);
        // console.log('stateToFormSubmit', this.stateParametersToArray());
        const cancelCaption = t('rundetail.input.cancel');
        const cancelButton = (<a title={cancelCaption} onClick={() => this.cancelForm()} className="btn inputStepCancel run-button btn-secondary" >
            <span className="button-label">{cancelCaption}</span>
        </a>);
        return (
          <ModalView
            isVisible={this.props.visible}
            hideOnOverlayClicked
            transitionClass="expand-in"
            transitionDuration={150}
        >
                    <ModalHeader>
                        {message}
                    </ModalHeader>

                    <ModalBody>
                        <div>
                            <div className="inputBody">
                              {
                                parametersArray.map((parameter, index) => {
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
                                <span>{cancelButton}</span>
                                <a title={ok} onClick={() => this.okForm()} className="btn inputStepSubmit" >
                                    <span className="button-label">{ok}</span>
                                </a>
                            </div>
                        </div>
                    </ModalBody>
                </ModalView>);
    }
}

const { bool, func, object, shape } = PropTypes;

InputParameters.propTypes = {
    visible: bool,
    _links: shape,
    node: shape().isRequired,
    onNavigation: func,
    runnable: object,
};

InputParameters.contextTypes = {
    config: object.isRequired,
};
